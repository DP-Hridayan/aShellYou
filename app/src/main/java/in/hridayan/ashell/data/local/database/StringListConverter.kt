package `in`.hridayan.ashell.data.local.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter

@ProvidedTypeConverter
class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}
