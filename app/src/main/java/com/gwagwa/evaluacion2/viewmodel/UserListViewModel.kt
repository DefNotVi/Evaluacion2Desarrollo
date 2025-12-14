package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import com.gwagwa.evaluacion2.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.text.lowercase

// ESTADO DE LA UI para la lista de usuarios
data class UserListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val users: List<UserDto> = emptyList(), // La lista ahora es de UserDto
    val searchQuery: String = ""
)

class UserListViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.authApiService
    private val authRepository = AuthRepository(apiService, SessionManager(application))

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    // FLUJO PARA FILTRAR USUARIOS
    val filteredUsers: StateFlow<List<UserDto>> =
        _uiState.map { state ->
            if (state.searchQuery.isNotBlank()) {
                val query = state.searchQuery.trim().lowercase()
                state.users.filter { user ->
                    user.email.lowercase().contains(query)
                }
            } else {
                state.users
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Al crear el ViewModel, carga la lista de usuarios
        loadUsers()
    }

    // FUNCIÓN PARA CARGAR LOS USUARIOS DESDE LA API
    fun loadUsers() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val userList = authRepository.getAllUsers()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = userList
                    )
                }
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    // FUNCIÓN PARA ACTUALIZAR LA BÚSQUEDA (igual que antes)
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // FUNCIÓN DE MANEJO DE ERRORES (simplificada para una sola llamada)
    private fun getErrorMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> "Error HTTP (${error.code()}) al cargar usuarios."
            is IOException -> "Error de red. No se pudo obtener la lista de usuarios."
            else -> "Error desconocido: ${error.message}"
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
