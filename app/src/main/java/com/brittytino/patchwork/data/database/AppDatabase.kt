package com.brittytino.patchwork.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.brittytino.patchwork.data.database.dao.*
import com.brittytino.patchwork.data.database.entity.*

@Database(
    entities = [
        ActionHistoryEntry::class,
        SystemSnapshot::class,
        AppBehaviorRule::class,
        AppCooldownRule::class,
        AppUsageEvent::class,
        IdleAppRule::class,
        IdleAppActionLog::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun actionHistoryDao(): ActionHistoryDao
    abstract fun systemSnapshotDao(): SystemSnapshotDao
    abstract fun appBehaviorRuleDao(): AppBehaviorRuleDao
    abstract fun appCooldownRuleDao(): AppCooldownRuleDao
    abstract fun appUsageEventDao(): AppUsageEventDao
    abstract fun idleAppRuleDao(): IdleAppRuleDao
    abstract fun idleAppActionLogDao(): IdleAppActionLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "patchwork_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
