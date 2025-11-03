package `in`.hridayan.ashell.core.common.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter

@ProvidedTypeConverter
class StringListConverter {
    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun toStringFromList(list: List<String>): String {
        return list.joinToString(",")
    }
}