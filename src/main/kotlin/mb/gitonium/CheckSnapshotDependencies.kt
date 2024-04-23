package mb.gitonium

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class CheckSnapshotDependencies @Inject constructor(private val extension: GitoniumExtension) : DefaultTask() {
    @get:Input
    val isRelease
        get() = extension.isRelease

    @get:Input
    val snapshotDependencyIds
        get(): List<String> {
            return project.configurations.flatMap { configuration ->
                configuration.allDependencies.mapNotNull { dependency ->
                    val version = dependency.version // Assign to local val to enable smart cast.
                    if (version != null && version.endsWith("-SNAPSHOT")) {
                        "${dependency.group}:${dependency.name}:${dependency.version}"
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
        val sb = StringBuilder()
        sb.append("Project '")
        sb.append(project.path)
        sb.append("' will be published as a release under version '")
        sb.append(extension.version)
        sb.append("', but has the following SNAPSHOT dependencies: ")
        snapshotDependencies.forEach {
            sb.append("\n- ")
            sb.append(it)
        }
        throw GradleException(sb.toString())
    }
}
