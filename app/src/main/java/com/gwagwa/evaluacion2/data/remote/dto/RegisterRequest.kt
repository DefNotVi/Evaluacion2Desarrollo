package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName
data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("name")
    val name: String?
)