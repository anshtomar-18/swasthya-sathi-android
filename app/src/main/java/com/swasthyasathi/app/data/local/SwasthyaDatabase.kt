package com.swasthyasathi.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SOSQueueEntity::class, AISettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SwasthyaDatabase : RoomDatabase() {

    abstract fun sosQueueDao(): SOSQueueDao
    abstract fun aiSettingsDao(): AISettingsDao

    companion object {
        @Volatile
        private var INSTANCE: SwasthyaDatabase? = null

        fun getDatabase(context: Context): SwasthyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SwasthyaDatabase::class.java,
                    "swasthya_sathi_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
