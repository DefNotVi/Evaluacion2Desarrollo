package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.ApiService
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.LoginRequest
import com.gwagwa.evaluacion2.data.remote.dto.RegisterRequest
import com.gwagwa.evaluacion2.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

// Estado de la UI para Login/Registro
data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isRegistrationSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios y Servicios
    private val apiService = RetrofitClient.create(application.applicationContext).create(ApiService::class.java)
    private val sessionManager = SessionManager(application.applicationContext)
    private val authRepository = AuthRepository(apiService, sessionManager)

    init{
        Timber.d("Aplicación iniciada")
    }



    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, error = null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        // Validación de formulario
        if (_uiState.value.username.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(error = "Usuario y contraseña requeridos.") }
            return
        }

        Timber.i("Usuario autenticado: %s", _uiState.value.username)



        _uiState.update { it.copy(isLoading = true, error = null) }


        viewModelScope.launch {
            val request = LoginRequest(
                email = _uiState.value.username.trim(), // DummyJSON usa "username", aquí lo dejo en email porque el Xano usa el email
                password = _uiState.value.password.trim(),
            )


            val result = authRepository.login(request)

            Timber.d("Response: $result.body")

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = exception.localizedMessage ?: "Error de autenticación."
                    ) }
                }
            )
        }
    }

 /** esto mejor lo comento en caso de necesitarlo en algun otro momento

    init {
        // Ejecutar una vez para asegurar que no hay token residual
        viewModelScope.launch {
            sessionManager.clearAuthToken()
        }
    }
*/
    /**
     * Simular el registro, ya que hubieron problemas con el codigo
     */
    fun register() {
        if (_uiState.value.name.isBlank() || _uiState.value.password.isBlank() || _uiState.value.email.isBlank()) {
            _uiState.update { it.copy(error = "Usuario y contraseña requeridos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val request = RegisterRequest(
                email = _uiState.value.email.trim(),
                password = _uiState.value.password.trim(),
                name = _uiState.value.name.trim()
            )

            val result = authRepository.register(request)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isRegistrationSuccess = true) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = exception.localizedMessage ?: "Error de Registro, porfavor inserte los datos adecuadamente."
                    ) }
                }
            )
        }

      /**
       * comento eto por si me sirve mas adelante
       * // Se asume que el registro fue exitoso y lleva a Login/Dashboard.
        _uiState.update { it.copy(isRegistrationSuccess = true) }*/
    }

    fun resetState() {
        _uiState.update { AuthUiState() }
    }
}
