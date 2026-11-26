package `in`.hridayan.ashell.core.common.logging

object NoOpAppLogger : AppLogger {
    override fun d(tag: String, message: String) = Unit

    override fun w(tag: String, message: String, throwable: Throwable?) = Unit

    override fun e(tag: String, message: String, throwable: Throwable?) = Unit
}
