package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Representa la respuesta del endpoint de itinerario.
 * AJUSTA LOS CAMPOS para que coincidan con la respuesta real de tu API.
 */
data class ItineraryDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("package_id")
    val packageId: Int,

    @SerializedName("days")
    val days: List<ItineraryDay> // Asumo que un itinerario tiene una lista de días
)

/**
 * Representa un día específico dentro del itinerario.
 */
data class ItineraryDay(
    @SerializedName("day_number")
    val dayNumber: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("activities")
    val activities: List<String> // Asumo una lista simple de actividades
)
