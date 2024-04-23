package mb.gitonium.git

import io.kotest.core.spec.style.FunSpec

/** Tests the [NativeGitRepo] class. These tests assume a local `git` installation is present. */
class NativeGitRepoTests: FunSpec({

    include(GitRepoTests { dir ->
        NativeGitRepo(dir)
    })

})
