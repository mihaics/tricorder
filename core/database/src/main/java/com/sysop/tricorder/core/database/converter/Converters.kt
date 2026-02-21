package com.sysop.tricorder.core.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
    private val moshi = Moshi.Builder().build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)
    private val stringMapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    private val stringMapAdapter = moshi.adapter<Map<String, String>>(stringMapType)
    private val doubleMapType = Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
    private val doubleMapAdapter = moshi.adapter<Map<String, Double>>(doubleMapType)

    @TypeConverter
    fun fromStringList(value: List<String>): String = stringListAdapter.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = stringListAdapter.fromJson(value) ?: emptyList()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = stringMapAdapter.toJson(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> = stringMapAdapter.fromJson(value) ?: emptyMap()

    @TypeConverter
    fun fromDoubleMap(value: Map<String, Double>): String = doubleMapAdapter.toJson(value)

    @TypeConverter
    fun toDoubleMap(value: String): Map<String, Double> = doubleMapAdapter.fromJson(value) ?: emptyMap()
}
