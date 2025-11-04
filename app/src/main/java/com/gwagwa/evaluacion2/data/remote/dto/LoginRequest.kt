package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la petición de login
 * Datos que ENVIAMOS al servidor
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,
)