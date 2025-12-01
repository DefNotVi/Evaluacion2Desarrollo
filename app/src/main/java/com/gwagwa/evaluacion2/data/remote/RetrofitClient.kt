package com.gwagwa.evaluacion2.data.remote

import android.content.Context
import com.gwagwa.evaluacion2.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://travelgo-api-1.onrender.com/api/"

    // "lateinit var" para indicar que se inicializará después
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    // Propiedad pública para acceder al servicio API
    // Lanzará un error si se intenta usar antes de llamar a "create"
    val authApiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Función "create" que será llamada desde MainActivity
    fun create(context: Context) {
        val sessionManager = SessionManager(context.applicationContext)
        val authInterceptor = AuthInterceptor(sessionManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
