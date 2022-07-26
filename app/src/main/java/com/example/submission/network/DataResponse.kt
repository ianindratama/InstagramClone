package com.example.submission.network

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.submission.model.UserModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ResponseRegisterUser(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)

data class ResponseLoginUser(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("loginResult")
    val loginResult: UserModel
)

data class AllStoriesResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("listStory")
    val listStory: List<StoryResponse>

)

@Parcelize
@Entity(tableName = "storyresponse")
data class StoryResponse(

    @field:PrimaryKey
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("photoUrl")
    val photoUrl: String,

    @field:SerializedName("createdAt")
    val createdAt: String,

    @field:SerializedName("lat")
    val lat: Float,

    @field:SerializedName("lon")
    val lon: Float

) : Parcelable

data class FileUploadResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)