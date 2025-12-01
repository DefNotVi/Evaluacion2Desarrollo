package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.remote.ApiService // Asegúrate que la ruta sea correcta
import com.gwagwa.evaluacion2.data.remote.dto.PackageDto

// El error estaba en el tipo de 'apiService'
class PackageRepository(private val apiService: ApiService) {

    suspend fun getTourPackages(): List<PackageDto> {
        // Ahora esto funciona
        return apiService.getTourPackages()
    }
}
