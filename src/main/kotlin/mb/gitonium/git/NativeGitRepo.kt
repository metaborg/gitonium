package mb.gitonium.git

import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Calls Git commands for a specific repository.
 *
 * This implementation assumes a local installation of Git, reachable through the `git` command.
 */
class NativeGitRepo(
    /** The current working directory. */
    override val directory: File,
): GitRepo {

    override fun getGitVersion(): String? {
        return try {
            runGitCommand("--version").substringAfter("git version ").substringBefore('\n')
        } catch (e: CommandException) {
            null
        }
    }

    @Throws(IOException::class)
    override fun getCurrentBranch(): String {
        // NOTE: `git rev-parse --abbrev-ref HEAD` would often also work to obtain the branch name.
        //  However, if the current repository has no commits yet, it will fail where `git branch --show-current` will not.
        //  See also: https://stackoverflow.com/a/78100106/146622
        return runGitCommand("branch", "--show-current")
    }

    @Throws(IOException::class)
    override fun getCurrentCommitHash(short: Boolean): String {
        return runGitCommand("rev-parse",
            *(if (short) listOf("--short=7") else emptyList()).toTypedArray(),
            "--verify",
            "HEAD",
        )
    }

    @Throws(IOException::class)
    override fun getIsClean(): Boolean {
        return getStatus().isEmpty()
    }

    @Throws(IOException::class)
    override fun getTagDescription(vararg patterns: String): String {
        return runGitCommand("describe",
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
        return runGitCommand("status", "--porcelain")
    }

    @Throws(IOException::class)
    override fun init() {
        runGitCommand("init")
    }

    @Throws(IOException::class)
    override fun addAll() {
        runGitCommand("add", "--all")
    }

    @Throws(IOException::class)
    override fun commit(message: String) {
        runGitCommand("commit", "-m", message)
    }

    @Throws(IOException::class)
    override fun detach() {
        runGitCommand("checkout", "--detach")
    }

    @Throws(IOException::class)
    override fun tag(tagName: String) {
        runGitCommand("tag", tagName)
    }

    /**
     * Runs a Git command.
     *
     * @param args The command arguments.
     * @return The output of the command.
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun runGitCommand(vararg args: String): String {
        val cmd = listOf("git") + args.asList()
        return runCommand(
            workingDirectory = directory,
            cmd,
        )
    }

    companion object {

        /**
         * Runs a command.
         *
         * @param workingDirectory The working directory to use.
         * @param args The arguments to the command.
         * @param environment The environment variables to use.
         * @return The standard output of the command.
         * @throws CommandException If the command fails or returns a non-zero exit code.
         * @throws IOException If an I/O error occurs.
         */
        @Throws(CommandException::class, IOException::class, InterruptedException::class)
        private fun runCommand(
            workingDirectory: File,
            args: List<String>,
            environment: Map<String, String> = emptyMap(),
        ): String {
            require(args.isNotEmpty()) { "No command arguments provided." }

            val processBuilder = ProcessBuilder().command(*args.toTypedArray())
            processBuilder.environment().putAll(environment)
            processBuilder.directory(workingDirectory)

            // THROWS: IOException, SecurityException, UnsupportedOperationException
            val process = processBuilder.start()
            // NOTE: We don't close streams that we didn't open.
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            // THROWS: InterruptedException
            val exitCode = process.waitFor()
            if (exitCode != 0) throw CommandException(args.joinToString(" "), exitCode, stderr)
            return stdout.trim()
        }
    }

}
