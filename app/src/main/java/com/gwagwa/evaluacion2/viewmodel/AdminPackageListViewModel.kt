package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import com.gwagwa.evaluacion2.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//  Estado de la UI para el Dashboard de Admin
data class AdminPackageUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null, // Para mostrar "Bienvenido, [nombre]"
    val error: String? = null
)

class AdminPackageListViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.authApiService
    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(apiService, sessionManager)

    private val _uiState = MutableStateFlow(AdminPackageUiState())
    val uiState: StateFlow<AdminPackageUiState> = _uiState.asStateFlow()

    init {
        // Al iniciar, carga los datos del perfil del administrador
        loadAdminProfile()
    }

    // Función para obtener los datos del admin
    fun loadAdminProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Reutiliza la función getProfile
                val profile = authRepository.getProfile()
                _uiState.update {
                    it.copy(isLoading = false, user = profile)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar el perfil: ${e.message}")
                }
            }
        }
    }

    // Función para cerrar sesión
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}


