package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO = Data Transfer Object
 * Este objeto representa los datos que VIAJAN entre tu app y el servidor
 */
data class UserDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

)