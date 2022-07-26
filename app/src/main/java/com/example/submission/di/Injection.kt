package com.example.submission.di

import android.content.Context
import com.example.submission.data.StoryRepository
import com.example.submission.database.StoryResponseDatabase
import com.example.submission.network.ApiConfig

object Injection {

    fun provideRepository(context: Context): StoryRepository {
        val database = StoryResponseDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }

}