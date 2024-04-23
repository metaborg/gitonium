package mb.gitonium.git

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.funSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeBlank
import io.kotest.matchers.string.shouldNotBeBlank
import java.io.File

/** Tests implementations of the [GitRepo] interface. */
@Suppress("TestFunctionName")
fun GitRepoTests(gitRepoBuilder: (File) -> GitRepo) = funSpec {

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
     * Initializes an empty repository in a new temporary directory.
     *
     * @return The initialized repository.
     */
    fun createEmptyRepository(): GitRepo {
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
        File(this.directory, actualPath).writeText(contents)
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

    context("init()") {
        test("should initialize a new Git repository in the given directory") {
            // Arrange
            val repoDir = tempdir()
            val repo = gitRepoBuilder(repoDir)

            // Act
            repo.init()

            // Assert
            File(repoDir, ".git").isDirectory shouldBe true
        }
    }

    context("addAll()") {
        test("should stage all changes") {
            // Arrange
            val repo = createEmptyRepository()
            val file1 = repo.writeFile("content1", "a.txt")
            val file2 = repo.writeFile("content2", "b.txt")

            // Act
            repo.addAll()

            // Assert
            val status = repo.getStatus().lineSequence().sorted().joinToString("\n") + "\n"
            status shouldBe """
                |A  $file1
                |A  $file2
                |""".trimMargin()
        }
    }

    context("commit()") {
        test("should commit all staged changes with the given message") {
            // Arrange
            val repo = createEmptyRepository()
            repo.writeFile("content1")
            repo.writeFile("content2")
            repo.addAll()

            // Act
            repo.commit("Committing all changes")

            // Assert
            val status = repo.getStatus()
            status shouldBe ""
        }
    }

    context("detach()") {
        test("should detach HEAD at the tip of the current branch") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            repo.detach()

            // Assert
            val branch = repo.getCurrentBranch()
            branch.shouldBeBlank()
        }
    }

    context("tag()") {
        test("should add a non-annotated (light-weight) tag to the current commit") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            repo.tag("v1.0")

            // Assert
            val tagDescription = repo.getTagDescription()
            tagDescription shouldBe "v1.0"
        }
    }

    context("getGitVersion()") {
        test("returns the version of the locally installed Git") {
            // Arrange
            val repo = gitRepoBuilder(File("."))

            // Act
            val gitVersion = repo.getGitVersion()

            // Assert
            println("Git version: $gitVersion")
            gitVersion shouldNotBe null
        }
    }

    context("getCurrentBranch()") {
        test("should throw an exception, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()
            val repo = gitRepoBuilder(repoDir)

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getCurrentBranch()
            }
            exception.exitCode shouldBe 128
        }

        test("should return a branch, when the repository has no commits") {
            // Arrange
            val repo = createEmptyRepository()

            // Act
            val branchName = repo.getCurrentBranch()

            // Assert
            branchName.shouldNotBeBlank()
        }

        test("should return a branch, when the repository has commits on a branch") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            val branchName = repo.getCurrentBranch()

            // Assert
            branchName.shouldNotBeBlank()
        }

        test("should return nothing, when the repository has a detached head") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.detach()

            // Act
            val branchName = repo.getCurrentBranch()

            // Assert
            branchName.shouldBeBlank()
        }
    }

    context("getCurrentCommitHash()") {
        test("should throw an exception, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()
            val repo = gitRepoBuilder(repoDir)

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getCurrentCommitHash()
            }
            exception.exitCode shouldBe 128
        }

        test("should throw an exception, when the repository has no commits") {
            // Arrange
            val repo = createEmptyRepository()

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getCurrentCommitHash()
            }
            exception.exitCode shouldBe 128
        }

        test("should return a hash, when the repository has commits on a branch") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            val hash = repo.getCurrentCommitHash()

            // Assert
            hash.shouldNotBeBlank()
            hash.length shouldBe 40
        }

        test("should return a hash, when the repository has a detached head") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.detach()

            // Act
            val hash = repo.getCurrentCommitHash()

            // Assert
            hash.shouldNotBeBlank()
            hash.length shouldBe 40
        }

        test("should return a short hash, when the repository has commits on a branch, and short is set") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            val hash = repo.getCurrentCommitHash(short = true)

            // Assert
            hash.shouldNotBeBlank()
            hash.length shouldBeGreaterThanOrEqual 7
        }

        test("should return a short hash, when the repository has a detached head, and short is set") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.detach()

            // Act
            val hash = repo.getCurrentCommitHash(short = true)

            // Assert
            hash.shouldNotBeBlank()
            hash.length shouldBeGreaterThanOrEqual 7
        }
    }

    context("getIsClean()") {
        test("should throw an exception, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()
            val repo = gitRepoBuilder(repoDir)

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getIsClean()
            }
            exception.exitCode shouldBe 128
        }

        test("should return true, when the repository has no commits") {
            // Arrange
            val repo = createEmptyRepository()

            // Act
            val isClean = repo.getIsClean()

            // Assert
            isClean shouldBe true
        }

        test("should return true, when the repository has commits but no uncommitted changes") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            val isClean = repo.getIsClean()

            // Assert
            isClean shouldBe true
        }

        test("should return false, when the repository has commits and also uncommitted changes") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val isClean = repo.getIsClean()

            // Assert
            isClean shouldBe false
        }

        test("should return false, when the repository has no commits but uncommitted changes") {
            // Arrange
            val repo = createEmptyRepository()
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val isClean = repo.getIsClean()

            // Assert
            isClean shouldBe false
        }
    }

    context("getTagDescription()") {
        test("should throw an exception, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()
            val repo = gitRepoBuilder(repoDir)

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getTagDescription()
            }
            exception.exitCode shouldBe 128
        }

        test("should throw an exception, when the repository has no commits") {
            // Arrange
            val repo = createEmptyRepository()

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getTagDescription()
            }
            exception.exitCode shouldBe 128
        }

        test("should return the short commit hash, when the repository has commits but no tags") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            val hash = repo.getCurrentCommitHash(short = true)

            // Act
            val tagDescription = repo.getTagDescription()

            // Assert
            tagDescription shouldBe hash
        }

        test("should return the tag, when the repository has a tag on the current commit") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.tag("v1.0")

            // Act
            val tagDescription = repo.getTagDescription()

            // Assert
            tagDescription shouldBe "v1.0"
        }

        test("should return the matching tag, when the repository has a tag on the current commit and the pattern matches") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.tag("build-20240423T1657")
            repo.tag("v1.0")

            // Act
            val tagDescription = repo.getTagDescription("v*.*")

            // Assert
            tagDescription shouldBe "v1.0"
        }

        test("should return the most recent tag, when the repository has a tag on a current commit and any previous tags") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.tag("v1.0")
            repo.commitFile("content")
            repo.tag("v2.0")

            // Act
            val tagDescription = repo.getTagDescription()

            // Assert
            tagDescription shouldBe "v2.0"
        }

        test("should return the short commit hash, when none of the tags match the pattern") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            val hash = repo.getCurrentCommitHash(short = true)
            repo.tag("build-20240423T1657")

            // Act
            val tagDescription = repo.getTagDescription("v*.*")

            // Assert
            tagDescription shouldBe hash
        }

        test("should return the tag, with commit count and hash, when the repository has a tag on a previous commit") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.tag("v1.0")
            repo.commitFile("content")
            val hash = repo.getCurrentCommitHash(short = true)

            // Act
            val tagDescription = repo.getTagDescription()

            // Assert
            tagDescription shouldBe "v1.0-1-g$hash"
        }

        test("should return the matching tag, with commit count and hash, when the repository has a tag on a previous commit and the pattern matches") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.tag("build-20240423T1657")
            repo.tag("v1.0")
            repo.commitFile("content")
            val hash = repo.getCurrentCommitHash(short = true)

            // Act
            val tagDescription = repo.getTagDescription("v*.*")

            // Assert
            tagDescription shouldBe "v1.0-1-g$hash"
        }

        test("should return the most recent tag, with commit count and hash, when the repository has a tag on a previous commit and multiple tags") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            repo.tag("v1.0")
            repo.commitFile("content")
            repo.tag("v2.0")
            repo.commitFile("content")
            val hash = repo.getCurrentCommitHash(short = true)

            // Act
            val tagDescription = repo.getTagDescription()

            // Assert
            tagDescription shouldBe "v2.0-1-g$hash"
        }
    }

    context("getStatus()") {
        test("should throw an exception, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()
            val repo = gitRepoBuilder(repoDir)

            // Act/Assert
            val exception = shouldThrow<CommandException> {
                repo.getStatus()
            }
            exception.exitCode shouldBe 128
        }

        test("should return nothing, when the repository has no commits") {
            // Arrange
            val repo = createEmptyRepository()

            // Act
            val status = repo.getStatus()

            // Assert
            status shouldBe ""
        }

        test("should return nothing, when the repository has commits but no uncommitted changes") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")

            // Act
            val status = repo.getStatus()

            // Assert
            status shouldBe ""
        }

        test("should return uncommitted changes, when the repository has commits and also uncommitted changes") {
            // Arrange
            val repo = createEmptyRepository()
            repo.commitFile("content")
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val status = repo.getStatus()

            // Assert
            status shouldBe "?? uncommitted.txt"
        }

        test("should return uncommitted changes, when the repository has no commits but uncommitted changes") {
            // Arrange
            val repo = createEmptyRepository()
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val status = repo.getStatus()

            // Assert
            status shouldBe "?? uncommitted.txt"
        }
    }

}
