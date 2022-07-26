package com.example.submission.model

import com.google.gson.annotations.SerializedName

data class UserModel(
    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("token")
    val token: String
)
