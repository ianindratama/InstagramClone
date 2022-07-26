package com.example.submission.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("/v1/register")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseRegisterUser>

    @FormUrlEncoded
    @POST("/v1/login")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseLoginUser>

    @GET("/v1/stories")
    suspend fun getAllStories(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String,
    ): AllStoriesResponse

    @GET("/v1/stories")
    fun getAllLocationStories(
        @Query("location") location: Int = 1,
        @Header("Authorization") token: String
    ): Call<AllStoriesResponse>

    @Multipart
    @POST("/v1/stories")
    fun uploadStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
    ): Call<FileUploadResponse>

    @Multipart
    @POST("/v1/stories")
    fun uploadStoryWithLocation(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("lat") lat: Float,
        @Part("lon") lon: Float,
    ): Call<FileUploadResponse>

}