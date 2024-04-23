package mb.gitonium

import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.IOException
import java.util.regex.Pattern

/** Extension for configuring the Gitonium plugin. */
@Suppress("unused")
open class GitoniumExtension(private val project: Project) {

    /** The pattern to use to match release tags. The first group should match the actual version string. */
    var tagPattern: Pattern = Pattern.compile(""".*release-(.+)""")
    /** Whether to set the version on the root project. */
    var setVersion: Boolean = true
    /** Whether to set the version on the subprojects. */
    var setSubprojectVersions: Boolean = true
    /** Whether to check for SNAPSHOT dependencies when publishing a release. */
    var checkSnapshotDependenciesInRelease: Boolean = true

    /** The computed version string. */
    val version: String by lazy {
        val repo = repo // Assign to local val to enable smart cast.
        if (repo == null) {
            project.logger.warn("Gitonium cannot set the version for $project; no Git repository was found. Defaulting to '${Project.DEFAULT_VERSION}")
            return@lazy Project.DEFAULT_VERSION
        }

        val branch = getBranch(repo)
        val releaseTagVersion = releaseTagVersion // Assign to local val to enable smart cast.
        when {
            releaseTagVersion != null -> releaseTagVersion
            branch != null -> "999.9.9-$branch-SNAPSHOT"
            else -> {
                project.logger.info("Gitonium cannot set the version for $project; the repository HEAD does not point to a branch, nor a release tag. Defaulting to '${Project.DEFAULT_VERSION}'")
                Project.DEFAULT_VERSION
            }
        }
    }

    /** Whether the current version is a release version. */
    val isRelease: Boolean get() = releaseTagVersion != null

    /** The Git repository, if any; otherwise `null`. */
    internal val repo: Repository? by lazy {
        try {
            FileRepositoryBuilder().readEnvironment().findGitDir(project.rootDir).setMustExist(true).build()
        } catch (e: RepositoryNotFoundException) {
            project.logger.warn("Gitonium cannot find a Git repository for $project", e)
            null
        }
    }

    /** The release tag version, if any; otherwise `null`. */
    private val releaseTagVersion: String? by lazy {
        val repo = repo ?: return@lazy null
        getReleaseTag(repo)
    }

    /**
     * Gets the HEAD ref of the repository.
     *
     * @param repo The repository.
     * @return The HEAD ref.
     */
    private fun getHead(repo: Repository): Ref {
        try {
            return repo.exactRef(Constants.HEAD) ?: throw GradleException("Gitonium cannot set the version for $project; repository has no HEAD")
        } catch (e: IOException) {
            throw GradleException("Gitonium cannot set the version for $project; exception occurred while resolving repository HEAD", e)
        }
    }

    /**
     * Gets the current branch of the repository.
     *
     * @param repo The repository.
     * @return The current branch, or `null` if the HEAD does not point to a branch.
     */
    private fun getBranch(repo: Repository): String? {
        val headRef = getHead(repo)
        if (!headRef.isSymbolic) return null
        return Repository.shortenRefName(headRef.target.name)
    }

    /**
     * Gets the release tag version for the repository HEAD.
     *
     * @param repo The repository.
     * @return The release tag version, or `null` if no release tag was found.
     */
    private fun getReleaseTag(repo: Repository): String? {
        val head = getHead(repo).objectId

        repo.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { tagRef ->
            val (finalTagRef, target) = if (tagRef.isPeeled) {
                tagRef to (tagRef.peeledObjectId ?: tagRef.objectId)
            } else {
                // Peel the ref if it has not been peeled yet, otherwise peeledObjectId will not be set.
                val peeled = repo.refDatabase.peel(tagRef)
                peeled to (peeled.peeledObjectId ?: tagRef.objectId)
            }

            if (target != null && AnyObjectId.isEqual(head, target)) {
                // Tag names contain 'refs/tags/', which must be removed before matching.
                val name = finalTagRef.name.replace(Constants.R_TAGS, "")
                val matcher = tagPattern.matcher(name)
                if (matcher.matches()) {
                    val tagVersion = matcher.group(1)
                    if (tagVersion != null) {
                        return tagVersion
                    }
                }
            }
        }
        return null
    }
}
