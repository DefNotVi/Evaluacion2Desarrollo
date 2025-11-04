package com.gwagwa.evaluacion2.data.remote

import com.gwagwa.evaluacion2.data.local.SessionManager
import kotlinx.coroutines.flow.first
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
        // 1. Obtener el token de manera síncrona
        val token = runBlocking {
            sessionManager.getAuthToken()
        }

        val originalRequest = chain.request()

        // 2. Modificar la petición SOLAMENTE si hay un token
        val requestToProceed = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            // Si no hay token, usar la petición original
            originalRequest
        }

        // 3. Continuar con la petición (autenticada o la original)
        return chain.proceed(requestToProceed)
    }
}