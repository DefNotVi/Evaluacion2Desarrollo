package com.gwagwa.evaluacion2.viewmodel
import kotlinx.coroutines.flow.first
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
import retrofit2.HttpException
import java.io.IOException

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

class LoginRegisterViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios y Servicios


    private val apiService = RetrofitClient.authApiService
    private val sessionManager = SessionManager(application.applicationContext)
    private val authRepository = AuthRepository( apiService,sessionManager)

    /**
     * Reinicia el estado de la UI a así no se loopea al cerrar la sesión
     */
    fun resetState() {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                isLoginSuccess = false,
                isRegistrationSuccess = false,
                error = null
            )
        }
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

    /**
     * Comprueba si el usuario ya ha iniciado sesión.
     * Esta función es útil para la navegación inicial.
     */
    fun checkIfUserIsLoggedIn() {

        viewModelScope.launch {
            // Declara una variable 'token'
            val token = sessionManager.authToken.first()

            sessionManager.authToken.first()
                if (!token.isNullOrBlank()) {
                    // Si hay un token, consideramos el login como exitoso para redirigir al usuario
                    _uiState.update { it.copy(isLoginSuccess = true) }
                }

        }
    }

    fun login() {
        // Validación de formulario
        if (_uiState.value.username.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(error = "Email y contraseña requeridos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }


        viewModelScope.launch {

            try {


                val request = LoginRequest(
                    email = _uiState.value.username.trim(), // DummyJSON usa "username", aquí lo dejo en email porque el Xano usa el email
                    password = _uiState.value.password.trim(),
                )


                authRepository.login(request)




                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }
            } catch (e: Exception) {
                // Errores de credenciales
                val mensajeError = when (e) {
                    // Captura errores HTTP (401, 403, 404, 500, etc (esta linea es MUY BUENA, descubrimiento del año definitivamente))
                    is HttpException -> {
                        "Usuario o contraseña incorrectos. intentalo de nuevo"
                    }
                    // Captura errores de red (sin internet, servidor caído, cosas así idk)
                    is IOException -> {
                        "No se pudo conectar al servidor. Revisa tu conexión a internet"
                    }
                    // Para cualquier otro tipo de error inesperado
                    else -> {
                        "Ocurrió un error inesperado. Inténtalo de nuevo más tarde"
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = mensajeError
                    )
                }
            }

        }
    }


    fun register() {
        if (_uiState.value.name.isBlank() || _uiState.value.password.isBlank() || _uiState.value.email.isBlank()) {
            _uiState.update { it.copy(error = "Usuario y contraseña requeridos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {

            try {


                val request = RegisterRequest(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password.trim(),
                    role = "CLIENTE",
                    name = _uiState.value.name.trim()
                )

                // Le cambié el codigo, es como lo mismo y mas cortito

                authRepository.register(request)

                    // El registro fue REALMENTE exitoso
                    _uiState.update { it.copy(
                        isLoading = false,
                        isRegistrationSuccess = true)
                    }


            } catch (e: Exception) {
                val mensajeError = when (e) {
                    is HttpException -> {
                        // Intenta obtener un mensaje más detallado del servidor
                        val errorBody = e.response()?.errorBody()?.string()
                        if (errorBody != null && errorBody.contains("email_1 unique constraint")) { // Ejemplo si usas Prisma/NestJS
                            "Este correo electrónico ya está registrado."
                        } else {
                            // Si no, usa el mensaje genérico
                            "Error del servidor (${e.code()}). Revisa que los datos cumplan los requisitos."
                        }
                    }
                    is IOException -> {
                        "No se pudo conectar al servidor. Revisa tu conexión a internet."
                    }
                    else -> {
                        "Ocurrió un error inesperado: ${e.message}"
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = mensajeError
                    )
                }
            }

            /**
             * comento eto por si me sirve mas adelante
             * // Se asume que el registro fue exitoso y lleva a Login/Dashboard.
            _uiState.update { it.copy(isRegistrationSuccess = true) }*/
        }

    }
}
