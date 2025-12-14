package com.gwagwa.evaluacion2.data.remote

import com.gwagwa.evaluacion2.data.remote.dto.*
import retrofit2.http.*
import retrofit2.Response

/**
 * Define los endpoints de tu API
 * Usando DummyJSON como ejemplo de API REST con autenticación JWT
 */
interface ApiService {

    /**
     * 🔐 LOGIN - Autenticar usuario
     * POST /user/login
     *
     * Ejemplo de uso:
     * val response = apiService.login(LoginRequest("emilys", "emilyspass"))
     * sessionManager.saveAuthToken(response.accessToken)
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register") // Ruta de Xano
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    // Asumimos que la respuesta de registro devuelve un LoginResponse o similar (con token).

    /**
     * 👤 OBTENER USUARIO ACTUAL (requiere autenticación)
     * GET /user/me
     *
     * ⚠️ IMPORTANTE: Este endpoint REQUIERE el token JWT
     * El AuthInterceptor lo añade automáticamente
     *
     * Ejemplo de uso:
     * val currentUser = apiService.getCurrentUser()
     */
    @GET("cliente-profile/me")
    suspend fun getProfile(): ProfileDetailsDto

    @GET("paquete-turistico/disponibles")
    suspend fun getAvailableTourPackages(): PackageResponse

    // ✅ CORRECTO: "paquete-turistico" (sin /api)
    @GET("paquete-turistico")
    suspend fun getAllTourPackages(): List<PackageDto> // Para el endpoint que lista TODOS los paquetes

    // ✅ CORRECTO: Endpoint para el detalle del paquete (NUEVO)
    @GET("paquete-turistico/{id}")
    suspend fun getTourPackageById(@Path("id") id: Int): PackageDto

    @GET("auth/users")
    suspend fun getAllUsers(): AllUsersResponse

    @PUT("cliente-profile/me")
    // Al actualizar, espera la nueva versión del perfil (ProfileDetailsDto) como respuesta
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileDetailsDto

    //  Endpoint para crear un nuevo paquete (POST)
    @POST("paquete-turistico")

    suspend fun createPackage(@Body request: CreatePackageRequest): PackageDto



}