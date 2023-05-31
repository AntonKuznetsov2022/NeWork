package ru.netology.nework.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {
    @TypeConverter
    fun fromList(ids: List<Int>): String {
        return Gson().toJson(ids)
    }

    @TypeConverter
    fun toList(data: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {
        }.type
        return Gson().fromJson(data, listType)
    }
}