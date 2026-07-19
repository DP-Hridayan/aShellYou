package `in`.hridayan.ashell.logcat.domain.model

enum class LogLevel(val tag: Char) {
    VERBOSE('V'),
    DEBUG('D'),
    INFO('I'),
    WARNING('W'),
    ERROR('E'),
    FATAL('F'),
    SILENT('S'),
    UNKNOWN('?')
}
