package mb.gitonium

import org.gradle.api.Project

/** Uses the [GitoniumExtension] to lazily compute the version string. */
class LazyGitoniumVersion(
    /** The Gitonium extension to use. */
    private val extension: GitoniumExtension,
    /** Whether this is a subproject. */
    private val isSubProject: Boolean,
) {
    override fun toString(): String {
        return when {
            extension.setVersion && !isSubProject -> extension.version
            extension.setSubprojectVersions && isSubProject -> extension.version
            else -> Project.DEFAULT_VERSION
        }
    }
}
