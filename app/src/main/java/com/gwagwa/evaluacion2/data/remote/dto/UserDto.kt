package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO = Data Transfer Object
 * Este objeto representa los datos que VIAJAN entre tu app y el servidor
 */
data class UserDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("created_at")
    val createdAt: Long?,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("token")
    val token: String? = null

)