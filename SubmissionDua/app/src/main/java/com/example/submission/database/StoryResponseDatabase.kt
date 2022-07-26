package com.example.submission.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.submission.network.StoryResponse

@Database(
    entities = [StoryResponse::class, RemoteKeys::class],
    version = 2,
    exportSchema = false
)
abstract class StoryResponseDatabase : RoomDatabase() {

    abstract fun storyResponseDao(): StoryResponseDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryResponseDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryResponseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryResponseDatabase::class.java, "StoryResponse.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

}