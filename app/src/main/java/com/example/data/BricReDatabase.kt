package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ResearchDocument::class, DocumentChunk::class, ResearchSession::class],
    version = 1,
    exportSchema = false
)
abstract class BricReDatabase : RoomDatabase() {
    abstract fun bricReDao(): BricReDao

    companion object {
        @Volatile
        private var INSTANCE: BricReDatabase? = null

        fun getDatabase(context: Context): BricReDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BricReDatabase::class.java,
                    "bricre_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
