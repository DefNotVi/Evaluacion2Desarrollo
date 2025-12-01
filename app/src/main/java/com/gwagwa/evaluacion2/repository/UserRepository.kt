package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.ProfileResponse
import com.gwagwa.evaluacion2.data.remote.dto.UserDto

/**
 * Repository: Abstrae la fuente de datos.
 * El ViewModel no sabe si los datos vienen de una API, base de datos local, etc.
 */
class UserRepository { // <-- SIN 'context: Context' EN EL CONSTRUCTOR

    // Esta línea ahora funcionará porque RetrofitClient ya fue inicializado por MyApplication
    private val apiService: ApiService = RetrofitClient.authApiService

    /**
     * Obtiene el perfil del usuario desde la API.
     * Usa Result<T> para un manejo de errores robusto.
     */
    suspend fun fetchProfile(): Result<UserDto> {
        return try {
            // ApiService.getProfile() devuelve un ProfileResponse
            val response: ProfileResponse = apiService.getProfile()

            if (response.success) {
                // Si la API dice que fue exitoso, devuelve los datos del usuario
                Result.success(response.data)
            } else {
                // Si la API marca "success" como false, lo trata como un fallo
                Result.failure(Exception("La API indicó un fallo al obtener el perfil."))
            }
        } catch (e: Exception) {
            // Si ocurre cualquier otro error (red, deserialización, etc), lo captura
            Result.failure(e)
        }
    }
}
