package mb.gitonium

import org.gradle.api.Project

class LazyGitoniumVersion(private val extension: GitoniumExtension, private val isSubProject: Boolean) {
    override fun toString(): String {
        return when {
            extension.setVersion && !isSubProject -> extension.version
            extension.setSubprojectVersions && isSubProject -> extension.version
            else -> Project.DEFAULT_VERSION
        }
    }
}
