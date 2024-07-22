package mb.gitonium

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/** A task that checks for SNAPSHOT dependencies when publishing a release. */
open class CheckSnapshotDependencies @Inject constructor(
    private val extension: GitoniumExtension,
) : DefaultTask() {

    @get:Input
    val isRelease get() = extension.isRelease

    @get:Input
    val snapshotDependencyIds get(): List<String> {
        return project.configurations.flatMap { configuration ->
            configuration.allDependencies.mapNotNull { dependency ->
                val version = dependency.version // Assign to local val to enable smart cast.
                if (version != null && version.endsWith("-SNAPSHOT")) {
                    "$project/$configuration: ${dependency.group}:${dependency.name}:${dependency.version}"
                } else {
                    null
                }
            }
        }.distinct()
    }

    @TaskAction
    fun check() {
        if (!isRelease) return
        val snapshotDependencies = snapshotDependencyIds
        if (snapshotDependencies.isEmpty()) return
        val msg = buildString {
            append("Project '")
            append(project.path)
            append("' will be published as a release under version '")
            append(project.version)
            append("', but has the following SNAPSHOT dependencies: ")
            snapshotDependencies.forEach {
                append("\n- ")
                append(it)
            }
        }
        throw GradleException(msg)
    }
}
