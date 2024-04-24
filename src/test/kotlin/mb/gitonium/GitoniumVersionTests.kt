package mb.gitonium

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import mb.gitonium.git.GitTestUtils.commitFile
import mb.gitonium.git.GitTestUtils.copyTestGitConfig
import mb.gitonium.git.GitTestUtils.createEmptyRepository
import mb.gitonium.git.NativeGitRepo
import java.io.File
import java.io.IOException

/** Tests the [GitoniumVersion] class. */
class GitoniumVersionTests: FunSpec({

    val gitConfigPath: File = copyTestGitConfig()

    fun buildGitRepo(directory: File): NativeGitRepo {
        return NativeGitRepo(directory, environment = mapOf(
            // Override the git configuration (Git >= 2.32.0)
            "GIT_CONFIG_GLOBAL" to gitConfigPath.absolutePath,
        ))
    }

    context("determineVersion()") {
        test("should throw, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()

            // Act/Assert
            shouldThrow<IOException> {
                GitoniumVersion.determineVersion(
                    repoDirectory = repoDir,
                )
            }
        }

        test("should return no version, when the directory is an empty Git repository") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe null
                versionInfo.versionString shouldBe null
                versionInfo.branch shouldBe "main"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }

        test("should return the version of the last release tag, when the HEAD points to a release tag") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.0.0")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 0, 0)
                versionInfo.versionString shouldBe "1.0.0"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe true
                versionInfo.isSnapshot shouldBe false
            }
        }

        test("should return a dirty release version, when the HEAD points to a release tag and there are changes") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.0.0")
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 0, 0, listOf(), listOf("dirty"))
                versionInfo.versionString shouldBe "1.0.0+dirty"
                versionInfo.isDirty shouldBe true
                versionInfo.isRelease shouldBe true
                versionInfo.isSnapshot shouldBe false
            }
        }

        test("should return the snapshot version ahead of the last release tag, when the HEAD points to a commit after a release tag") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.0.0")
            repo.commitFile("Second commit", "file2.txt")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 0, 1, listOf("main", "SNAPSHOT"))
                versionInfo.versionString shouldBe "1.0.1-main.SNAPSHOT"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }

        test("should return a dirty snapshot version, when the HEAD points to a commit after a release tag and there are changes") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.0.0")
            repo.commitFile("Second commit", "file2.txt")
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 0, 1, listOf("main", "SNAPSHOT"), listOf("dirty"))
                versionInfo.versionString shouldBe "1.0.1-main.SNAPSHOT+dirty"
                versionInfo.isDirty shouldBe true
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }
    }

})
