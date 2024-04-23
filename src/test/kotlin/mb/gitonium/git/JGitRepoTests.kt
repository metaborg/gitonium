package mb.gitonium.git

import io.kotest.core.spec.style.FunSpec

/** Tests the [JGitRepo] class. */
class JGitRepoTests: FunSpec({

    include(GitRepoTests { dir ->
        JGitRepo(dir)
    })

})
