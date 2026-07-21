package `in`.hridayan.ashell.qstiles.domain.model

enum class TileErrorType(val code: Int) {
    NONE(0),
    PERMISSION_DENIED(1),
    TIMEOUT(2),
    EXECUTION_FAILED(3),
    UNKNOWN(4);

    companion object {
        fun fromCode(code: Int): TileErrorType =
            entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}
