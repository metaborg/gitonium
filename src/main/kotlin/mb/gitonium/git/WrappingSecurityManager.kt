package mb.gitonium.git

import java.io.FileDescriptor
import java.net.InetAddress
import java.security.Permission

/** Wraps a security manager, delegating all calls to the wrapped security manager. */
open class WrappingSecurityManager(
    /** The wrapped security manager. */
    private val wrappedSecurityManager: SecurityManager?,
) : SecurityManager() {

    override fun checkExit(status: Int) {
        wrappedSecurityManager?.checkExit(status)
    }

    override fun checkPermission(perm: Permission) {
        wrappedSecurityManager?.checkPermission(perm)
    }

    override fun checkPermission(perm: Permission, context: Any) {
        wrappedSecurityManager?.checkPermission(perm, context)
    }

    override fun checkCreateClassLoader() {
        wrappedSecurityManager?.checkCreateClassLoader()
    }

    override fun checkAccess(t: Thread) {
        wrappedSecurityManager?.checkAccess(t)
    }

    override fun checkAccess(g: ThreadGroup) {
        wrappedSecurityManager?.checkAccess(g)
    }

    override fun checkExec(cmd: String) {
        wrappedSecurityManager?.checkExec(cmd)
    }

    override fun checkLink(lib: String) {
        wrappedSecurityManager?.checkLink(lib)
    }

    override fun checkRead(fd: FileDescriptor) {
        wrappedSecurityManager?.checkRead(fd)
    }

    override fun checkRead(file: String) {
        wrappedSecurityManager?.checkRead(file)
    }

    override fun checkRead(file: String, context: Any) {
        wrappedSecurityManager?.checkRead(file, context)
    }

    override fun checkWrite(fd: FileDescriptor) {
        wrappedSecurityManager?.checkWrite(fd)
    }

    override fun checkWrite(file: String) {
        wrappedSecurityManager?.checkWrite(file)
    }

    override fun checkDelete(file: String) {
        wrappedSecurityManager?.checkDelete(file)
    }

    override fun checkConnect(host: String, port: Int) {
        wrappedSecurityManager?.checkConnect(host, port)
    }

    override fun checkConnect(host: String, port: Int, context: Any) {
        wrappedSecurityManager?.checkConnect(host, port, context)
    }

    override fun checkListen(port: Int) {
        wrappedSecurityManager?.checkListen(port)
    }

    override fun checkAccept(host: String, port: Int) {
        wrappedSecurityManager?.checkAccept(host, port)
    }

    override fun checkMulticast(maddr: InetAddress) {
        wrappedSecurityManager?.checkMulticast(maddr)
    }

    override fun checkMulticast(maddr: InetAddress, ttl: Byte) {
        wrappedSecurityManager?.checkMulticast(maddr, ttl)
    }

    override fun checkPropertiesAccess() {
        wrappedSecurityManager?.checkPropertiesAccess()
    }

    override fun checkPropertyAccess(key: String) {
        wrappedSecurityManager?.checkPropertyAccess(key)
    }

    override fun checkPrintJobAccess() {
        wrappedSecurityManager?.checkPrintJobAccess()
    }

    override fun checkPackageAccess(pkg: String) {
        wrappedSecurityManager?.checkPackageAccess(pkg)
    }

    override fun checkPackageDefinition(pkg: String) {
        wrappedSecurityManager?.checkPackageDefinition(pkg)
    }

    override fun checkSetFactory() {
        wrappedSecurityManager?.checkSetFactory()
    }

    override fun checkSecurityAccess(target: String) {
        wrappedSecurityManager?.checkSecurityAccess(target)
    }

    override fun getSecurityContext(): Any =
        wrappedSecurityManager?.securityContext ?: super.getSecurityContext()

    override fun getThreadGroup(): ThreadGroup =
        wrappedSecurityManager?.threadGroup ?: super.getThreadGroup()
}
