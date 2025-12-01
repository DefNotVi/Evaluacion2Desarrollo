package com.gwagwa.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.PackageDto
import com.gwagwa.evaluacion2.repository.PackageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.app.Application
import androidx.lifecycle.AndroidViewModel // <-- CAMBIO de ViewModel a AndroidViewModel
import com.gwagwa.evaluacion2.data.local.SessionManager
import com.gwagwa.evaluacion2.repository.AuthRepository


data class PackageListUiState(
    val isLoading: Boolean = false,
    val allPackages: List<PackageDto> = emptyList(), // La lista original de la API
    val filteredPackages: List<PackageDto> = emptyList(), // La lista que se muestra en la UI
    val error: String? = null,
    // Estados para los filtros
    val searchQuery: String = "",
    val selectedCategory: String? = null
)

class PackageListViewModel(application: Application) : AndroidViewModel(application) {

    // --- LÓGICA DE PAQUETES  ---
    private val apiService = RetrofitClient.authApiService
    private val packageRepository = PackageRepository(apiService)

    // --- LÓGICA DE AUTENTICACIÓN ---
    private val sessionManager = SessionManager(application.applicationContext)
    private val authRepository = AuthRepository(apiService, sessionManager) // Reutilizamos el apiService

    private val _uiState = MutableStateFlow(PackageListUiState())
    val uiState: StateFlow<PackageListUiState> = _uiState

    init {
        fetchPackages()
    }

    fun fetchPackages() { // Cambiado a 'public' por si necesitas recargar desde la UI
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val packages = packageRepository.getTourPackages()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allPackages = packages,
                        filteredPackages = packages // Al inicio, la lista filtrada es igual a la completa
                    )
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Error de red. Revisa tu conexión.") }
            } catch (e: HttpException) { //Se usa la HttpException de Retrofit
                _uiState.update { it.copy(isLoading = false, error = "Error del servidor.") }
            }
        }
    }

    // --- FILTROS ---

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filteredList = state.allPackages.filter { pkg -> // Esto usará el 'filter' estándar de Kotlin
            // Filtro por nombre (búsqueda)
            val matchesSearch = pkg.name.contains(state.searchQuery, ignoreCase = true)
            // Filtro por categoría
            val matchesCategory = state.selectedCategory == null || pkg.category == state.selectedCategory

            matchesSearch && matchesCategory
        }
        _uiState.update { it.copy(filteredPackages = filteredList) }
    }

    // --- LOGOUT ---

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // No necesitamos un callback aquí. La navegación se maneja en la UI.
        }
    }
}
