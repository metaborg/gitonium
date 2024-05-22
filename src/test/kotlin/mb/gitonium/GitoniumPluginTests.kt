package mb.gitonium

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mb.gitonium.git.GitTestUtils.createEmptyRepository
import mb.gitonium.git.GitTestUtils.writeFile
import mb.gitonium.git.NativeGitRepo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/** Tests the [GitoniumPlugin]. */
class GitoniumPluginTests: FunSpec({

    context("task :tasks") {
        test("should print the Gitonium tasks") {
            // Arrange
            val repo = createEmptyRepository { NativeGitRepo(it) }
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
            val repo = createEmptyRepository { NativeGitRepo(it) }
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
            val versionStr = result.output.substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.3"
        }

        test("should print dirty version when git repo has changed files") {
            // Arrange
            val repo = createEmptyRepository { NativeGitRepo(it) }
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
            val versionStr = result.output.substringAfter("> Task :printVersion\n").substringBefore('\n')
            versionStr shouldBe "1.2.3+dirty"
        }
    }


    context("task :checkSnapshotDependencies") {
        test("should fail if there are snapshot dependencies") {
            // Arrange
            val repo = createEmptyRepository { NativeGitRepo(it) }
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
            val repo = createEmptyRepository { NativeGitRepo(it) }
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
            val repo = createEmptyRepository { NativeGitRepo(it) }
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
            val repo = createEmptyRepository { NativeGitRepo(it) }
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

})
