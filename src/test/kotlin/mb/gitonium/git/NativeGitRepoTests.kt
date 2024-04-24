package mb.gitonium.git

import io.kotest.core.spec.style.FunSpec
import mb.gitonium.git.GitTestUtils.copyTestGitConfig
import java.io.File

/** Tests the [NativeGitRepo] class. These tests assume a local `git` installation is present. */
class NativeGitRepoTests: FunSpec({

    val gitConfigPath: File = copyTestGitConfig()

    include(GitRepoTests { dir ->
        NativeGitRepo(dir, environment = mapOf(
            // Override the git configuration (Git >= 2.32.0)
            "GIT_CONFIG_GLOBAL" to gitConfigPath.absolutePath,
        ))
    })

})
