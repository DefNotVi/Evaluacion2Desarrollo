package com.gwagwa.evaluacion2.data.remote.dto;

data class PackageDto(
        val id: Int,
        val name: String,    val description: String,
        val price: Double,
        val category: String, // ej: "Aventura", "Playa", "Cultural"
        val imageUrl: String, // Una URL a una imagen del paquete
        val itinerary: List<ItineraryItemDto>
)

data class ItineraryItemDto(
        val day: Int,
        val title: String,
        val description: String
)

