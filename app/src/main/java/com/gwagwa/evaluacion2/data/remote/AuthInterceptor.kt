package com.gwagwa.evaluacion2.data.remote

import com.gwagwa.evaluacion2.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor: Añade automáticamente el token JWT a las peticiones
 *
 * ¿Cuándo se ejecuta?
 * - ANTES de cada petición HTTP
 *
 * ¿Qué hace?
 * 1. Recupera el token del SessionManager
 * 2. Si existe, añade el header: Authorization: Bearer {token}
 * 3. Si no existe, deja la petición sin modificar
 */

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Recuperar el token (usando runBlocking porque intercept no es suspend)
        val token = runBlocking {
            sessionManager.getAuthToken()
        }

        // Si hay un token, añade el Header
        val requestWithAuth = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token") // ⬅️ Formato estándar JWT
                .build()
        } else {
            originalRequest
        }


        // Si no hay token, continuar con la petición original
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }



        // Crear nueva petición CON el token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        // Continuar con la petición autenticada
        return chain.proceed(requestWithAuth)


    }
}