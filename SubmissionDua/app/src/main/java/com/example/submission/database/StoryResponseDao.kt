package com.example.submission.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.submission.network.StoryResponse

@Dao
interface StoryResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryResponses(storyResponses: List<StoryResponse>)

    @Query("SELECT * FROM storyresponse ORDER BY createdAt DESC")
    fun getStoryResponses(): PagingSource<Int, StoryResponse>

    @Query("SELECT * FROM storyresponse ORDER BY createdAt DESC")
    fun getWidgetStoryResponses(): List<StoryResponse>

    @Query("DELETE FROM storyresponse")
    suspend fun deleteAll()

}