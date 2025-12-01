package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.PackageDto
import java.io.IOException

class PackageRepository(private val apiService: ApiService) {

    suspend fun getTourPackages(): List<PackageDto> {
        // Llama al método actualizado, que devuelve PackageResponse
        val response = apiService.getAvailableTourPackages()

        // Comprueba si la respuesta fue exitosa y devuelve solo la lista de datos
        if (response.success) {
            return response.data // <-- CAMBIO: Devuelve la lista que está dentro
        } else {
            // Si la API dice que no fue exitoso, lanza un error
            throw IOException("La API indicó un fallo en la respuesta de paquetes.")
        }
    }
}


