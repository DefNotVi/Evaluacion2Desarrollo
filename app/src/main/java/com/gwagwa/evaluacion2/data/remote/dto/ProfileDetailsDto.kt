package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta del endpoint cliente-profile/me
 */
data class ProfileDetailsDto(
    @SerializedName("user")
    val userId: String,

    @SerializedName("_id")
    val profileId: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("preferencias")
    val preferencias: List<String>?,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("__v")
    val __v: Int,

    @SerializedName("documentoIdentidad")
    val documentoIdentidad: String? = null

    // (por si lo necesito luego)
    // val email: String? = null
)