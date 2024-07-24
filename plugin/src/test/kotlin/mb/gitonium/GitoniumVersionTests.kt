package mb.gitonium

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import mb.gitonium.git.GitTestUtils.commitAll
import mb.gitonium.git.GitTestUtils.commitFile
import mb.gitonium.git.GitTestUtils.copyTestGitConfig
import mb.gitonium.git.GitTestUtils.createEmptyRepository
import mb.gitonium.git.NativeGitRepo
import org.gradle.api.Project
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
        test("should return `unspecified`, when the directory is not a Git repository") {
            // Arrange
            val repoDir = tempdir()

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repoDir,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe null
                versionInfo.versionString shouldBe Project.DEFAULT_VERSION
                versionInfo.branch shouldBe null
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }

        test("should return `unspecified`, when the directory is an empty Git repository") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe null
                versionInfo.versionString shouldBe Project.DEFAULT_VERSION
                versionInfo.branch shouldBe "main"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }

        test("should return the version of the release tag, when the HEAD points to a release tag") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.12.123")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 12, 123)
                versionInfo.versionString shouldBe "1.12.123"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe true
                versionInfo.isSnapshot shouldBe false
            }
        }

        test("should return a dirty release version, when the HEAD points to a release tag and there are changes") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.12.123")
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 12, 123, listOf(), listOf("dirty"))
                versionInfo.versionString shouldBe "1.12.123+dirty"
                versionInfo.isDirty shouldBe true
                versionInfo.isRelease shouldBe true
                versionInfo.isSnapshot shouldBe false
            }
        }

        test("should return the snapshot version ahead of the last release tag, when the HEAD points to a commit after a release tag") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.createBranch("develop")
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.12.123")
            repo.commitFile("Second commit", "file2.txt")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 12, 124, listOf("develop-SNAPSHOT"))
                versionInfo.versionString shouldBe "1.12.124-develop-SNAPSHOT"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }

        test("should return the snapshot version ahead of the last release tag, when the HEAD points to a commit after a release tag on the main branch") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.12.123")
            repo.commitFile("Second commit", "file2.txt")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 12, 124, listOf("SNAPSHOT"))
                versionInfo.versionString shouldBe "1.12.124-SNAPSHOT"
                versionInfo.isDirty shouldBe false
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }

        test("should return a dirty snapshot version, when the HEAD points to a commit after a release tag and there are changes") {
            // Arrange
            val repo = createEmptyRepository(::buildGitRepo)
            repo.commitFile("Initial commit", "file1.txt")
            repo.tag("release-1.12.123")
            repo.commitFile("Second commit", "file2.txt")
            File(repo.directory, "uncommitted.txt").writeText("uncommitted")

            // Act
            val versionInfo = GitoniumVersion.determineVersion(
                repoDirectory = repo.directory,
            )

            // Assert
            assertSoftly {
                versionInfo.version shouldBe SemanticVersion(1, 12, 124, listOf("SNAPSHOT"), listOf("dirty"))
                versionInfo.versionString shouldBe "1.12.124-SNAPSHOT+dirty"
                versionInfo.isDirty shouldBe true
                versionInfo.isRelease shouldBe false
                versionInfo.isSnapshot shouldBe true
            }
        }
    }

})
