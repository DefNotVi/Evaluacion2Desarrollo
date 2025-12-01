package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: UserDto
)
        