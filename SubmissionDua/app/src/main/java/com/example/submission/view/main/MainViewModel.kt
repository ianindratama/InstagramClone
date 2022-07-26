package com.example.submission.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.submission.data.StoryRepository
import com.example.submission.model.UserModel
import com.example.submission.model.UserPreference
import com.example.submission.network.StoryResponse
import kotlinx.coroutines.launch

class MainViewModel(private val pref: UserPreference, private val storyRepository: StoryRepository) : ViewModel() {

    lateinit var story: LiveData<PagingData<StoryResponse>>

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logoutUser() {
        viewModelScope.launch {
            pref.logout()
            storyRepository.deleteStory()
        }
    }

    fun getAllStories(token: String){

        val tokenInserted = "Bearer $token"
        story = storyRepository.getStory(tokenInserted).cachedIn(viewModelScope)

    }

}