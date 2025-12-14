package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreatePackageRequest(
    @SerializedName("nombre")
    val nombre: String,

    // Usamos 'descripcion' según el POSTMAN, que el backend lo interpreta como itinerario
    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("destino")
    val destino: String,

    @SerializedName("precio")
    val precio: Int,

    @SerializedName("duracionDias")
    val duracionDias: Int
)