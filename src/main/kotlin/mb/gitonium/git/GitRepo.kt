package mb.gitonium.git

import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Calls Git commands for a specific repository.
 */
interface GitRepo {

    /** The repository working directory. */
    val directory: File

    /**
     * Gets the version of Git that is locally installed.
     *
     * @return The version of Git that is locally installed;
     * or `null` if Git is not installed or the version could not be determined.
     * @throws IOException If an I/O error occurs.
     */
    fun getGitVersion(): String?

    /**
     * Gets the name of the current branch. If the repository is not on a branch (i.e., detached HEAD),
     * this returns an empty string.
     *
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun getCurrentBranch(): String

    /**
     * Gets the current commit hash.
     *
     * @param short Whether to return the short commit hash (7 characters or more) or the full (40 character) commit hash.
     * @return The current commit hash.
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun getCurrentCommitHash(short: Boolean = false): String

    /**
     * Gets whether the repository is clean (i.e., has no uncommitted changes).
     *
     * @return `true` when the repository is clean; `false` when there are uncommitted changes.
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun getIsClean(): Boolean

    /**
     * Gets a human-readable name for this commit based on the last tag and the commit hash.
     *
     * This returns a name of the form `"<tag>[-<numCommits>-g<shortHash>]"`, where `<tag>` is the last matching tag
     * on this branch, `<numCommits>` is the number of commits since the tag, and `<shortHash>`
     * is the short commit hash. If the matching tag points to the current commit, this returns just the tag.
     * This is used as the version number.
     *
     * @param patterns The glob patterns to match tags against; or none to match all tags.
     * @param withHash Whether to include the short commit hash in the name if the tag does not point to the current commit.
     * @param firstParentOnly Whether to only consider the first parent when looking for tags across merge commits.
     * @return The human-readable name of the last tag on this branch, optionally with a short commit hash suffix
     * if the tag does not point to the current commit. If there is no tag, this just returns the short commit hash.
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun getTagDescription(
        vararg patterns: String,
        withHash: Boolean = false,
        firstParentOnly: Boolean = false,
    ): String

    /**
     * Gets the status of the repository.
     *
     * This is equivalent to `git status --porcelain`.
     *
     * @param showUntracked Whether to show untracked files.
     * @return The status of the repository.
     * @throws CommandException If the command fails or returns a non-zero exit code.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun getStatus(showUntracked: Boolean = true): String

    /**
     * Initializes the repository.
     *
     * This is equivalent to `git init`.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun init()

    /**
     * Stages all changes in the repository.
     *
     * This is equivalent to `git add -A`.
     */
    @Throws(IOException::class)
    fun addAll()

    /**
     * Commits all staged changes with the given message.
     *
     * This is equivalent to `git commit -m <message>`.
     *
     * @param message The commit message.
     * @param allowEmpty Whether to allow an empty commit.
     */
    @Throws(IOException::class)
    fun commit(message: String, allowEmpty: Boolean = false)

    /**
     * Detach HEAD at the tip of the current branch.
     *
     * This is equivalent to `git checkout --detach`.
     */
    @Throws(IOException::class)
    fun detach()

    /**
     * Adds a non-annotated (light-weight) tag to the current commit.
     *
     * This is equivalent to `git tag <tagName>`.
     *
     * @param tagName The name of the tag.
     */
    @Throws(IOException::class)
    fun tag(tagName: String)

    /**
     * Creates a new branch with the specified name.
     *
     * This is equivalent to `git switch --create <branchName>`.
     *
     * @param branchName The name of the branch.
     */
    @Throws(IOException::class)
    fun createBranch(branchName: String)

}
