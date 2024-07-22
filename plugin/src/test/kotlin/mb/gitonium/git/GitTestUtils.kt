package mb.gitonium.git

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import java.io.File

object GitTestUtils {

    /**
     * Generates a random name of the given length.
     *
     * @param length The length of the name.
     * @return The random name.
     */
    fun randomName(length: Int): String {
        require(length > 0) { "Length must be positive" }

        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Copies a test gitconfig to a temporary file, and returns its path.
     */
    fun TestConfiguration.copyTestGitConfig(): File {
        // Copy the git configuration somewhere
        GitTestUtils::class.java.getResourceAsStream("/gitconfig")?.use { input ->
            val gitconfigFile = tempfile(".gitconfig")
            gitconfigFile.outputStream().use { output ->
                input.copyTo(output)
            }
            return gitconfigFile
        } ?: error("Could not copy git configuration.")
    }

    /**
     * Initializes an empty repository in a new temporary directory.
     *
     * @param gitRepoBuilder The function to create a new repository in the given directory.
     * @return The initialized repository.
     */
    fun TestConfiguration.createEmptyRepository(gitRepoBuilder: (File) -> GitRepo): GitRepo {
        val repoDir = tempdir()
        val repo = gitRepoBuilder(repoDir)
        repo.init()
        return repo
    }

    /**
     * Creates/modifies a file.
     *
     * @return The path of the file, relative to the repository root.
     */
    fun GitRepo.writeFile(contents: String, path: String? = null): String {
        val actualPath = path ?: (randomName(8) + ".txt")
        val file = File(this.directory, actualPath)
        file.parentFile.mkdirs()
        file.writeText(contents)
        return actualPath
    }

    /**
     * Creates/modifies a file and commits all changes.
     *
     * @return The path of the file, relative to the repository root.
     */
    fun GitRepo.commitFile(contents: String, path: String? = null): String {
        val actualPath = writeFile(contents, path)
        this.addAll()
        this.commit("Committing $actualPath")
        return actualPath
    }

    /**
     * Creates a (possibly empty) commit of all files, with the given message.
     *
     * @param message The message of the commit.
     * @return The hash of the commit.
     */
    fun GitRepo.commitAll(message: String = "Commit ${randomName(8)}"): String {
        this.addAll()
        this.commit(message, allowEmpty = true)
        return this.getCurrentCommitHash()
    }
}
