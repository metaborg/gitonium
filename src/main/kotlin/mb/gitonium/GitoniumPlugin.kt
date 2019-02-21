package mb.gitonium

import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.*
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

@Suppress("unused")
open class GitoniumExtension(private val project: Project) {
  var tagPattern: Pattern = Pattern.compile(""".*release-(.+)""")
  var autoSetVersion: Boolean = true
  var autoSetSubprojectVersions: Boolean = true
  var checkSnapshotDependenciesInRelease: Boolean = true


  val version: String by lazy {
    val branch = run {
      val headRef = repo.exactRef(Constants.HEAD)
        ?: throw GradleException("Gitonium cannot set project version; repository has no HEAD")
      if(headRef.isSymbolic) {
        Repository.shortenRefName(headRef.target.name)
      } else {
        null
      }
    }
    val releaseTagVersion = releaseTagVersion // Assign to local val to enable smart cast.
    when {
      releaseTagVersion != null -> releaseTagVersion
      branch != null -> "$branch-SNAPSHOT"
      else -> throw GradleException("Gitonium cannot set project version; repository HEAD is detached (and does not have a release tag)")
    }
  }

  val isRelease: Boolean get() = releaseTagVersion != null


  internal val repo: Repository by lazy {
    try {
      FileRepositoryBuilder().readEnvironment().findGitDir(project.rootDir).setMustExist(true).build()
    } catch(e: RepositoryNotFoundException) {
      throw GradleException("Gitonium cannot set project version; no git repository found at ${project.rootDir}", e)
    }
  }

  private val releaseTagVersion: String? by lazy {
    val head = try {
      repo.resolve(Constants.HEAD)
        ?: throw GradleException("Gitonium cannot set project version; repository has no HEAD")
    } catch(e: IOException) {
      throw GradleException("Gitonium cannot set project version; exception occurred when resolving repository HEAD", e)
    }
    releaseTagVersion(repo, head, tagPattern)
  }

  private fun releaseTagVersion(repo: Repository, head: ObjectId, tagPattern: Pattern): String? {
    repo.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { tagRef ->
      // Peel the ref if it has not been peeled yet, otherwise peeledObjectId will return null.
      val finalTagRef = if(tagRef.isPeeled) {
        tagRef
      } else {
        repo.refDatabase.peel(tagRef)
      }
      val target = finalTagRef.peeledObjectId
      if(target != null && AnyObjectId.equals(head, target)) {
        // Tag names contain 'refs/tags/', which must be removed before matching.
        val name = finalTagRef.name.replace(Constants.R_TAGS, "")
        val matcher = tagPattern.matcher(name)
        if(matcher.matches()) {
          val tagVersion = matcher.group(1)
          if(tagVersion != null) {
            return tagVersion
          }
        }
      }
    }
    return null
  }
}

class LazyGitoniumVersion(private val extension: GitoniumExtension, private val isSubProject: Boolean) {
  override fun toString(): String {
    return when {
      extension.autoSetVersion && !isSubProject -> extension.version
      extension.autoSetSubprojectVersions && isSubProject -> extension.version
      else -> Project.DEFAULT_VERSION
    }
  }
}

@Suppress("unused")
class GitoniumPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // Create and add extension.
    val extension = GitoniumExtension(project)
    project.extensions.add("gitonium", extension)
    // Set project and sub-project versions.
    project.version = LazyGitoniumVersion(extension, false)
    project.subprojects.forEach {
      it.version = LazyGitoniumVersion(extension, true)
    }
    // Register "check for snapshot dependencies" task when publishing for project and sub-projects.
    project.afterEvaluate {
      registerCheckSnapshotDependenciesTask(this, extension)
      subprojects.forEach {
        registerCheckSnapshotDependenciesTask(it, extension)
      }
    }
    // Close repository after build is finished to free resources.
    project.gradle.buildFinished {
      extension.repo.close()
    }
  }

  private fun registerCheckSnapshotDependenciesTask(project: Project, extension: GitoniumExtension) {
    if(!extension.checkSnapshotDependenciesInRelease) return
    val publishTask = project.tasks.findByName("publish") ?: return
    val checkTask = project.tasks.register<CheckSnapshotDependencies>("checkSnapshotDependencies", extension)
    publishTask.dependsOn(checkTask)
  }
}

open class CheckSnapshotDependencies @Inject constructor(private val extension: GitoniumExtension) : DefaultTask() {
  @Input
  fun isRelease(): Boolean {
    return extension.isRelease
  }

  @Input
  fun snapshotDependencyIds(): List<String> {
    return project.configurations.flatMap { configuration ->
      configuration.allDependencies.mapNotNull { dependency ->
        val version = dependency.version // Assign to local val to enable smart cast.
        if(version != null && version.endsWith("-SNAPSHOT")) {
          "${dependency.group}:${dependency.name}:${dependency.version}"
        } else {
          null
        }
      }
    }.distinct()
  }

  @TaskAction
  fun check() {
    if(!isRelease()) return
    val snapshotDependencies = snapshotDependencyIds()
    if(snapshotDependencies.isEmpty()) return
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
