package mb.gitonium

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

/** The Gitonium plugin. */
@Suppress("unused")
class GitoniumPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create and add extension
        val extension = GitoniumExtension(project, project.objects)
        project.extensions.add("gitonium", extension)

        // Configure the version
        // The value will be computed and cached when LazyGitoniumVersion.toString() is called for the first time.
        project.version = LazyGitoniumVersion(extension, false)
        project.subprojects.forEach { subproject ->
            subproject.version = LazyGitoniumVersion(extension, true)
        }

        // Register tasks
        registerCheckSnapshotDependenciesTask(project, extension)
        registerPrintVersionTask(project)
        registerAssertNotDirtyTask(project)
        registerWriteBuildPropertiesTask(project, extension)
        project.subprojects.forEach { subproject ->
            registerCheckSnapshotDependenciesTask(subproject, extension)
            registerPrintVersionTask(subproject)
            registerAssertNotDirtyTask(subproject)
            registerWriteBuildPropertiesTask(subproject, extension)
        }
    }

    /**
     * Registers a task that checks whether any of the dependencies are snapshot dependencies when publishing.
     *
     * @param project The project for which to register the task.
     * @param extension The Gitonium extension, used for the configuration.
     */
    private fun registerCheckSnapshotDependenciesTask(project: Project, extension: GitoniumExtension) {
        val checkTask = project.tasks.register<CheckSnapshotDependencies>("checkSnapshotDependencies", extension)
        project.tasks.named("checkSnapshotDependencies") {
            group = "Verification"
            description = "Asserts that there are no snapshot dependencies used in $project."
        }
        project.pluginManager.withPlugin("maven-publish") {
            project.tasks.named("publish") {
                dependsOn(checkTask)
            }
        }
        project.gradle.taskGraph.whenReady {
            project.tasks.withType<CheckSnapshotDependencies>().configureEach {
                onlyIf { extension.checkSnapshotDependenciesInRelease.get() }
            }
        }
    }

    /**
     * Registers a task that prints the current version of the project to STDOUT.
     *
     * @param project The project for which to register the task.
     */
    private fun registerPrintVersionTask(project: Project) {
        project.tasks.register("printVersion") {
            group = "Help"
            description = "Prints the current version of $project."

            doLast {
                println(project.version)
            }
        }
    }

    /**
     * Registers a task that asserts that the current version of the project is not 'dirty',
     * i.e., has changes not in the current version tag.
     *
     * @param project The project for which to register the task.
     */
    private fun registerAssertNotDirtyTask(project: Project) {
        val assertNotDirty = project.tasks.register("assertNotDirty") {
            group = "Verification"
            description = "Asserts that there are no uncommitted changes in $project."

            doLast {
                if (project.version.toString().endsWith("+dirty")) {
                    throw GradleException("Cannot publish a dirty version: ${project.version}")
                }
            }
        }
        project.pluginManager.withPlugin("maven-publish") {
            project.tasks.named("publish") {
                dependsOn(assertNotDirty)
            }
        }
    }

    /**
     * Registers a task that writes the build info to a properties file when building.
     *
     * @param project The project for which to register the task.
     * @param extension The Gitonium extension, used for the configuration.
     */
    private fun registerWriteBuildPropertiesTask(project: Project, extension: GitoniumExtension) {
        val writeBuildProperties = project.tasks.register("writeBuildProperties") {
            group = "Build"
            description = "Writes the build properties to a file."
            onlyIf { extension.buildPropertiesFile.isPresent }

            doLast {
                val buildDateTimeStr = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )
                val propertiesFile = extension.buildPropertiesFile.get().asFile
                propertiesFile.parentFile.mkdirs()
                propertiesFile.writer().use { w ->
                    val p = Properties()
                    with(extension.versionInfo) {
                        versionString?.let { p["version"] = it }
                        releaseVersionString?.let { p["release-version"] = it }
                        commit?.let { p["commit"] = it }
                        p["build-time"] = buildDateTimeStr
                    }
                    p.store(w, "Build info (generated by Gitonium)")
                }
            }
        }
        project.pluginManager.withPlugin("java") {
            project.tasks.named("classes").configure {
                dependsOn(writeBuildProperties)
            }
        }
    }
}

