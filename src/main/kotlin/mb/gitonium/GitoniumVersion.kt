package mb.gitonium

import mb.gitonium.git.CommandException
import mb.gitonium.git.GitRepo
import mb.gitonium.git.NativeGitRepo
import java.io.File
import java.io.IOException

/**
 * A Gitonium version, determined from a Git repository.
 */
data class GitoniumVersion(
    /** The current branch name; or `null` if it could not be determined. */
    val branch: String?,
    /** The version string; or `null` if it could not be determined. */
    val versionString: String?,
    /** The version; or `null` if it could not be determined. */
    val version: SemanticVersion?,
    /** Whether the repository is dirty (i.e., has uncommitted changes). */
    val isDirty: Boolean,
    /** Whether the current commit has a release version tag. */
    val isRelease: Boolean,
) {
    /** Whether the current commit does not have a release version tag. */
    val isSnapshot: Boolean get() = !isRelease

    companion object {
        fun determineVersion(
            /** The repository directory. */
            repoDirectory: File,
            /** The prefix to use to match release tags. */
            tagPrefix: String = "release-",
            /** The suffix to use for dirty versions; or an empty string to use no suffix. */
            dirtySuffix: String = "dirty",
            /** The major increase for snapshot versions. */
            snapshotMajorIncrease: Int = 0,
            /** The minor increase for snapshot versions. */
            snapshotMinorIncrease: Int = 0,
            /** The patch increase for snapshot versions. */
            snapshotPatchIncrease: Int = 1,
            /** The suffix to use for snapshot versions; or an empty string to use no suffix. */
            snapshotSuffix: String = "-SNAPSHOT",
            /** Whether to include the branch name in snapshot versions. */
            snapshotIncludeBranch: Boolean = true,
            /** Whether to check for SNAPSHOT dependencies when publishing a release. */
            firstParentOnly: Boolean = false,
            /** Whether to always create a snapshot version string, even if the HEAD points to a release tag. */
            alwaysSnapshot: Boolean = false,
        ): GitoniumVersion {

            val repo = getGitRepo(repoDirectory) ?: throw IOException("No Git repository found at $repoDirectory.")

            val (tagVersion, tagIsSnapshot) = repo.getCurrentVersion(tagPrefix, firstParentOnly)
            val isSnapshot = tagIsSnapshot || alwaysSnapshot
            val actualTagVersion = if (tagVersion != null && isSnapshot) tagVersion.copy(
                major = tagVersion.major + snapshotMajorIncrease,
                minor = tagVersion.minor + snapshotMinorIncrease,
                patch = tagVersion.patch + snapshotPatchIncrease,
            ) else tagVersion

            // Determine the current branch name
            val branch = if (isSnapshot && snapshotIncludeBranch) repo.getCurrentBranchOrNull() else null
            val snapshotVersionSuffix = if (isSnapshot) { "${branch ?: ""}$snapshotSuffix".ifBlank { null } } else null
            val dirtyVersionSuffix = if (repo.isDirty()) dirtySuffix.ifBlank { null } else null

            val version = actualTagVersion?.let {
                SemanticVersion(
                    major = it.major,
                    minor = it.minor,
                    patch = it.patch,
                    preRelease = it.preRelease + listOfNotNull(snapshotVersionSuffix),
                    build = it.build + listOfNotNull(dirtyVersionSuffix),
                )
            }
            return GitoniumVersion(
                branch = branch,
                versionString = version?.toString(),
                version = version,
                isDirty = repo.isDirty(),
                isRelease = !isSnapshot
            )
        }

        /**
         * Gets the [GitRepo] object for the given repository directory.
         *
         * @param repoDirectory The repository directory.
         * @return The [GitRepo] object for the given repository directory; or `null` if the directory is not a Git repository.
         * @throws IOException If an I/O error occurs, or if Git is not available.
         */
        private fun getGitRepo(repoDirectory: File): GitRepo? {
            val repo = NativeGitRepo(repoDirectory)

            // Ensure Git is available
            try {
                repo.getGitVersion() ?: throw IOException("Git is not available.")
            } catch (e: IOException) {
                throw IOException("Git is not available.", e)
            }

            // Ensure the directory exists and is a Git repository
            try {
                repo.getStatus()
            } catch (e: CommandException) {
                return null
            } catch (e: IOException) {
                return null
            }

            return repo
        }

        /** The release version tag on the current commit, if any; otherwise `null`. */
        private fun GitRepo.getReleaseTagVersion(tagPrefix: String, firstParentOnly: Boolean): String? {
            // If the HEAD points to a commit with a release version tag,
            //  the output of `git describe` _with hash_ will not include a hash
            //  and therefore be equal to the output of `git describe` _without hash).
            val tag = getRecentReleaseTagVersion(tagPrefix, firstParentOnly) ?: return null
            val commitDescription = getTagDescription("$tagPrefix*", withHash = true)
            return tag.takeIf { it == commitDescription }
        }

        /** The most recent release version tag (not necessarily on the current commit), if any; otherwise, `null`. */
        private fun GitRepo.getRecentReleaseTagVersion(tagPrefix: String, firstParentOnly: Boolean): String? {
            return getTagDescription("$tagPrefix*", withHash = false, firstParentOnly = firstParentOnly)
                .takeIf { it.isNotBlank() }
        }

        /** Gets the current (snapshot) version of a repository. */
        private fun GitRepo.getCurrentVersion(tagPrefix: String, firstParentOnly: Boolean): Pair<SemanticVersion?, Boolean> {
            val releaseTagVersionStr = getReleaseTagVersion(tagPrefix, firstParentOnly)
                ?.substringAfter(tagPrefix)
            val isSnapshot: Boolean
            val tagVersion = if (releaseTagVersionStr == null) {
                // The HEAD does not have a release version tag
                isSnapshot = true
                val recentTagVersionStr = getRecentReleaseTagVersion(tagPrefix, firstParentOnly)
                    ?.substringAfter(tagPrefix) ?: return (null to isSnapshot)
                val tagVersion = SemanticVersion.of(recentTagVersionStr) ?: return (null to isSnapshot)
                // Increment the version, such that Gradle accepts this version as _newer_ than the last release version.
                tagVersion
            } else {
                // The HEAD does have a release version tag
                isSnapshot = false
                SemanticVersion.of(releaseTagVersionStr) ?: return (null to isSnapshot)
            }
            return tagVersion to isSnapshot
        }

        private fun GitRepo.getCurrentBranchOrNull(): String? {
            return try {
                getCurrentBranch()
            } catch (ex: IOException) {
                null
            }
        }

        /** Whether the repository is dirty (i.e., has uncommitted changes). */
        private fun GitRepo.isDirty(): Boolean {
            return try {
                !getIsClean()
            } catch (e: IOException) {
                // Let's assume it's clean.
                false
            }
        }
    }
}
