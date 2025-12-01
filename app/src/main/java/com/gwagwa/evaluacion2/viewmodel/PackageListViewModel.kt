// Archivo: C:/Users/Sora/AndroidStudioProjects/Evaluacion2Desarrollo/app/src/main/java/com/gwagwa/evaluacion2/viewmodel/PackageListViewModel.kt

package com.gwagwa.evaluacion2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.PackageDto
import com.gwagwa.evaluacion2.data.remote.dto.UserDto
import com.gwagwa.evaluacion2.repository.AuthRepository
import com.gwagwa.evaluacion2.repository.PackageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// 1. ESTADO DE LA UI: Define todo lo que la pantalla puede necesitar.
// Es una única fuente de verdad para tu Composable.
data class PackageListUiState(
    // Estado general
    val isLoading: Boolean = true, // Inicia en `true` para mostrar un spinner al entrar.
    val error: String? = null,

    // Datos
    val profile: UserDto? = null,
    val packages: List<PackageDto> = emptyList(),

    // Estado de los filtros
    val searchQuery: String = "",
    val selectedCategory: String? = null
)

class PackageListViewModel(application: Application) : AndroidViewModel(application) {

    // 2. INICIALIZACIÓN: Preparamos las dependencias.
    // Usamos `private val` para que solo el ViewModel pueda acceder a ellas.
    private val apiService = RetrofitClient.authApiService
    private val packageRepository = PackageRepository(apiService)
    private val authRepository = AuthRepository(apiService, SessionManager(application))

    // 3. STATEFLOW: El corazón del ViewModel.
    // `_uiState` es mutable y privada. La UI solo observa `uiState`, que es inmutable.
    private val _uiState = MutableStateFlow(PackageListUiState())
    val uiState: StateFlow<PackageListUiState> = _uiState.asStateFlow()

    // 4. BLOQUE INIT: La acción principal que se dispara al crear el ViewModel.
    init {
        loadInitialData()
    }

    // 5. CARGA DE DATOS: Lógica central para obtener datos de la red.
    fun loadInitialData() {
        // Ponemos `isLoading` a true y limpiamos errores antiguos.
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Usamos `runCatching` en cada llamada para que un fallo no cancele el otro.
            // CORRECCIÓN: Se eliminaron los <> de la llamada a getProfile()
            val profileResult = runCatching { authRepository.getProfile() }
            val packagesResult = runCatching { packageRepository.getTourPackages() }

            // Actualizamos el estado con los resultados, ya sean de éxito o de fallo.
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    profile = profileResult.getOrNull(), // Si falla, será null.
                    packages = packagesResult.getOrDefault(emptyList()), // Si falla, lista vacía.
                    // Si ALGUNO de los dos falló, mostramos un error genérico.
                    error = if (profileResult.isFailure || packagesResult.isFailure) {
                        "No se pudieron cargar todos los datos. Inténtalo de nuevo."
                    } else {
                        null // Si todo fue bien, no hay error.
                    }
                )
            }
        }
    }


    // 6. MANEJO DE EVENTOS DE LA UI: Funciones que la UI llamará.
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // La navegación la gestionas en la UI observando un estado o un evento.
        }
    }
}
