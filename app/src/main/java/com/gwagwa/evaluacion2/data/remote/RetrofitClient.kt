package com.gwagwa.evaluacion2.data.remote

import android.content.Context
import com.gwagwa.evaluacion2.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
 * Objeto singleton que configura y proporciona el cliente Retrofit para la API de Xano.
 *
 * Este object implementa el patrón Singleton usando `object` de Kotlin para garantizar
 * una única instancia compartida de Retrofit y OkHttpClient en toda la aplicación.
 * Todas las propiedades se inicializan de forma lazy (solo cuando se acceden).
 *
 * **Configuración:**
 * - **Base URL:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/`
 * - **Convertidor JSON:** Gson para serialización/deserialización automática
 * - **Timeouts:** 30 segundos para conexión, lectura y escritura
 * - **Logging:** Registra todo el cuerpo de requests/responses en desarrollo
 *
 * **Patrón Lazy:**
 * Las propiedades `okHttpClient`, `retrofit` y `authApiService` usan `by lazy`,
 * lo que significa que se crean solo la primera vez que se acceden y se reutilizan
 * en accesos subsecuentes. Esto optimiza el uso de memoria y rendimiento.
 *
 * Ejemplo de uso en Repository:
 * ```kotlin
 * class UserRepository {
 *     private val apiService = RetrofitClient.authApiService
 *
 *     suspend fun registerUser(request: SignUpRequest): Result<AuthResponse> {
 *         return try {
 *             val response = apiService.signUp(request)
 *             if (response.isSuccessful) {
 *                 Result.success(response.body()!!)
 *             } else {
 *                 Result.failure(Exception("HTTP ${response.code()}"))
 *             }
 *         } catch (e: Exception) {
 *             Result.failure(e)
 *         }
 *     }
 * }
 * ```
 *
 * Ejemplo con Dependency Injection (Hilt):
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     @Provides
 *     @Singleton
 *     fun provideAuthApiService(): AuthApiService {
 *         return RetrofitClient.authApiService
 *     }
 * }
 * ```
 *
 * ⚠️ **Nota de producción:**
 * El logging interceptor registra cuerpos completos de requests/responses (nivel BODY).
 * En producción, cambiar a `Level.NONE` o `Level.BASIC` para evitar logs sensibles.
 *
 *
 * @see com.gwagwa.evaluacion2.repository.UserRepository
 */

object RetrofitClient {

    // ⚠️ CAMBIA ESTA URL POR LA DE TU API <-- ya la cambié :D
    private const val BASE_URL = "https://travelgo-api-hyjz.onrender.com/api/"

    private lateinit var sessionManager: SessionManager

    private lateinit var context: Context
    /**
     * Inicializa Retrofit con el contexto de la app
     * Llamar desde Application o ViewModel al inicio
     */
    fun create(context: Context) {

        this.context = context.applicationContext
        sessionManager = SessionManager(context.applicationContext)
}
    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthInterceptor(sessionManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authApiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /*val packageApiService by lazy {
        retrofit.create()
    }*/

}

