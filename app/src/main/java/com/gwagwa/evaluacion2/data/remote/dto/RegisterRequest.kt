package com.gwagwa.evaluacion2.data.remote.dto

data class RegisterRequest(
    val username: String,
    val password: String,
    val name: String? = null // Opcional si Xano lo requiere
)