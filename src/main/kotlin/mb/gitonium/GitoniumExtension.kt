package mb.gitonium

import org.gradle.api.Project

/** Extension for configuring the Gitonium plugin. */
@Suppress("unused")
open class GitoniumExtension(private val project: Project) {

    /** The prefix to use to match release tags. */
    var tagPrefix: String = "release-"

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

    /** Whether to set the version on the root project. */
    var setVersion: Boolean = true
    /** Whether to set the version on the subprojects. */
    var setSubprojectVersions: Boolean = true
    /** Whether to check for SNAPSHOT dependencies when publishing a release. */
    var checkSnapshotDependenciesInRelease: Boolean = true

    /** The version info, determined lazily. */
    val versionInfo: GitoniumVersion by lazy {
        GitoniumVersion.determineVersion(
            project.rootDir,
            tagPrefix,
            dirtySuffix,
            snapshotMajorIncrease,
            snapshotMinorIncrease,
            snapshotPatchIncrease,
            snapshotSuffix,
            snapshotIncludeBranch,
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
