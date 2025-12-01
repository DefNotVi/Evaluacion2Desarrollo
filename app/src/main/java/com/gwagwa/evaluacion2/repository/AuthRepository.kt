package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.AuthResponse
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
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

        //    Si la respuesta contiene un token, lo guarda en la sesión
        response.data?.accessToken?.let { token ->
            sessionManager.saveAuthToken(token)
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
        response.data?.accessToken?.let { token ->
            sessionManager.saveAuthToken(token)
        }
        return response
    }

    suspend fun getProfile(): UserDto {
        //    Llama a la API
        //    ahora devuelve un "ProfileResponse"
        val response = apiService.getProfile()

        //    Comprueba si la API indicó éxito y devuelve solo el UserDto
        //    que está dentro del campo "data"
        if (response.success) {
            return response.data
        } else {
            // Si la API dice que falló, lanza una excepción
            throw IOException("La API indicó un fallo al obtener el perfil.")
        }
    }



}