package com.example.submission.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submission.network.ApiConfig
import com.example.submission.network.ResponseLoginUser
import com.example.submission.model.UserModel
import com.example.submission.model.UserPreference
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _status = MutableLiveData<Int?>()
    val status: LiveData<Int?> = _status

    fun loginUser(email: String, password: String){
        _isLoading.value = true
        _status.value = null
        val client = ApiConfig.getApiService().loginUser(email, password)
        client.enqueue(object : Callback<ResponseLoginUser> {
            override fun onResponse(
                call: Call<ResponseLoginUser>,
                response: Response<ResponseLoginUser>
            ) {
                if (response.isSuccessful) {

                    val responseBody = response.body()

                    if (responseBody != null && !responseBody.error){

                        val userData = UserModel(
                            responseBody.loginResult.userId,
                            responseBody.loginResult.name,
                            responseBody.loginResult.token
                        )

                        viewModelScope.launch {
                            pref.saveUser(userData)
                            _isLoading.value = false
                            _status.value = 200
                        }

                    }

                }else if(response.code() == 401){

                    _isLoading.value = false

                    val jsonObjectError = JSONObject(response.errorBody()!!.string())

                    if (jsonObjectError.get("error") as Boolean){
                        when (jsonObjectError.get("message") as String){
                            "User not found" -> {
                                _status.value = 150
                            }
                            "Invalid password" -> {
                                _status.value = 100
                            }
                            else -> {
                                _status.value = 0
                            }
                        }
                    } else {
                        _status.value = 0
                    }

                }else{

                    _isLoading.value = false
                    _status.value = 0

                }

            }

            override fun onFailure(call: Call<ResponseLoginUser>, t: Throwable) {
                _isLoading.value = false
                _status.value = 0
            }
        })

    }
    
}