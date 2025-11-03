package com.gwagwa.evaluacion2.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.gwagwa.evaluacion2.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

import com.gwagwa.evaluacion2.repository.AvatarRepository // Importación necesaria

/**
 * Estado de la UI
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val error: String? = null,
    val avatarUri: Uri? = null
)

/**
 * ViewModel: Maneja la lógica de UI y el estado
 * Usa AndroidViewModel para tener acceso al Application Context
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio de la API
    private val userRepository = UserRepository(application.applicationContext)

    // Repositorio de DataStore para el avatar
    private val avatarRepository = AvatarRepository(application.applicationContext)

    // Estado PRIVADO (solo el ViewModel lo modifica)
    private val _uiState = MutableStateFlow(ProfileUiState())

    // Estado PÚBLICO (la UI lo observa)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        // Inicia la observación del URI del avatar guardado en DataStore
        // Este Flow actualiza automáticamente el estado cada vez que se guarda un nuevo URI.
        viewModelScope.launch {
            avatarRepository.getAvatarUri().collect { uri ->
                _uiState.update { it.copy(avatarUri = uri) }
            }
        }
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
     * Carga los datos del usuario desde la API
     */
    fun loadUser(id: Int = 1) {
        // Indicar que está cargando
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        // Ejecutar en coroutine (no bloquea la UI)
        viewModelScope.launch {
            val result = userRepository.fetchUser(id)

            // Actualizar el estado según el resultado
            // Nota: Aquí estás usando _uiState.value = result.fold(...) lo cual es correcto.
            _uiState.value = result.fold(
                onSuccess = { user ->
                    // ✅ Éxito: mostrar datos
                    _uiState.value.copy(
                        isLoading = false,
                        userName = user.username,
                        userEmail = user.email ?: "Sin email",
                        error = null
                    )
                },
                onFailure = { exception ->
                    // ❌ Error: mostrar mensaje
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.localizedMessage ?: "Error desconocido"
                    )
                }
            )
        }
    }
}