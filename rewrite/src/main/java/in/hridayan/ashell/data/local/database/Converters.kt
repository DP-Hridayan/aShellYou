package `in`.hridayan.ashell.data.local.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@ProvidedTypeConverter
class Converters {
    companion object {
        private val gson = Gson()
        private val type = object : TypeToken<List<String>>() {}.type
    }

    @TypeConverter
    fun fromList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toList(value: String?): List<String> {
        return value?.let { gson.fromJson(it, type) } ?: emptyList()
    }
}
