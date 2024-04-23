package mb.gitonium

import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.IOException
import java.util.regex.Pattern

@Suppress("unused")
open class GitoniumExtension(private val project: Project) {
    var tagPattern: Pattern = Pattern.compile(""".*release-(.+)""")
    var setVersion: Boolean = true
    var setSubprojectVersions: Boolean = true
    var checkSnapshotDependenciesInRelease: Boolean = true


    val version: String by lazy {
        val repo = repo // Assign to local val to enable smart cast.
        if (repo == null) {
            project.logger.warn("Gitonium cannot set the version for $project; no Git repository was found. Defaulting to '${Project.DEFAULT_VERSION}")
            Project.DEFAULT_VERSION
        } else {
            val branch = run {
                val headRef = try {
                    repo.exactRef(Constants.HEAD)
                        ?: throw GradleException("Gitonium cannot set the version for $project; repository has no HEAD")
                } catch (e: IOException) {
                    throw GradleException(
                        "Gitonium cannot set the version for $project; exception occurred while resolving repository HEAD",
                        e
                    )
                }
                if (headRef.isSymbolic) {
                    Repository.shortenRefName(headRef.target.name)
                } else {
                    null
                }
            }
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
    }

    val isRelease: Boolean get() = releaseTagVersion != null


    internal val repo: Repository? by lazy {
        try {
            FileRepositoryBuilder().readEnvironment().findGitDir(project.rootDir).setMustExist(true).build()
        } catch (e: RepositoryNotFoundException) {
            project.logger.warn("Gitonium cannot find a Git repository for $project", e)
            null
        }
    }

    private val releaseTagVersion: String? by lazy {
        val repo = repo // Assign to local val to enable smart cast.
        if (repo == null) {
            null
        } else {
            val head = try {
                repo.exactRef(Constants.HEAD)?.objectId
                    ?: throw GradleException("Gitonium cannot set project version; repository has no HEAD")
            } catch (e: IOException) {
                throw GradleException(
                    "Gitonium cannot set the version for $project; exception occurred while resolving repository HEAD",
                    e
                )
            }
            releaseTagVersion(repo, head, tagPattern)
        }
    }

    private fun releaseTagVersion(repo: Repository, head: ObjectId, tagPattern: Pattern): String? {
        repo.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { tagRef ->
            val (finalTagRef, target) = if (tagRef.isPeeled) {
                Pair(tagRef, tagRef.peeledObjectId ?: tagRef.objectId)
            } else {
                // Peel the ref if it has not been peeled yet, otherwise peeledObjectId will not be set.
                val peeled = repo.refDatabase.peel(tagRef)
                Pair(peeled, peeled.peeledObjectId ?: tagRef.objectId)
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
