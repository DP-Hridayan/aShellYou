package `in`.hridayan.ashell.core.shizuku.data

import ashell.core.shizuku.IShellProcess

/**
 * Wraps a remote helper process handle into a local [Process].
 */
fun interface RemoteProcessFactory {
    fun create(remote: IShellProcess): Process
}

class ShizukuRemoteProcessFactory : RemoteProcessFactory {
    override fun create(remote: IShellProcess): Process = ShizukuRemoteShellProcess(remote)
}
