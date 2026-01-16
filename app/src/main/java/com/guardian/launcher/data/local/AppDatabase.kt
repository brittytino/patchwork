package com.guardian.launcher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room Database for Guardian Launcher.
 * 
 * Stores:
 * - Approved apps
 * - Rules (time, app, internet)
 * - Usage statistics
 * - Parent settings
 */
@Database(
    entities = [
        // TODO: Add entity classes here
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    // TODO: Add DAO declarations here
    // abstract fun appRuleDao(): AppRuleDao
    // abstract fun timeRuleDao(): TimeRuleDao
    // abstract fun usageDao(): UsageDao
    
    companion object {
        const val DATABASE_NAME = "guardian_launcher.db"
    }
}
