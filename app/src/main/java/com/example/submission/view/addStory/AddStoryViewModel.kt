package com.example.submission.view.addStory

import androidx.lifecycle.*
import com.example.submission.model.UserModel
import com.example.submission.model.UserPreference
import com.example.submission.network.ApiConfig
import com.example.submission.network.FileUploadResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _status = MutableLiveData<Int?>()
    val status: LiveData<Int?> = _status

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun setLoadingToTrue(){
        _isLoading.value = true
    }

    fun logoutUser() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun uploadStory(description: RequestBody, image: MultipartBody.Part, lat: Float?, lon: Float?, token: String){
        _status.value = null

        val service = if (lat != null && lon != null){

            ApiConfig.getApiService().uploadStoryWithLocation(
                description = description,
                file = image,
                lat = lat,
                lon = lon,
                token = token)

        }else{
            ApiConfig.getApiService().uploadStory(description = description, file = image, token = token)
        }

        service.enqueue(object : Callback<FileUploadResponse> {
            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error && responseBody.message == "Story created successfully") {
                        _status.value = 200
                    }else{
                        _status.value = 0
                    }
                } else {
                    _status.value = 0
                }
                _isLoading.value = false
            }

            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                _isLoading.value = false
                _status.value = 0
            }

        })
    }

}