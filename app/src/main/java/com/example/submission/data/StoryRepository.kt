package com.example.submission.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.example.submission.database.StoryResponseDatabase
import com.example.submission.network.ApiService
import com.example.submission.network.StoryResponse

class StoryRepository(private val storyResponseDatabase: StoryResponseDatabase, private val apiService: ApiService) {

    fun getStory(token: String): LiveData<PagingData<StoryResponse>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyResponseDatabase, apiService, token),
            pagingSourceFactory = {
                storyResponseDatabase.storyResponseDao().getStoryResponses()
            }
        ).liveData
    }

    suspend fun deleteStory(){
        storyResponseDatabase.storyResponseDao().deleteAll()
    }

}