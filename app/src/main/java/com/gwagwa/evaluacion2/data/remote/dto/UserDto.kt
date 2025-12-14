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

    // CAMPOS NUEVOS
    @SerializedName("emailVerified")
    val emailVerified: Boolean,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("profileId")
    val profileId: String?,

    @SerializedName("documentoIdentidad")
    val documentoIdentidad: String? = null,

    @SerializedName("preferencias")
    val preferencias: List<String>? = null,

    // fin de campos nuevos :)

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
)