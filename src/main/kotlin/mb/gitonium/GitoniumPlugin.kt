package mb.gitonium

import org.eclipse.jgit.lib.internal.WorkQueue
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

@Suppress("unused")
class GitoniumPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create and add extension.
        val extension = GitoniumExtension(project)
        project.extensions.add("gitonium", extension)
        // Set project and sub-project versions.
        project.version = LazyGitoniumVersion(extension, false)
        project.subprojects.forEach {
            it.version = LazyGitoniumVersion(extension, true)
        }
        // Register "check for snapshot dependencies" task when publishing for project and sub-projects.
        project.afterEvaluate {
            registerCheckSnapshotDependenciesTask(this, extension)
            subprojects.forEach {
                registerCheckSnapshotDependenciesTask(it, extension)
            }
        }
        // Close repository after build is finished to free resources.
        project.gradle.buildFinished {
            extension.repo?.close()
            WorkQueue.getExecutor().shutdown() // Shutdown JGit work queue.
        }
    }

    private fun registerCheckSnapshotDependenciesTask(project: Project, extension: GitoniumExtension) {
        if (!extension.checkSnapshotDependenciesInRelease) return
        val publishTask = project.tasks.findByName("publish") ?: return
        val checkTask = project.tasks.register<CheckSnapshotDependencies>("checkSnapshotDependencies", extension)
        publishTask.dependsOn(checkTask)
    }
}

