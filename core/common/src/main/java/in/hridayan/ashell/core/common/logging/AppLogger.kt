package `in`.hridayan.ashell.core.common.logging

interface AppLogger {
    fun d(tag: String, message: String)

    fun w(tag: String, message: String, throwable: Throwable? = null)

    fun e(tag: String, message: String, throwable: Throwable? = null)
}
