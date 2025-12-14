package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("documentoIdentidad")
    val documentoIdentidad: String,

    @SerializedName("preferencias")
    val preferencias: List<String>
)