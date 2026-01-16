package com.guardian.launcher.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Type converters for Room Database.
 */
class Converters {
    
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }
    
    @TypeConverter
    fun fromDayOfWeekSet(value: Set<DayOfWeek>?): String? {
        return value?.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toDayOfWeekSet(value: String?): Set<DayOfWeek>? {
        return value?.split(",")
            ?.mapNotNull { 
                try {
                    DayOfWeek.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
            ?.toSet()
    }
}
