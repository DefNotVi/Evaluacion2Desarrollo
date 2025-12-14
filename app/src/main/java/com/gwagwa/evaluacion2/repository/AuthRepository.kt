package com.gwagwa.evaluacion2.repository

import retrofit2.HttpException
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.AuthResponse
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
import com.gwagwa.evaluacion2.data.remote.dto.UpdateProfileRequest
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import java.io.IOException


class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    /**
     * Lógica de Login: Llama a la API, guarda el token.
     */


 /** SI ME SIRVIÓ XD*/
 suspend fun saveToken(token: String) {
     sessionManager.saveAuthToken(token)
 }

    suspend fun login(request: LoginRequest): AuthResponse {
        // Llama a la API para intentar iniciar sesión
        val response = apiService.login(request)

        // Si la respuesta contiene datos
        // se puede acceder a estos (por ahora solo el token y el rol)
        response.data?.let { data ->
            data.accessToken.let { token ->
                sessionManager.saveAuthToken(token)
            }
            sessionManager.saveUserRole(data.user.role)
        }
        // Devuelve la respuesta completa al ViewModel
        return response
    }




    /**
     * Cierre de Sesión: Elimina el token guardado.
     */
    suspend fun logout() {
        sessionManager.clearAuthToken()
    }

    /**
     * Verifica si hay un token de sesión guardado.
     */
    suspend fun isAuthenticated(): Boolean {
        // Obtiene el token, si no es nulo/vacío, está autenticado
        return !sessionManager.getAuthToken().isNullOrEmpty()
    }

    /**
     * Lógica de Registro: Llama a la API, recibe el token y guarda la sesión.
     */
    suspend fun register(registerRequest: RegisterRequest): AuthResponse {
        val response = apiService.register(registerRequest)
        if (!response.success) {
            // Lanzamos la excepción si el registro no fue exitoso
            // DESPUÉS
            throw IOException(response.message)
        }

        // lo mismo del login xd
        response.data?.let { data ->
            // Guarda el token
            data.accessToken.let { token ->
                sessionManager.saveAuthToken(token)
            }
            // Y guarda el rol del nuevo usuario (que será "CLIENTE")
            sessionManager.saveUserRole(data.user.role)
            sessionManager.saveUserEmail(data.user.email)
            sessionManager.saveUserId(data.user.id)
        }
        return response
    }

    suspend fun getProfile(): UserDto {

        val userEmail = sessionManager.getUserEmail()
        val userRole = sessionManager.getUserRole()
        val userId = sessionManager.getUserId()

        if (userEmail.isNullOrBlank() || userId.isNullOrBlank() || userRole.isNullOrBlank()) {
            throw IOException("Fallo al obtener datos de sesión (Email, Role, o ID) después de la autenticación.")
        }
        try {
            // Obtener datos de Perfil
            val profileDetails = apiService.getProfile()

            // Combinar los datos en un solo UserDto
            return UserDto(
                id = userId, // De SessionManager
                email = userEmail, // De SessionManager
                role = userRole, // De SessionManager
                createdAt = profileDetails.createdAt,
                isActive = profileDetails.isActive,
                emailVerified = true, //
                nombre = profileDetails.nombre,
                telefono = profileDetails.telefono,
                direccion = profileDetails.direccion,
                profileId = profileDetails.profileId,
            )
        } catch (e: Exception) {
            // Si falla la API del perfil (ej. token expirado o error 404)
            throw IOException("Fallo al obtener los detalles del perfil desde la API: ${e.message}")
        }
    }


    /**
     * Actualizar el perfil del cliente
     * Toma el DTO de solicitud y devuelve el UserDto completo actualizado
     */
    suspend fun updateProfile(request: UpdateProfileRequest): UserDto {

        // Obtener datos de la Sesión (para reconstruir el UserDto completo)
        val userEmail = sessionManager.getUserEmail()
        val userRole = sessionManager.getUserRole()
        val userId = sessionManager.getUserId()

        if (userEmail.isNullOrBlank() || userId.isNullOrBlank() || userRole.isNullOrBlank()) {
            throw IOException("Fallo al obtener datos de sesión. Intente cerrar e iniciar sesión nuevamente.")
        }

        try {
            // Llama al endpoint PUT para actualizar los datos
            val updatedProfileDetails = apiService.updateProfile(request)

            // Reconstruir y devolver el UserDto actualizado
            return UserDto(
                id = userId, // De SessionManager
                email = userEmail, // De SessionManager
                role = userRole, // De SessionManager
                createdAt = updatedProfileDetails.createdAt,
                isActive = updatedProfileDetails.isActive,
                emailVerified = true,
                nombre = updatedProfileDetails.nombre,
                telefono = updatedProfileDetails.telefono,
                direccion = updatedProfileDetails.direccion,
                profileId = updatedProfileDetails.profileId,
                avatarUrl = null
            )
        } catch (e: Exception) {
            throw IOException("Fallo al actualizar el perfil: ${e.message}")
        }
    }

    suspend fun getAllUsers(): List<UserDto> {

        val allUsersResponse = apiService.getAllUsers()

        // Verifica si la API reportó éxito
        if (allUsersResponse.success) {
            // Devuelve la lista de usuarios que se encuentra dentro del campo 'data' (o 'userList')
            return allUsersResponse.userList
        } else {
            // Lanza una excepción si "success" es false
            throw Exception("Error al obtener la lista de usuarios desde la API.")
        }
    }

}