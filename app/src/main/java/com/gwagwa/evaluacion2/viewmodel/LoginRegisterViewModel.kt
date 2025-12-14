package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
import com.gwagwa.evaluacion2.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// --- Estado de la UI Simplificado---
data class AuthUiState(
    val email: String = "",
    val name: String = "", // Usado para el campo 'nombre' del registro
    val password: String = "",
    val isLoading: Boolean = false,
    val authSuccess: Boolean = false,
    val error: String? = null,
    val userRole: String? = null
)

class LoginRegisterViewModel(application: Application) : AndroidViewModel(application) {

    // --- Dependencias ---
    private val apiService = RetrofitClient.authApiService
    private val sessionManager = SessionManager(application.applicationContext)
    private val authRepository = AuthRepository(apiService, sessionManager)

    // --- State Management ---
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // --- Eventos desde la UI ---
    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email, error = null) } }
    fun onNameChange(name: String) { _uiState.update { it.copy(name = name, error = null) } }
    fun onPasswordChange(password: String) { _uiState.update { it.copy(password = password, error = null) } }
    fun resetState() { _uiState.value = AuthUiState() }

    /**
     * Comprueba si el usuario ya tiene una sesión activa al iniciar la app.
     */
    fun checkIfUserIsLoggedIn() {
        viewModelScope.launch {
            val token = sessionManager.authToken.first()
            // .first() es una forma segura de obtener el primer valor del Flow
            if (!token.isNullOrBlank()) {
                // Si hay un token, también leemos el rol guardado
                val role = sessionManager.userRole.first()
                _uiState.update { it.copy(authSuccess = true) }
            }
        }
    }

    // --- LÓGICA DE NEGOCIO ---

    fun login() {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(error = "Email y contraseña son requeridos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // CORRECCIÓN: Usamos 'email' del UiState.
                val request = LoginRequest(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password.trim()
                )

                // El repositorio ahora se encarga de guardar el token internamente.
                authRepository.login(request)

                // Lee el rol que se acaba de guardar desde el SessionManager
                val userRole = sessionManager.userRole.first()


                // Si la línea anterior no lanzó una excepción, el login fue exitoso.
                _uiState.update { it.copy(isLoading = false, authSuccess = true, userRole = userRole) }

            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun register() {
        if (_uiState.value.name.isBlank() || _uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(error = "Nombre, email y contraseña son requeridos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    name = _uiState.value.name.trim(), // 'name' se mapea a 'nombre' gracias al DTO
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password.trim(),
                    role = "CLIENTE"
                )

                // El repositorio ya guarda el token automáticamente si el registro es exitoso.
                authRepository.register(request)

                // Lee el rol del nuevo usuario
                val userRole = sessionManager.userRole.first()

                // Si no hay excepción, el registro fue exitoso.
                _uiState.update { it.copy(isLoading = false, authSuccess = true, userRole = userRole) }

            } catch (e: Exception) {
                handleAuthError(e, isRegister = true)
            }
        }
    }

    /**
     * Función centralizada para manejar errores de autenticación de forma más específica.
     */
    private fun handleAuthError(e: Exception, isRegister: Boolean = false) {
        val errorMessage = when (e) {
            is HttpException -> when (e.code()) {
                400 -> if (isRegister) "Datos inválidos. El email debe ser único y la contraseña segura." else "Email o contraseña incorrectos."
                401 -> "No autorizado. Email o contraseña incorrectos."
                409 -> "Este correo electrónico ya está registrado." // 409 Conflict es común para duplicados
                else -> "Error del servidor (${e.code()}). Inténtalo de nuevo."
            }
            is IOException -> "Sin conexión. Revisa tu acceso a internet."
            else -> "Ocurrió un error inesperado."
        }
        _uiState.update { it.copy(isLoading = false, error = errorMessage) }
    }
}
