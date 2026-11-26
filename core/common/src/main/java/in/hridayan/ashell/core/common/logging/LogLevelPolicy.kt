package `in`.hridayan.ashell.core.common.logging

object LogLevelPolicy {
    fun shouldLogDebug(isDebuggable: Boolean): Boolean = isDebuggable
}
