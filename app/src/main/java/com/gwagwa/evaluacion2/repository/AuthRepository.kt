package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.LoginResponse
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
import com.gwagwa.evaluacion2.data.remote.dto.RegisterResponse

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


 // Asumimos que esta función está en tu AuthRepository.kt

// Importante: Debes definir qué tipo de respuesta devuelve la API
// Si la API devuelve un objeto con el token, úsalo aquí (ej: LoginResponse)
// Si solo necesitas guardar el token y no devolver nada más, puedes usar Unit.
// Lo ajustaré para que no devuelva nada, ya que el token se guarda internamente.

 suspend fun login(request: LoginRequest): LoginResponse {
     // Eliminé el 'Result<Unit>' del tipo de retorno
     val response = apiService.login(request)

     // Guarda el token de acceso para futuras peticiones.
     sessionManager.saveAuthToken(response.accessToken)

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
        // Obtenemos el token. Si no es nulo/vacío, está autenticado.
        return !sessionManager.getAuthToken().isNullOrEmpty()
    }

    /**
     * Lógica de Registro: Llama a la API, recibe el token y guarda la sesión.
     */
    suspend fun register(request: RegisterRequest): RegisterResponse {
        // Ahora simplemente llama a la función de la API y devuelve su resultado
        // El bloque try-catch lo moví al LoginViewModel
        return apiService.register(request)
    }


}