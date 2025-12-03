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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// ESTADO DE LA UI: Define todo lo que la pantalla puede necesitar
data class PackageListUiState(
    // Estado general
    val isLoading: Boolean = true,
    val error: String? = null,

    // Datos
    val profile: UserDto? = null,
    val packages: List<PackageDto> = emptyList(),

    // Estado de los filtros
    val searchQuery: String = "",
    val selectedCategory: String? = null // Mantenemos el campo para el filtro
)

class PackageListViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.authApiService
    private val packageRepository = PackageRepository(apiService)
    private val authRepository = AuthRepository(apiService, SessionManager(application))


    //  STATEFLOW: El corazón del ViewModel
    private val _uiState = MutableStateFlow(PackageListUiState())
    val uiState: StateFlow<PackageListUiState> = _uiState.asStateFlow()

    // Lista paquetes filtrada (AHORA INCLUYE FILTRO POR CATEGORÍA)

    val filteredPackages: StateFlow<List<PackageDto>> =
        _uiState.map { state ->
            // --- INICIO DEl FILTRADO ---
            var list = state.packages

            //  Aplicar filtro de búsqueda por nombre
            if (state.searchQuery.isNotBlank()) {
                val query = state.searchQuery.trim().lowercase()
                list = list.filter {
                    it.name.lowercase().contains(query)
                }
            }

            if (state.selectedCategory != null) {
                list = list.filter {
                    // Mantiene el paquete si su categoría (destino) coincide con la seleccionada
                    it.category == state.selectedCategory
                }
            }
            // --- FIN DEL FILTRADO ---
            list
        }.stateIn(
            scope = viewModelScope,
            // Configuración para que el StateFlow esté activo mientras la UI lo observe
            started = SharingStarted.WhileSubscribed(5000),
            // Valor inicial antes de que se calcule por primera vez
            initialValue = _uiState.value.packages
        )

    //  BLOQUE INIT: La acción principal que se dispara al crear el ViewModel
    init {
        loadInitialData()
    }

    // Lógica para determinar el mensaje de error
    private fun getErrorMessage(profileFailure: Throwable?, packagesFailure: Throwable?): String? {
        val messages = mutableListOf<String>()

        //Mensaje de fallo de Perfil
        if (profileFailure != null) {
            val errorMsg = when (profileFailure) {
                is HttpException -> "Error HTTP (${profileFailure.code()}) al cargar el perfil. ¿Sesión expirada?"
                is IOException -> "Error de red al cargar el perfil."
                else -> "Fallo desconocido al cargar el perfil."
            }
            messages.add(errorMsg)
        }

        //Mensaje de fallo de Paquetes
        if (packagesFailure != null) {
            val errorMsg = when (packagesFailure) {
                // El error de mapeo JSON (por el cambio en el backend) suele ser una excepción de IO o de deserialización
                is HttpException -> "Error HTTP (${packagesFailure.code()}) al cargar los paquetes. (Verifica URL/Auth)"
                is IOException -> "Error de red o mapeo de datos JSON fallido al cargar los paquetes. (Revisar PackageDto)"
                else -> "Fallo desconocido al cargar los paquetes."
            }
            messages.add(errorMsg)
        }

        //Combina los mensajes si ambos fallaron
        return when (messages.size) {
            0 -> null
            1 -> messages.first()
            else -> "Fallos de carga: ${messages.joinToString(separator = " y ")}."
        }
    }


    //  Lógica central para obtener datos de la red
    fun loadInitialData() {
        // Se pone "isLoading" a true y limpia errores antiguos
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Usa "runCatching" para ejecutar las llamadas y manejar los fallos
            val profileResult = runCatching { authRepository.getProfile() }
            val packagesResult = runCatching { packageRepository.getTourPackages() }

            // Obtenemos los fallos para analizarlos
            val profileFailure = profileResult.exceptionOrNull()
            val packagesFailure = packagesResult.exceptionOrNull()

            // Determina el mensaje de error con la nueva lógica
            val errorMessage = getErrorMessage(profileFailure, packagesFailure)

            // Actualiza el estado con los resultados, ya sean de éxito o de fallo
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    profile = profileResult.getOrNull(),
                    packages = packagesResult.getOrDefault(emptyList()),
                    // Asigna el mensaje de error específico o null
                    error = errorMessage
                )
            }
        }
    }


    //  Funciones que la UI llamará.
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    // Esta función ayuda a la UI a saber qué categoría mostrar (se supone xd)
    fun getUniqueCategories(): List<String> {
        return _uiState.value.packages
            .map { it.category } // Obtiene todas las categorías
            .distinct()           // Elimina las duplicadas
            .sorted()             // Ordena para que se muestren de forma consistente
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}