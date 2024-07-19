package mb.gitonium

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

private val LOG: Logger = LoggerFactory.getLogger(GitoniumExtension::class.java)

/**
 * Configuration for the Gitonium plugin.
 *
 * Create an instance like this:
 *
 * ```kotlin
 * extensions.create<GitoniumExtension>("gitonium")
 * ```
 */
open class GitoniumExtension @Inject constructor(
    /** The project itself. */
    private val project: Project,
    /** The Gradle object factory. */
    objects: ObjectFactory,
){
    /** The prefix to use to match release tags. */
    val tagPrefix: Property<String> = objects.property(String::class.java)
        .convention("release-")
    /** The major increase for snapshot versions. */
    val snapshotMajorIncrease: Property<Int> = objects.property(Int::class.java)
        .convention(0)
    /** The minor increase for snapshot versions. */
    val snapshotMinorIncrease: Property<Int> = objects.property(Int::class.java)
        .convention(0)
    /** The patch increase for snapshot versions. */
    val snapshotPatchIncrease: Property<Int> = objects.property(Int::class.java)
        .convention(1)
    /** The suffix to use for snapshot versions; or an empty string to use no suffix. */
    val snapshotSuffix: Property<String> = objects.property(String::class.java)
        .convention("SNAPSHOT")
    /** The suffix to use for dirty (release or snapshot) versions; or an empty string to use no suffix. */
    val dirtySuffix: Property<String> = objects.property(String::class.java)
        .convention("dirty")
    /** Whether to include the branch name in snapshot versions. */
    val snapshotIncludeBranch: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)
    /** Whether to consider the first parent only when looking for tags across merge commits. */
    val firstParentOnly: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)
    /** Whether to check for SNAPSHOT dependencies when publishing a release. */
    val checkSnapshotDependenciesInRelease: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)
    /** Whether to always create a snapshot version string, even if the HEAD points to a release tag. */
    val alwaysSnapshotVersion: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(false)
    /** The name of the main branch. */
    val mainBranch: Property<String> = objects.property(String::class.java)
        .convention("main")
    /** A properties file to write the build and version info to, or unset to not write. */
    val buildPropertiesFile: RegularFileProperty = objects.fileProperty()
        .convention(null as RegularFile?)
    /** Whether to set the version on the root project. */
    var setVersion: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)
    /** Whether to set the version on the subprojects. */
    var setSubprojectVersions: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)

    /** The version info, determined lazily. */
    val versionInfo: GitoniumVersion by lazy {
        GitoniumVersion.determineVersion(
            project.rootDir,
            tagPrefix.get(),
            dirtySuffix.get(),
            snapshotMajorIncrease.get(),
            snapshotMinorIncrease.get(),
            snapshotPatchIncrease.get(),
            snapshotSuffix.get(),
            snapshotIncludeBranch.get(),
            firstParentOnly.get(),
            alwaysSnapshotVersion.get() || (project.providers.gradleProperty("gitonium.isSnapshot").getOrNull()?.toBoolean() ?: false),
            mainBranch.getOrNull(),
        )
    }

    /**
     * The computed version string.
     *
     * If the repository HEAD points to a release tag, the version is set to the tag version (e.g., `"1.0.0"`).
     * If the repository HEAD points not to a release tag, the version is set to a snapshot version
     * that is higher than the last release version, with the branch name (if different from the main branch)
     * and `-SNAPSHOT` as the suffix (e.g., `"1.0.1-develop-SNAPSHOT"`).
     * If the repository is dirty, the version is suffixed with `+dirty` (e.g., `"1.0.1-SNAPSHOT+dirty"`).
     */
    val version: String by lazy {
        versionInfo.versionString ?: run {
            LOG.warn("Gitonium could not determine version from Git repository, using default version.")
            Project.DEFAULT_VERSION
        }
    }

    /** Whether the current commit has a release version tag. */
    val isRelease: Boolean get() = versionInfo.isRelease

    /** Whether the repository is dirty (i.e., has uncommitted changes). */
    val isDirty: Boolean get() = versionInfo.isDirty
}

