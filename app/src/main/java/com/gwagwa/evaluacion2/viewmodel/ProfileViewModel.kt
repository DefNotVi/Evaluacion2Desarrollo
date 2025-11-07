package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.gwagwa.evaluacion2.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

import com.gwagwa.evaluacion2.repository.AvatarRepository

/**
 * Estado de la UI
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val userEmail: String = "",
    val error: String? = null,
    val token: String? = null,
    val avatarUri: Uri? = null
)

/**
 * ViewModel: Maneja la lógica de UI y el estado
 * Usa AndroidViewModel para tener acceso al Application Context
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio de la API
    // Nota: El UserRepository ya está configurado para devolver datos simulados
    private val userRepository = UserRepository(application.applicationContext)

    // Repositorio de DataStore para el avatar
    private val avatarRepository = AvatarRepository(application.applicationContext)

    // Estado PRIVADO (solo el ViewModel lo modifica)
    private val _uiState = MutableStateFlow(ProfileUiState())

    // Estado PÚBLICO (la UI lo observa)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        // Inicia la observación del URI del avatar guardado en DataStore
        viewModelScope.launch {
            avatarRepository.getAvatarUri().collect { uri ->
                _uiState.update { it.copy(avatarUri = uri) }
            }
        }
        // Cargar los datos del usuario al iniciar el ViewModel
        loadUser()
    }

    /**
     * Actualiza la URI del avatar del usuario y lo guarda en DataStore.
     * El estado de la UI se actualizará automáticamente gracias al Flow.
     */
    fun updateAvatar(uri: Uri?) {
        viewModelScope.launch {
            avatarRepository.saveAvatarUri(uri)
        }
    }


    /**
     * Carga los datos del usuario desde la SIMULACIÓN del repositorio.
     * Ya no usa response<T>, por lo que el manejo es directo con try-catch.
     */
    fun loadUser() {
        // Indicar que está cargando usando .update{}
        _uiState.update {
            it.copy(isLoading = true, error = null)
        }

        // Ejecutar en coroutine (no bloquea la UI)
        viewModelScope.launch {


            val response = userRepository.fetchProfile()

            // Actualizar el estado según el resultado
            // Usar .fold para manejar el resultado y actualizar el estado
            response.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = user.name,
                            userEmail = user.email?: "Sin email",
                            token = user.token,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.localizedMessage ?: "Error desconocido"
                        )
                    }
                }
            )
        }
    }
}
