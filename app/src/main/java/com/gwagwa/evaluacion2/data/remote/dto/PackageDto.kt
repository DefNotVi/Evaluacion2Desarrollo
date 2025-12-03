package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PackageDto(

    @SerializedName("_id")
    val id: String, // Cambiado de Int a String, ya que _id es un ObjectId

    @SerializedName("nombre")
    val name: String,

    @SerializedName("descripcion")
    val description: String,

    @SerializedName("imagen")
    val imageUrl: String? = null,

    @SerializedName("precio")
    val price: Double,

    // Usa el campo "destino" como "category" para los filtros
    @SerializedName("destino")
    val category: String,
)