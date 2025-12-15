package com.gwagwa.evaluacion2.repository

import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.AuthResponse
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import com.gwagwa.evaluacion2.data.remote.dto.ProfileDetailsDto // Necesitarás este DTO si getProfile lo usa
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.io.IOException

// Nota: Eliminé los mocks de Context y SharedPreferences ya que mockSessionManager es suficiente.

class AuthRepositoryTest {

    // Mocks
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager

    // SUT (System Under Test)
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        // Crear mocks
        mockApiService = mockk()
        // Usa relaxed = true para simplificar coEvery/coVerify (no se como funciona realmente pero lo hace)
        mockSessionManager = mockk(relaxed = true)

        // Crear instancia del repository
        repository = AuthRepository(mockApiService, mockSessionManager)
    }

    @After
    fun teardown() {
        // Limpiar mocks
        unmockkAll()
    }
       
    // -------------------- LOGIN TESTS --------------------
    @Test
    fun `login exitoso debe retornar Success y guardar Token, ID, Email y Role`() = runTest {
        // Given
        val email = "test@example.com"
        val userId = "user123"
        val role = "CLIENTE"
        val token = "mock_token_12345"

        // Mock UserDto del AuthResponse (solo los campos necesarios para el login)
        val userDtoLogin = mockk<UserDto>(relaxed = true) {
            coEvery { id } returns userId
            coEvery { this@mockk.email } returns email
            coEvery { this@mockk.role } returns role
        }

        val authData = AuthResponse.AuthDataDto(
            user = userDtoLogin,
            accessToken = token
        )

        val apiResponse = AuthResponse(
            success = true,
            message = "Login exitoso",
            data = authData
        )

        coEvery { mockApiService.login(any()) } returns apiResponse

        // When
        val result = repository.login(LoginRequest(email, "password123"))

        // Then
        assertTrue(result.success, "El resultado debe ser Success")
        assertEquals(userId, result.data?.user?.id)

        // Verificar que se guardaron los 4 elementos de sesión
        coVerify(exactly = 1) { mockSessionManager.saveAuthToken(token) }
        coVerify(exactly = 1) { mockSessionManager.saveUserRole(role) }
        coVerify(exactly = 1) { mockSessionManager.saveUserEmail(email) }
        coVerify(exactly = 1) { mockSessionManager.saveUserId(userId) }
    }

    @Test
    fun `login con credenciales inválidas debe retornar Failure y no guardar nada`() = runTest {
        // Given
        val apiResponse = AuthResponse(
            success = false,
            message = "Credenciales inválidas",
            data = null
        )

        coEvery { mockApiService.login(any()) } returns apiResponse

        // When
        val result = repository.login(mockk())

        // Then
        assertFalse(result.success, "El resultado debe ser Failure")
        assertEquals("Credenciales inválidas", result.message)
        // Verificar que NADA se guardó
        coVerify(exactly = 0) { mockSessionManager.saveAuthToken(any()) }
        coVerify(exactly = 0) { mockSessionManager.saveUserEmail(any()) }
    }

    // -------------------- REGISTER TESTS --------------------
    @Test
    fun `registro exitoso debe guardar Token, ID, Email y Role`() = runTest {
        // Given
        val request = mockk<RegisterRequest>(relaxed = true)
        val email = "nuevo@example.com"
        val userId = "newuser456"
        val role = "CLIENTE"
        val token = "new_token_12345"

        // Mock UserDto dentro del AuthResponse
        val userDtoRegister = mockk<UserDto>(relaxed = true) {
            coEvery { id } returns userId
            coEvery { this@mockk.email } returns email
            coEvery { this@mockk.role } returns role
        }

        val apiResponse = AuthResponse(
            success = true,
            message = "Registro exitoso",
            data = AuthResponse.AuthDataDto(userDtoRegister, token)
        )

        coEvery { mockApiService.register(request) } returns apiResponse

        // When
        repository.register(request)

        // Then
        // Verificar que se guardaron los 4 elementos de sesión
        coVerify(exactly = 1) { mockSessionManager.saveAuthToken(token) }
        coVerify(exactly = 1) { mockSessionManager.saveUserRole(role) }
        coVerify(exactly = 1) { mockSessionManager.saveUserEmail(email) }
        coVerify(exactly = 1) { mockSessionManager.saveUserId(userId) }
    }


    // -------------------- LOGOUT TESTS --------------------
    @Test
    fun `logout debe llamar a clearAuthToken del SessionManager`() = runTest {
        // Given: No se necesita ninguna preparación especial xd

        // When: Ejecuta la función de logout en el repositorio
        repository.logout()

        // Then: Verifica que la función "clearAuthToken" del mockSessionManager
        //       fue llamada exactamente una vez
        coVerify(exactly = 1) { mockSessionManager.clearAuthToken() }
    }

    // -------------------- GET PROFILE TESTS --------------------

    @Test
    fun `getProfile exitoso debe combinar datos de SessionManager y API`() = runTest {
        // Given
        val userId = "session_id_1"
        val userEmail = "session@test.com"
        val userRole = "CLIENTE"

        //  Configurar datos de la Sesión
        coEvery { mockSessionManager.getUserId() } returns userId
        coEvery { mockSessionManager.getUserEmail() } returns userEmail
        coEvery { mockSessionManager.getUserRole() } returns userRole

        //  Configurar la respuesta de la API (ProfileDetailsDto)
        val apiProfile = mockk<ProfileDetailsDto>(relaxed = true) {
            // Datos que solo vienen de la API
            coEvery { nombre } returns "Usuario Test"
            coEvery { direccion } returns "Calle Falsa 123"
            coEvery { telefono } returns "555-0000"
            coEvery { documentoIdentidad } returns "12345678-9"
            coEvery { preferencias } returns listOf("Montaña", "Ciudad")
        }

        coEvery { mockApiService.getProfile() } returns apiProfile

        // When
        val result = repository.getProfile()

        // Then
        //  Verificar que los datos de SessionManager se usaron
        assertEquals(userId, result.id)
        assertEquals(userEmail, result.email)
        assertEquals(userRole, result.role)

        //  Verificar que los datos de la API se usaron
        assertEquals("Usuario Test", result.nombre)
        assertEquals("Calle Falsa 123", result.direccion)
        assertEquals("555-0000", result.telefono)
        assertEquals("12345678-9", result.documentoIdentidad)
        assertEquals(listOf("Montaña", "Ciudad"), result.preferencias)
        assertTrue(result.emailVerified) // Por la lógica en AuthRepository
    }

    @Test
    fun `getProfile sin datos de sesion debe lanzar IOException`() = runTest {
        // Given: SessionManager devuelve nulo para el ID (simula un fallo de sesión)
        coEvery { mockSessionManager.getUserId() } returns null
        coEvery { mockSessionManager.getUserEmail() } returns "a@b.com" // Los demás pueden ser válidos
        coEvery { mockSessionManager.getUserRole() } returns "CLIENTE"

        // When & Then
        assertFailsWith<IOException>("Debe fallar si falta el ID") {
            repository.getProfile()
        }

        // Verificación extra: La API no debe ser llamada
        coVerify(exactly = 0) { mockApiService.getProfile() }
    }

    @Test
    fun `getProfile con error de API debe propagar IOException`() = runTest {
        // Given: Sesión válida, pero la API falla
        coEvery { mockSessionManager.getUserId() } returns "id"
        coEvery { mockSessionManager.getUserEmail() } returns "email"
        coEvery { mockSessionManager.getUserRole() } returns "role"

        // La API lanza un error (ej: 404/401, que Retrofit se encarga de)
        coEvery { mockApiService.getProfile() } throws RuntimeException("Token expirado")

        // When & Then
        val exception = assertFailsWith<IOException> {
            repository.getProfile()
        }
        // El AuthRepository tira una excepción con su propio mensaje de fallo
        assertTrue(exception.message!!.contains("Fallo al obtener los detalles del perfil desde la API"))
    }
}