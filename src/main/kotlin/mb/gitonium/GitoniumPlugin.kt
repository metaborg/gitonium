package mb.gitonium

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/** The Gitonium plugin. */
@Suppress("unused")
class GitoniumPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create and add extension
        val extension = GitoniumExtension(project)
        project.extensions.add("gitonium", extension)

        // Set project and subproject versions
        project.version = LazyGitoniumVersion(extension, false)
        project.subprojects.forEach {
            it.version = LazyGitoniumVersion(extension, true)
        }

        // Register tasks
        registerCheckSnapshotDependenciesTask(project, extension)
        registerPrintVersionTask(project)
        registerAssertNotDirtyTask(project)
        project.subprojects.forEach {
            registerCheckSnapshotDependenciesTask(it, extension)
            registerPrintVersionTask(it)
            registerAssertNotDirtyTask(it)
        }
    }

    /**
     * Registers a task that checks whether any of the dependencies are snapshot dependencies when publishing.
     *
     * @param project The project for which to register the task.
     * @param extension The Gitonium extension, used for the configuration.
     */
    private fun registerCheckSnapshotDependenciesTask(project: Project, extension: GitoniumExtension) {
        if (!extension.checkSnapshotDependenciesInRelease) return
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
}

