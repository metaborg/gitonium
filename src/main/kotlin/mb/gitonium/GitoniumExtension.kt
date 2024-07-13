package mb.gitonium

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.internal.enterprise.test.FileProperty
import java.util.regex.Pattern

/** Extension for configuring the Gitonium plugin. */
@Suppress("unused")
open class GitoniumExtension(private val project: Project) {

    /** The prefix to use to match release tags. */
    var tagPrefix: String = "release-"
    /** The pattern to use to match release tags. Deprecated. */
    @Deprecated("Use tagPrefix instead.", ReplaceWith("tagPrefix"))
    var tagPattern: Pattern? = null

    /** The suffix to use for dirty (release or snapshot) versions; or an empty string to use no suffix. */
    var dirtySuffix: String = "dirty"
    /** The major increase for snapshot versions. */
    var snapshotMajorIncrease: Int = 0
        set(value) {
            require(value >= 0) { "Snapshot major increase must be non-negative." }
            field = value
        }
    /** The minor increase for snapshot versions. */
    var snapshotMinorIncrease: Int = 0
        set(value) {
            require(value >= 0) { "Snapshot minor increase must be non-negative." }
            field = value
        }
    /** The patch increase for snapshot versions. */
    var snapshotPatchIncrease: Int = 1
        set(value) {
            require(value >= 0) { "Snapshot patch increase must be non-negative." }
            field = value
        }
    /** The suffix to use for snapshot versions; or an empty string to use no suffix. */
    var snapshotSuffix: String = "-SNAPSHOT"
    /** Whether to include the branch name in snapshot versions. */
    var snapshotIncludeBranch: Boolean = true
    /** Whether to consider the first parent only when looking for tags across merge commits. */
    var firstParentOnly: Boolean = false

    /** Whether to set the version on the root project. */
    var setVersion: Boolean = true
    /** Whether to set the version on the subprojects. */
    var setSubprojectVersions: Boolean = true
    /** Whether to check for SNAPSHOT dependencies when publishing a release. */
    var checkSnapshotDependenciesInRelease: Boolean = true

    /** Whether to always create a snapshot version string, even if the HEAD points to a release tag. */
    var alwaysSnapshotVersion: Boolean = false

    /** A properties file to write the build and version info to, or unset to not write. */
    val buildPropertiesFile: RegularFileProperty = project.objects.fileProperty()

    /** The version info, determined lazily. */
    val versionInfo: GitoniumVersion by lazy {
        @Suppress("DEPRECATION") val prefix = tagPattern?.let {
            // For backwards compatibility, we allow tagPattern to be set if it consists of a prefix and a suffix of `(.+)`.
            val patternStr = it.pattern()
            if (patternStr.endsWith("(.+)")) {
                patternStr.substringBeforeLast("(.+)")
            } else {
                throw IllegalArgumentException("tagPattern is no longer supported, use tagPrefix.")
            }
        } ?: tagPrefix

        GitoniumVersion.determineVersion(
            project.rootDir,
            prefix,
            dirtySuffix,
            snapshotMajorIncrease,
            snapshotMinorIncrease,
            snapshotPatchIncrease,
            snapshotSuffix,
            snapshotIncludeBranch,
            firstParentOnly,
            alwaysSnapshotVersion,
        )
    }

    /**
     * The computed version string.
     *
     * If the repository HEAD points to a release tag, the version is set to the tag version (e.g., `"1.0.0"`).
     * If the repository HEAD points not to a release tag, the version is set to a snapshot version
     * that is higher than the last release version and has the branch name and `.SNAPSHOT` suffix (e.g., `"1.0.1-master-SNAPSHOT"`).
     * If the repository is dirty, the version is suffixed with `+dirty` (e.g., `"1.0.1-master-SNAPSHOT+dirty"`).
     */
    val version: String by lazy {
        val versionString = versionInfo.versionString
        if (versionString == null) {
            project.logger.warn("Gitonium could not determine version from Git repository, using default version.")
            return@lazy Project.DEFAULT_VERSION
        }
        versionString
    }

    /** Whether the current commit has a release version tag. */
    val isRelease: Boolean get() = versionInfo.isRelease

    /** Whether the repository is dirty (i.e., has uncommitted changes). */
    val isDirty: Boolean get() = versionInfo.isDirty
}
