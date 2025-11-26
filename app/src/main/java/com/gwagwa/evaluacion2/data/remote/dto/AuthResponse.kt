package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta de login y register :D
 * Datos que RECIBIMOS del servidor tras login o register exitoso
 */
data class AuthResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: AuthDataDto?
)

data class AuthDataDto(
    @SerializedName("user")
    val user: UserDto,

    @SerializedName("access_token")
    val accessToken: String,
)