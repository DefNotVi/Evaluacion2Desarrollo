package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta del endpoint cliente-profile/me
 * que devuelve los detalles del perfil SIN el wrapper {success, data}
 * y SIN los campos de autenticación (email, role).
 */
data class ProfileDetailsDto(
    @SerializedName("user")
    val userId: String, // ID del usuario al que pertenece este perfil

    @SerializedName("_id")
    val profileId: String, // ID de este documento de perfil

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

    // (por si lo necesito luego)
    // val email: String? = null
)