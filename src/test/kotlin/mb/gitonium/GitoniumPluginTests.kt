package mb.gitonium

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mb.gitonium.git.GitTestUtils.copyTestGitConfig
import mb.gitonium.git.GitTestUtils.createEmptyRepository
import mb.gitonium.git.GitTestUtils.writeFile
import mb.gitonium.git.NativeGitRepo
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/** Tests the [GitoniumPlugin]. */
class GitoniumPluginTests: FunSpec({

    val gitConfigPath: File = copyTestGitConfig()

    fun gitRepoBuilder(dir: File) = NativeGitRepo(dir, environment = mapOf(
        // Override the git configuration (Git >= 2.32.0)
        "GIT_CONFIG_GLOBAL" to gitConfigPath.absolutePath,
    ))

    context("task :tasks") {
        test("should print the Gitonium tasks") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            // Ignore untracked directory: .gradle
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":tasks")
                .withPluginClasspath()
                .build()

            // Assert
            result.output shouldContain "printVersion"
            result.output shouldContain "checkSnapshotDependencies"
            result.output shouldContain "assertNotDirty"
        }
    }

    context("task :printVersion") {
        test("should print clean version when git repo is clean") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            // Ignore untracked directory: .gradle
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":printVersion")
                .withPluginClasspath()
                .build()

            // Assert
            val versionStr = result.output.normaliseLineSeparators()
                .substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.3"
        }

        test("should print snapshot version when git repo is clean but gitonium.isSnapshot is set") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            repo.commit("Initial commit", allowEmpty = true)
            repo.tag("release-1.2.2")
            // Ignore untracked directory: .gradle
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("Gitignore commit")
            repo.tag("release-1.2.4")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":printVersion", "-Pgitonium.isSnapshot=true")
                .withPluginClasspath()
                .build()

            // Assert
            val versionStr = result.output.normaliseLineSeparators()
                .substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.3-SNAPSHOT"
        }

        test("should print dirty version when git repo has changed files") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            repo.writeFile("", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")
            // Changed file: .gitignore
            repo.writeFile(".gradle", ".gitignore")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":printVersion")
                .withPluginClasspath()
                .build()

            // Assert
            val versionStr = result.output.normaliseLineSeparators()
                .substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.3+dirty"
        }

        test("should print snapshot version when git repo has had a commit since the tag") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            repo.writeFile("", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")
            // Changed file: .gitignore
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("New commit")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":printVersion")
                .withPluginClasspath()
                .build()

            // Assert
            val versionStr = result.output.normaliseLineSeparators()
                .substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.4-SNAPSHOT"
        }

        test("should print snapshot and dirty version when git repo has had changes and a commit since the tag") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            repo.writeFile("", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")
            // Changed file: .gitignore
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("New commit")
            // Changed file: .gitignore
            repo.writeFile(".gradle\n.idea", ".gitignore")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":printVersion")
                .withPluginClasspath()
                .build()

            // Assert
            val versionStr = result.output.normaliseLineSeparators()
                .substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.4-SNAPSHOT+dirty"
        }
    }


    context("task :checkSnapshotDependencies") {
        test("should fail if there are snapshot dependencies") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }

                    dependencies {
                        implementation("org.metaborg:org.spoofax.terms:2.6.0-SNAPSHOT")
                    }
                """.trimIndent()
            )
            // Ignore untracked directory: .gradle
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":checkSnapshotDependencies")
                .withPluginClasspath()
                .buildAndFail()

            // Assert
            result.tasks.single { it.path == ":checkSnapshotDependencies" }.outcome shouldBe TaskOutcome.FAILED
        }

        test("should fail to publish if there are snapshot dependencies") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        `maven-publish`
                        id("org.metaborg.gitonium")
                    }

                    dependencies {
                        implementation("org.metaborg:org.spoofax.terms:2.6.0-SNAPSHOT")
                    }
                """.trimIndent()
            )
            // Ignore untracked directory: .gradle
            repo.writeFile(".gradle", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":publish")
                .withPluginClasspath()
                .buildAndFail()

            // Assert
            println(result.output)
            result.tasks.single { it.path == ":checkSnapshotDependencies" }.outcome shouldBe TaskOutcome.FAILED
        }
    }


    context("task :assertNotDirty") {
        test("should fail if the project is dirty") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            repo.writeFile("", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")
            // Changed file: .gitignore
            repo.writeFile(".gradle", ".gitignore")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":assertNotDirty")
                .withPluginClasspath()
                .buildAndFail()

            // Assert
            result.tasks.single { it.path == ":assertNotDirty" }.outcome shouldBe TaskOutcome.FAILED
        }

        test("should fail to publish if the project is dirty") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        `maven-publish`
                        id("org.metaborg.gitonium")
                    }
                """.trimIndent()
            )
            repo.writeFile("", ".gitignore")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")
            // Changed file: .gitignore
            repo.writeFile(".gradle", ".gitignore")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":publish")
                .withPluginClasspath()
                .buildAndFail()

            // Assert
            println(result.output)
            result.tasks.single { it.path == ":assertNotDirty" }.outcome shouldBe TaskOutcome.FAILED
        }
    }


    context("task :writeBuildProperties") {
        test("should write properties file if set") {
            // Arrange
            val repo = createEmptyRepository(::gitRepoBuilder)
            val buildFile = repo.directory.resolve("build.gradle.kts")
            buildFile.writeText(
                """
                    plugins {
                        `java-library`
                        id("org.metaborg.gitonium")
                    }

                    gitonium {
                        buildPropertiesFile.set(layout.buildDirectory.file("resources/main/version.properties"))
                    }
                """.trimIndent()
            )
            repo.writeFile("class Program { static void main(String[] args) { } }", "src/main/java/Program.java")
            repo.addAll()
            repo.commit("Initial commit")
            repo.tag("release-1.2.3")


            // Act
            val result = GradleRunner.create()
                .withProjectDir(repo.directory)
                .withArguments(":build")
                .withPluginClasspath()
                .build()

            // Assert
            result.tasks.single { it.path == ":writeBuildProperties" }.outcome shouldBe TaskOutcome.SUCCESS
        }
    }

})
