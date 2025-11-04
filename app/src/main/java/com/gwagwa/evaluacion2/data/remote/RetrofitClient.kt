package com.gwagwa.evaluacion2.data.remote

import android.content.Context
import com.gwagwa.evaluacion2.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⚠️ CAMBIA ESTA URL POR LA DE TU API <-- ya la cambié :D
    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/"

    /**
     * Inicializa Retrofit con el contexto de la app
     * Llamar desde Application o ViewModel al inicio
     */
    fun create(context: Context): Retrofit {

        //  SessionManager
        val sessionManager = SessionManager(context)

        // AuthInterceptor
        val authInterceptor = AuthInterceptor(sessionManager)

        // HttpLoggingInterceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient (muestra cositas en el logcat :O)
        val okHttpClient = OkHttpClient.Builder()
            // ⚠️ ORDEN IMPORTANTE: AuthInterceptor primero, luego Logging <-- ok! no lo toco entonces :>
            // Logging primero (para ver la request modificada por AuthInterceptor)
            .addInterceptor(loggingInterceptor)
            // Inyección del token
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        // Retrofit con el cliente configurado
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}