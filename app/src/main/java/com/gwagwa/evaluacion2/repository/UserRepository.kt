package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.remote.dto.UsersResponse
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
        .authApiService


    suspend fun fetchProfile(): Result<UserDto> {
        return try {
            val user = apiService.getProfile()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /** suspend fun fetchProfile(): UserDto {
        // Retorna un objeto fijo en lugar de llamar a la API
        return UserDto(
            id = 999,
            email = "mi.correo.unico@test.com",
            username = "Vicente Escobar",
            avatarUrl = null, // o una URL simulada
            firstName = "Carlos",
            lastName = "Torres"
        )
    }*/
    /**
     * Obtiene un usuario de la API
     *
     * Usa Result<T> para manejar éxito/error de forma elegante
     */
    /** su
     * spend fun fetchUser(id: Int = 1): Result<UserDto> {
        return try {
            // Llamar a la API (esto puede tardar varios segundos)
            val user = apiService.getUserById(id)

            // Retornar éxito
            Result.success(user)

        } catch (e: Exception) {
            // Si algo falla (sin internet, timeout, etc.)
            Result.failure(e)
        }
    }*/
}