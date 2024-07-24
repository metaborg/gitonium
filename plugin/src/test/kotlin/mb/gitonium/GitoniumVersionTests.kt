package mb.gitonium

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.assertions.assertSoftly
import org.gradle.api.Project
import org.metaborg.git.GitRepo
import org.metaborg.git.GitTestUtils.commitFile
import org.metaborg.git.GitTestUtils.copyTestGitConfig
import org.metaborg.git.GitTestUtils.createEmptyRepository
import org.metaborg.git.NativeGit
import java.io.File

/** Tests the [GitoniumVersion] class. */
class GitoniumVersionTests: FunSpec({

    val gitConfigPath: File = copyTestGitConfig()

    fun buildGitRepo(directory: File): GitRepo {
        return NativeGit().open(directory,
            // Override the git configuration (Git >= 2.32.0)
            globalConfig = gitConfigPath,
        )
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
