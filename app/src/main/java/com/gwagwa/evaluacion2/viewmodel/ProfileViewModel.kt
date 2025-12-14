package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.repository.AuthRepository
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
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val saveSuccess: Boolean = false,
    val name: String = "",
    val userEmail: String = "",
    val error: String? = null,
    val token: String? = null,
    val avatarUri: Uri? = null,
    val telefono: String = "No disponible",
    val direccion: String = "No disponible",
    val documentoIdentidad: String = "",
    val preferencias: List<String> = emptyList()
)

/**
 * ViewModel: Maneja la lógica de UI y el estado
 * Usa AndroidViewModel para tener acceso al Application Context
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio de la API

    // Repositorio de DataStore para el avatar
    private val avatarRepository = AvatarRepository(application.applicationContext)

    // Estado PRIVADO (solo el ViewModel lo modifica)
    private val _uiState = MutableStateFlow(ProfileUiState())

    private val authRepository = AuthRepository(
        RetrofitClient.authApiService,
        SessionManager(application.applicationContext)
    )

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
     * Funciones para la UI de Edición
     */
    fun setEditMode(isEditing: Boolean) {
        _uiState.update { it.copy(isEditMode = isEditing, error = null, saveSuccess = false) }
        // Opcional: Si sales del modo edición, podrías querer recargar los datos
        if (!isEditing) {
            loadUser()
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onTelefonoChange(newTelefono: String) {
        _uiState.update { it.copy(telefono = newTelefono) }
    }

    fun onDireccionChange(newDireccion: String) {
        _uiState.update { it.copy(direccion = newDireccion) }
    }

    fun onDocumentoChange(newDocumento: String) {
        _uiState.update { it.copy(documentoIdentidad = newDocumento) }
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


            try {
                val user = authRepository.getProfile()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userEmail = user.email,
                        // Mapea los campos existentes
                        name = user.nombre,
                        telefono = user.telefono?.takeIf { t -> t.isNotEmpty() }?: "Telefono no disponible",
                        direccion = user.direccion?.takeIf { d -> d.isNotEmpty() }?: "Direccion No disponible",

                        // ✅ Mapea los nuevos campos (asumiendo que UserDto los tiene)
                        documentoIdentidad = user.documentoIdentidad ?: "",
                        preferencias = user.preferencias ?: emptyList(),
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Error al cargar el perfil."
                    )
                }
            }
        }
    }

    // ... (después de loadUser)

    /**
     * Función de guardado: Envía los datos editados a la API.
     */
    fun saveProfile() {
        val state = _uiState.value

        // Validación
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }

        viewModelScope.launch {
            try {
                // Crear el DTO de solicitud (usando el DTO que definimos antes)
                val request = com.gwagwa.evaluacion2.data.remote.dto.UpdateProfileRequest(
                    nombre = state.name.trim(),
                    // Los campos 'No disponible' se deben convertir a cadena vacía o valores nulos antes de enviar
                    telefono = state.telefono.takeIf { it != "No disponible" } ?: "",
                    direccion = state.direccion.takeIf { it != "No disponible" } ?: "",
                    documentoIdentidad = state.documentoIdentidad,
                    preferencias = state.preferencias
                )

                // Llamar al repositorio
                val updatedUser = authRepository.updateProfile(request)

                // Actualizar el estado con los nuevos datos recibidos
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditMode = false,
                        saveSuccess = true,
                        // Refrescar con los datos actualizados
                        name = updatedUser.nombre,
                        telefono = updatedUser.telefono ?: "No disponible",
                        direccion = updatedUser.direccion ?: "No disponible",
                        documentoIdentidad = updatedUser.documentoIdentidad ?: "",
                        preferencias = updatedUser.preferencias ?: emptyList()
                    )
                }

            } catch (e: Exception) {
                // Manejo de Errores
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Fallo al guardar: ${e.message ?: "Error desconocido"}",
                        saveSuccess = false
                    )
                }
            }
        }
    }
}
