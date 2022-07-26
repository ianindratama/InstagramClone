package com.example.submission.view.maps

import androidx.lifecycle.*
import com.example.submission.model.UserModel
import com.example.submission.model.UserPreference
import com.example.submission.network.AllStoriesResponse
import com.example.submission.network.ApiConfig
import com.example.submission.network.StoryResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _status = MutableLiveData<Int?>()
    val status: LiveData<Int?> = _status

    private val _listStoryResponse = MutableLiveData<List<StoryResponse>>()
    val listStoryResponse: LiveData<List<StoryResponse>> = _listStoryResponse

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logoutUser() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun getAllStories(token: String){
        _isLoading.value = true
        _status.value = null
        val tokenInserted = "Bearer $token"
        val client = ApiConfig.getApiService().getAllLocationStories(token = tokenInserted)
        client.enqueue(object : Callback<AllStoriesResponse> {
            override fun onResponse(
                call: Call<AllStoriesResponse>,
                response: Response<AllStoriesResponse>
            ) {
                if (response.isSuccessful) {

                    val responseBody = response.body()

                    if (responseBody != null && !responseBody.error){

                        if (responseBody.message == "Stories fetched successfully"){
                            if (responseBody.listStory.isNotEmpty()){

                                _listStoryResponse.value = responseBody.listStory
                                _status.value = 200

                            }else{
                                _status.value = 100
                            }
                        }else{
                            _status.value = 0
                        }
                    }

                    _isLoading.value = false

                }else{

                    _isLoading.value = false
                    _status.value = 0

                }

            }

            override fun onFailure(call: Call<AllStoriesResponse>, t: Throwable) {
                _isLoading.value = false
                _status.value = 0
            }
        })
    }

}