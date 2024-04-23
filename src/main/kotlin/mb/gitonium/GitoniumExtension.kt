package mb.gitonium

import mb.gitonium.git.CommandException
import mb.gitonium.git.GitRepo
import mb.gitonium.git.NativeGitRepo
import org.gradle.api.Project
import java.io.IOException

/** Extension for configuring the Gitonium plugin. */
@Suppress("unused")
open class GitoniumExtension(private val project: Project) {

    /** The prefix to use to match release tags. */
    var tagPrefix: String = "release-"
    /** The suffix to use for snapshot versions. */
    var snapshotSuffix: String = "SNAPSHOT"
    /** The suffix to use for dirty versions; or an empty string to use no suffix. */
    var dirtySuffix: String = "dirty"
    /** Whether to include the branch name in snapshot versions. */
    var includeBranchInSnapshots: Boolean = true
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
            snapshotSuffix,
            dirtySuffix,
            includeBranchInSnapshots
        )
    }

    /**
     * The computed version string.
     *
     * If the repository HEAD points to a release tag, the version is set to the tag version (e.g., `"1.0.0"`).
     * If the repository HEAD points not to a release tag, the version is set to a snapshot version
     * that is higher than the last release version and has the branch name and `.SNAPSHOT` suffix (e.g., `"1.0.1-master.SNAPSHOT"`).
     * If the repository is dirty, the version is suffixed with `+dirty` (e.g., `"1.0.1-master.SNAPSHOT+dirty"`).
     */
    val version: String get() = versionInfo.versionString ?: Project.DEFAULT_VERSION

    /** Whether the current commit has a release version tag. */
    val isRelease: Boolean get() = versionInfo.isRelease

    /** Whether the repository is dirty (i.e., has uncommitted changes). */
    val isDirty: Boolean get() = versionInfo.isDirty
}
