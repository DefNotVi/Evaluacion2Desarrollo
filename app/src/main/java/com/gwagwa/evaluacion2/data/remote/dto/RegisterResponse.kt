package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta de login
 * Datos que RECIBIMOS del servidor tras login exitoso
 */
data class RegisterResponse(

    @SerializedName("id")
    val id: Int,

    @SerializedName("authToken")
    val accessToken: String,
)

