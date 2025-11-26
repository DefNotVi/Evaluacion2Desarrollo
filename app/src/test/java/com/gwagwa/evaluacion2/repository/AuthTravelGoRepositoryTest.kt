package com.gwagwa.evaluacion2.repository

import android.content.Context
import android.content.SharedPreferences
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.dto.AuthResponse
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import io.mockk.Runs
import io.mockk.coEvery
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue


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
    fun `login con error de red debe retornar Failure con mensaje de error`() = runTest {
        // Given
        coEvery {
            mockApiService.login(any())
        } throws Exception("Network error")

        // When
        val result = repository.login(LoginRequest("test@example.com", "password")

        // Then
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("Error de red") == true
        )
    }
}