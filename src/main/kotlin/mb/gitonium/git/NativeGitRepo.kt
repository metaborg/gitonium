package mb.gitonium.git

import mb.gitonium.runCommand
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
    /** Environment variables to apply. */
    val environment: Map<String, String> = emptyMap(),
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
    override fun getTagDescription(
        vararg patterns: String,
        withHash: Boolean,
        firstParentOnly: Boolean,
        commit: String,
    ): String {
        if (withHash) {
            return runGitCommand(
                "describe",
                // Both annotated and non-annotated tags
                "--tags",
                // Abbreviate the commit hash to 7 or more characters (however many are needed to make it unique)
                "--abbrev=7",
                // Just the abbreviated commit hash if no tag is found
                "--always",
                // Follow only the first parent of merge commits
                *(if (firstParentOnly) listOf("--first-parent") else emptyList()).toTypedArray(),
                // Match the pattern
                *patterns.map { "--match=$it" }.toTypedArray(),
                commit,
            )
        } else {
            try {
                return runGitCommand(
                    "describe",
                    // Both annotated and non-annotated tags
                    "--tags",
                    // Leave out the commit hash
                    "--abbrev=0",
                    // Follow only the first parent of merge commits
                    *(if (firstParentOnly) listOf("--first-parent") else emptyList()).toTypedArray(),
                    // Match the pattern
                    *patterns.map { "--match=$it" }.toTypedArray(),
                    commit,
                )
            } catch (ex: CommandException) {
                if (ex.exitCode == 128 && "No names found, cannot describe anything" in ex.stderr) {
                    // No tags found
                    return ""
                } else {
                    throw ex
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun getStatus(showUntracked: Boolean): String {
        return runGitCommand("status", "--porcelain", "--untracked-files=${if (showUntracked) "normal" else "no"}")
    }

    @Throws(IOException::class)
    override fun init() {
        runGitCommand("init", "--initial-branch=main")
    }

    @Throws(IOException::class)
    override fun addAll() {
        runGitCommand("add", "--all")
    }

    @Throws(IOException::class)
    override fun commit(message: String, allowEmpty: Boolean) {
        runGitCommand("commit",
            "-m", message,
            *(if (allowEmpty) listOf("--allow-empty") else emptyList()).toTypedArray()
        )
    }

    @Throws(IOException::class)
    override fun detach() {
        runGitCommand("checkout", "--detach")
    }

    @Throws(IOException::class)
    override fun tag(tagName: String) {
        runGitCommand("tag", tagName)
    }

    @Throws(IOException::class)
    override fun createBranch(branchName: String) {
        runGitCommand("switch", "--create", branchName)
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
            environment = environment,
        )
    }
}
