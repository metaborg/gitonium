package mb.gitonium

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/** The Gitonium plugin. */
@Suppress("unused")
class GitoniumPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create and add extension.
        val extension = GitoniumExtension(project)
        project.extensions.add("gitonium", extension)

        // Set project and subproject versions.
        project.version = LazyGitoniumVersion(extension, false)
        project.subprojects.forEach {
            it.version = LazyGitoniumVersion(extension, true)
        }

        // Register "check for snapshot dependencies" task when publishing for project and sub-projects.
        project.afterEvaluate {
            registerCheckSnapshotDependenciesTask(this, extension)
            registerPrintVersionTask(this)
            subprojects.forEach {
                registerCheckSnapshotDependenciesTask(it, extension)
                registerPrintVersionTask(it)
            }
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
        val publishTask = project.tasks.findByName("publish") ?: return
        val checkTask = project.tasks.register<CheckSnapshotDependencies>("checkSnapshotDependencies", extension)
        publishTask.dependsOn(checkTask)
    }

    /**
     * Registers a task that prints the current version of the project to STDOUT.
     *
     * @param project The project for which to register the task.
     */
    private fun registerPrintVersionTask(project: Project) {
        project.tasks.register("printVersion") {
            println(project.version)
        }
    }
}

