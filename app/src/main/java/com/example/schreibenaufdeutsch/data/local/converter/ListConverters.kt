package com.example.schreibenaufdeutsch.data.local.converter

import androidx.room.TypeConverter
import com.example.schreibenaufdeutsch.data.local.entity.TaskVariation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListConverters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        return Json.decodeFromString(data)
    }

    @TypeConverter
    fun fromVariationList(list: List<TaskVariation>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toVariationList(data: String): List<TaskVariation> {
        return Json.decodeFromString(data)
    }
}
