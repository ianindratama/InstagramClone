package com.example.submission.view.detailStory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submission.model.UserPreference
import kotlinx.coroutines.launch

class DetailStoryViewModel(private val pref: UserPreference) : ViewModel() {

    fun logoutUser() {
        viewModelScope.launch {
            pref.logout()
        }
    }

}