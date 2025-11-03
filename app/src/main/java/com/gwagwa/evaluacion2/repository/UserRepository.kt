package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import android.content.Context

/**
 * Repository: Abstrae la fuente de datos
 * El ViewModel NO sabe si los datos vienen de API, base de datos local, etc.
 */
class UserRepository(context: Context) {

    // Crear la instancia del API Service (pasando el contexto)
    private val apiService: ApiService = RetrofitClient
        .create(context)
        .create(ApiService::class.java)

    /**
     * Obtiene un usuario de la API
     *
     * Usa Result<T> para manejar éxito/error de forma elegante
     */
    suspend fun fetchUser(id: Int = 1): Result<UserDto> {
        return try {
            // Llamar a la API (esto puede tardar varios segundos)
            val user = apiService.getUserById(id)

            // Retornar éxito
            Result.success(user)

        } catch (e: Exception) {
            // Si algo falla (sin internet, timeout, etc.)
            Result.failure(e)
        }
    }
}