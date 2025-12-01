package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName// Esta clase representa la estructura completa de la respuesta de la API
data class PackageResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<PackageDto>, // La lista de paquetes está aquí

    @SerializedName("total")
    val total: Int
)
