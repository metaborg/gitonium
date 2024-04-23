package mb.gitonium.git

import org.eclipse.jgit.api.Git
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.Charset


/**
 * Uses JGit for Git operations on a specific repository.
 */
class JGitRepo(
    /** The repository working directory. */
    override val directory: File,
) : GitRepo {
    private var _git: Git? = null
    /** The [Git] object, which is created if it doesn't exist. */
    private var git: Git
        get() = _git ?: Git.open(directory)
        set(value) {
            // Close the old Git instance
            _git?.close()
            _git = value
        }


    override fun getGitVersion(): String? {
        return Git::class.java.getPackage()?.implementationVersion
    }

    @Throws(IOException::class)
    override fun getCurrentBranch(): String {
        return git.repository.branch
    }

    @Throws(IOException::class)
    override fun getCurrentCommitHash(short: Boolean): String {

        return runJGitCommand(
            "rev-parse",
            *(if (short) listOf("--short=7") else emptyList()).toTypedArray(),
            "--verify",
            "HEAD",
        )
    }

    @Throws(IOException::class)
    override fun getIsClean(): Boolean {
        return try {
            getStatus().isEmpty()
        } catch (ex: CommandException) {
            if (ex.exitCode == 128 && "Repository has neither a working tree, nor an index" in ex.stderr) {
                // JGit does not support the `status` command for a bare repository.
                true
            } else {
                throw ex
            }
        }
    }

    @Throws(IOException::class)
    override fun getTagDescription(vararg patterns: String): String {
        return runJGitCommand(
            "describe",
            "--tags",           // Both annotated and non-annotated tags
            "--abbrev=7",       // Abbreviate the commit hash to 7 or more characters (however many are needed to make it unique)
            "--first-parent",   // Only consider the first parent of the commit when encountering a merge commit
            "--always",         // Just the abbreviated commit hash if no tag is found
            *patterns.map { "--match=$it" }.toTypedArray(),
            "HEAD",
        )
    }

    @Throws(IOException::class)
    override fun getStatus(): String {
        return runJGitCommand("status", "--porcelain")
    }

    @Throws(IOException::class)
    override fun init() {
        runJGitCommand("init")
    }

    @Throws(IOException::class)
    override fun addAll() {
        runJGitCommand("add", "--all")
    }

    @Throws(IOException::class)
    override fun commit(message: String) {
        runJGitCommand("commit", "-m", message)
    }

    @Throws(IOException::class)
    override fun detach() {
        runJGitCommand("checkout", "--detach")
    }

    @Throws(IOException::class)
    override fun tag(tagName: String) {
        runJGitCommand("tag", tagName)
    }

    /**
     * Runs a JGit command.
     *
     * @param args The command arguments.
     * @return The output of the command.
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(CommandException::class, IOException::class, InterruptedException::class)
    fun runJGitCommand(vararg args: String): String {
        require(args.isNotEmpty()) { "No command arguments provided." }

        val (exitCode, stdout, stderr) = runAsCommand {
            org.eclipse.jgit.pgm.Main.main(listOf("--git-dir", directory.absolutePath).toTypedArray() + args)
        }

        if (exitCode != 0) throw CommandException("jgit " + args.joinToString(" "), exitCode, stderr.takeIf { it.isNotBlank() } ?: stdout)
        return stdout.trim()
    }

    companion object {
        private fun runAsCommand(
            block: () -> Unit,
        ): Triple<Int, String, String> {
            val newSecurityManager = NoExitSecurityManager(System.getSecurityManager())
            val newStandardOut = ByteArrayOutputStream()
            val newStandardErr = ByteArrayOutputStream()

            // Intercept calls to `System.exit` to prevent the JVM from exiting.
            val originalSecurityManager = System.getSecurityManager()
            System.setSecurityManager(newSecurityManager)

            try {
                // Redirect standard output to capture it.
                val originalStandardOut = System.out
                val stdOutPrintStream = PrintStream(newStandardOut, true, Charset.defaultCharset().name())
                System.setOut(stdOutPrintStream)

                try {
                    // Redirect standard error to capture it.
                    val originalStandardErr = System.err
                    val stdErrPrintStream = PrintStream(newStandardErr, true, Charset.defaultCharset().name())
                    System.setErr(stdErrPrintStream)

                    try {
                        block()
                    } finally {
                        stdErrPrintStream.flush()
                        System.setErr(originalStandardErr)
                    }
                } finally {
                    stdOutPrintStream.flush()
                    System.setOut(originalStandardOut)
                }
            } catch (_: CheckExitCalled) {
                // Expected and ignored.
            } finally {
                System.setSecurityManager(originalSecurityManager)
            }

            return Triple(
                newSecurityManager.firstExitCode ?: 0,
                newStandardOut.toString(Charset.defaultCharset()),
                newStandardErr.toString(Charset.defaultCharset()),
            )
        }
    }
}




