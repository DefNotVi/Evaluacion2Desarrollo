package com.gwagwa.evaluacion2.repository

import android.content.Context
import android.content.SharedPreferences
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.AuthResponse
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.IOException


class AuthRepositoryTest {

    // Mocks
    private lateinit var mockContext: Context
    private lateinit var mockApiService: ApiService
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var mockSessionManager: SessionManager


    // SUT (System Under Test)
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        // Crear mocks
        mockContext = mockk(relaxed = true)
        mockApiService = mockk()
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        mockSessionManager = mockk(relaxed = true)

        // Configurar comportamiento de SharedPreferences
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockEditor.clear() } returns mockEditor

        // Mockear RetrofitClient para que use nuestro mock
        mockkObject(com.gwagwa.evaluacion2.data.remote.RetrofitClient)
        every { com.gwagwa.evaluacion2.data.remote.RetrofitClient.authApiService } returns mockApiService

        // Crear instancia del repository
        repository = AuthRepository(mockApiService, mockSessionManager)
    }

    @After
    fun teardown() {
        // Limpiar todos los mocks
        unmockkAll()
    }

    @Test
    fun `login exitoso debe retornar Success con User`() = runTest {
        // Given - Preparar datos de prueba
        val email = "test@example.com"
        val password = "password123"

        val userDto = UserDto(
            id = "user123",
            email = email,
            role = "CLIENTE",
            createdAt = "123456789",
            isActive = true,
            avatarUrl = null
        )

        val authData = AuthResponse.AuthDataDto(
            user = userDto,
            accessToken = "mock_token_12345"
        )

        val apiResponse = AuthResponse(
            success = true,
            message = "Login exitoso",
            data = authData
        )

        // Configurar mock para retornar respuesta exitosa
        coEvery {
            mockApiService.login(
                LoginRequest(email, password)
            )
        } returns apiResponse

        // When - Ejecutar login
        val result = repository.login(LoginRequest(email, password))

        // Then - Verificar resultado
        assertTrue(result.success, "El resultado debe ser Success")

        val user = result.data?.user
        assertEquals("user123", user?.id)
        assertEquals(email, user?.email)
        assertEquals("CLIENTE", user?.role)

        // Verificar que se guardó el token
        coEvery { mockSessionManager.saveAuthToken("mock_token_12345") }

    }

    @Test
    fun `login con credenciales inválidas debe retornar Failure`() = runTest {
        // Given
        val email = "wrong@example.com"
        val password = "wrongpassword"

        val apiResponse = AuthResponse(
            success = false,
            message = "Credenciales inválidas",
            data = null
        )

        coEvery {
            mockApiService.login(any())
        } returns apiResponse

        // When
        val result = repository.login(LoginRequest(email, password))

        // Then
        assertFalse(result.success, "El resultado debe ser Failure")
        assertEquals(
            "Credenciales inválidas",
            result.message
        )
    }

    @Test
    fun `login con error de red debe lanzar una excepcion`() = runTest {
        // Given
        val request = LoginRequest("test@example.com", "password")

        // --- CORRECCIÓN CLAVE ---
        // Simulamos que mockApiService.login lanza una excepción directamente
        coEvery { mockApiService.login(request) } throws RuntimeException("Error de Red")

        // When & Then
        // Usamos assertFailsWith para verificar que el código dentro de las llaves {}
        // lanza la excepción que esperamos. El test pasará si lo hace, y fallará si no.
        val exception = assertFailsWith<RuntimeException> {
            repository.login(request)
        }

        // Finalmente, verificamos que el mensaje de la excepción es el correcto.
        assertEquals("Error de Red", exception.message)
    }


    // ==================== REGISTER TESTS ====================
    @Test
    fun `registro exitoso debe guardar el token`() = runTest {
        // Given
        val request = RegisterRequest(
            name = "Nuevo Usuario",
            email = "nuevo@example.com",
            password = "password123",
            role = "CLIENTE"
        )
        val token = "new_token_12345"
        val apiResponse = AuthResponse(true, "Registro exitoso", AuthResponse.AuthDataDto(mockk(), token))

        coEvery { mockApiService.register(request) } returns apiResponse

        // When
        repository.register(request)

        // Then
        // Esto ahora funcionará porque el 'setup' inyecta el mock correcto.
        coVerify(exactly = 1) { mockSessionManager.saveAuthToken(token) }
    }

    @Test
    fun `registro con email duplicado debe lanzar IOException`() = runTest {
        // Given
        val request = RegisterRequest(
            name = "Test User",
            email = "existing@example.com",
            password = "password123",
            role = "CLIENTE"
        )
        val apiResponse = AuthResponse(
            success = false,
            message = "El email ya está registrado",
            data = null
        )
        coEvery { mockApiService.register(request) } returns apiResponse

        // When & Then
        val exception = assertFailsWith<IOException> {
            repository.register(request)
        }
        assertEquals("El email ya está registrado", exception.message)
    }

    @Test
    fun `logout debe llamar a clearAuthToken del SessionManager`() = runTest {
        // Given: No se necesita ninguna preparación especial, el repositorio ya está creado.

        // When: Ejecutamos la función de logout en el repositorio.
        repository.logout()

        // Then: Verificamos que la función 'clearAuthToken' del mockSessionManager
        //       fue llamada exactamente una vez.
        coVerify(exactly = 1) { mockSessionManager.clearAuthToken() }
    }
}