package mb.gitonium.git

/** A security manager that intercepts calls to `System.exit`. */
class NoExitSecurityManager(
    /** The wrapped security manager, which may be `null`. */
    wrappedSecurityManager: SecurityManager?,
) : WrappingSecurityManager(wrappedSecurityManager) {

    /** The first recorded exit code; or `null` while none has been recorded. */
    var firstExitCode: Int? = null
        private set

    override fun checkExit(status: Int) {
        if (this.firstExitCode == null) {
            this.firstExitCode = status
        }

        throw CheckExitCalled()
    }
}
