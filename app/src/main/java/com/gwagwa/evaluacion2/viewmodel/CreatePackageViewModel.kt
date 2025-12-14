package com.gwagwa.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gwagwa.evaluacion2.data.remote.RetrofitClient
import com.gwagwa.evaluacion2.data.remote.dto.CreatePackageRequest // Necesario para la llamada API
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// --- ESTADO DE LA UI ---
data class CreatePackageUiState(
    // Campos del formulario (usa String para la entrada de usuario)
    val nombre: String = "",
    val descripcion: String = "",
    val destino: String = "",
    val precio: String = "",
    val duracionDias: String = "",

    // Estado de la operación
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPackageCreated: Boolean = false // Flag para indicar éxito
)

class CreatePackageViewModel : ViewModel() {

    // --- Dependencias ---
    private val apiService = RetrofitClient.authApiService

    // --- State Management ---
    private val _uiState = MutableStateFlow(CreatePackageUiState())
    val uiState: StateFlow<CreatePackageUiState> = _uiState.asStateFlow()

    // --- Eventos desde la UI ---

    // Función unificada para manejar todos los cambios de campo
    fun updateField(value: String, fieldName: String) {
        // Resetear la bandera de éxito y errores al cambiar un campo
        _uiState.update { currentState ->
            when (fieldName) {
                "nombre" -> currentState.copy(nombre = value, error = null, isPackageCreated = false)
                "descripcion" -> currentState.copy(descripcion = value, error = null, isPackageCreated = false)
                "destino" -> currentState.copy(destino = value, error = null, isPackageCreated = false)
                "precio" -> currentState.copy(precio = value, error = null, isPackageCreated = false)
                "duracionDias" -> currentState.copy(duracionDias = value, error = null, isPackageCreated = false)
                else -> currentState
            }
        }
    }

    // Para resetear el formulario después de la creación si es necesario
    fun resetState() {
        _uiState.value = CreatePackageUiState()
    }

    // --- LÓGICA DE NEGOCIO ---

    fun createPackage() {
        val state = _uiState.value

        // VALIDACIÓN y conversión de tipos
        val precioInt = state.precio.toIntOrNull()
        val duracionInt = state.duracionDias.toIntOrNull()

        if (state.nombre.isBlank() || state.descripcion.isBlank() || state.destino.isBlank() || precioInt == null || duracionInt == null) {
            _uiState.update { it.copy(error = "Por favor, completa todos los campos correctamente. Precio y Duración deben ser números.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Construir el Request DTO
                val request = CreatePackageRequest(
                    nombre = state.nombre.trim(),
                    descripcion = state.descripcion.trim(),
                    destino = state.destino.trim(),
                    precio = precioInt,
                    duracionDias = duracionInt
                )

                // Llamada a la API (Asume éxito si no lanza excepción)
                apiService.createPackage(request)

                // Éxito: Resetea el formulario y pone la Flag
                _uiState.update {
                    CreatePackageUiState(isPackageCreated = true)
                }

            } catch (e: Exception) {
                // Manejo de Errores de API/Red
                handleCreatePackageError(e)
            }
        }
    }

    /**
     * Función centralizada para manejar errores específicos de la creación de paquetes.
     */
    private fun handleCreatePackageError(e: Exception) {
        val errorMessage = when (e) {
            is HttpException -> when (e.code()) {
                400 -> "Error en los datos enviados. Revisa el formulario."
                401 -> "No autorizado. Tu sesión de administrador expiró."
                else -> "Error del servidor. Inténtalo de nuevo."
            }
            is IOException -> "Sin conexión. Revisa tu acceso a internet."
            else -> "ᗜˬᗜ" +
                    "⠀⠀⠀⣠⠤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⠀⠀\n" +
                    "⠀⠀⡜⠁⠀⠈⢢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⠋⠷⠶⠱⡄\n" +
                    "⠀⢸⣸⣿⠀⠀⠀⠙⢦⡀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠫⢀⣖⡃⢀⣸⢹\n" +
                    "⠀⡇⣿⣿⣶⣤⡀⠀⠀⠙⢆⠀⠀⠀⠀⠀⣠⡪⢀⣤⣾⣿⣿⣿⣿⣸\n" +
                    "⠀⡇⠛⠛⠛⢿⣿⣷⣦⣀⠀⣳⣄⠀⢠⣾⠇⣠⣾⣿⣿⣿⣿⣿⣿⣽\n" +
                    "⠀⠯⣠⣠⣤⣤⣤⣭⣭⡽⠿⠾⠞⠛⠷⠧⣾⣿⣿⣯⣿⡛⣽⣿⡿⡼\n" +
                    "⠀⡇⣿⣿⣿⣿⠟⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⠻⣿⣿⣮⡛⢿⠃\n" +
                    "⠀⣧⣛⣭⡾⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢿⣿⣷⣎⡇\n" +
                    "⠀⡸⣿⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢿⣷⣟⡇\n" +
                    "⣜⣿⣿⡧⠀⠀⠀⠀⠀⡀⠀⠀⠀⠀⠀⠀⣄⠀⠀⠀⠀⠀⣸⣿⡜⡄\n" +
                    "⠉⠉⢹⡇⠀⠀⠀⢀⣞⠡⠀⠀⠀⠀⠀⠀⡝⣦⠀⠀⠀⠀⢿⣿⣿⣹\n" +
                    "⠀⠀⢸⠁⠀⠀⢠⣏⣨⣉⡃⠀⠀⠀⢀⣜⡉⢉⣇⠀⠀⠀⢹⡄⠀⠀\n" +
                    "⠀⠀⡾⠄⠀⠀⢸⣾⢏⡍⡏⠑⠆⠀⢿⣻⣿⣿⣿⠀⠀⢰⠈⡇⠀⠀\n" +
                    "⠀⢰⢇⢀⣆⠀⢸⠙⠾⠽⠃⠀⠀⠀⠘⠿⡿⠟⢹⠀⢀⡎⠀⡇⠀⠀\n" +
                    "⠀⠘⢺⣻⡺⣦⣫⡀⠀⠀⠀⣄⣀⣀⠀⠀⠀⠀⢜⣠⣾⡙⣆⡇⠀⠀\n" +
                    "⠀⠀⠀⠙⢿⡿⡝⠿⢧⡢⣠⣤⣍⣀⣤⡄⢀⣞⣿⡿⣻⣿⠞⠀⠀⠀\n" +
                    "⠀⠀⠀⢠⠏⠄⠐⠀⣼⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠳⢤⣉⢳⠀⠀⠀\n" +
                    "⢀⡠⠖⠉⠀⠀⣠⠇⣿⡿⣿⡿⢹⣿⣿⣿⣿⣧⣠⡀⠀⠈⠉⢢⡀⠀\n" +
                    "⢿⠀⠀⣠⠴⣋⡤⠚⠛⠛⠛⠛⠛⠛⠛⠛⠙⠛⠛⢿⣦⣄⠀⢈⡇⠀\n" +
                    "⠈⢓⣤⣵⣾⠁⣀⣀⠤⣤⣀⠀⠀⠀⠀⢀⡤⠶⠤⢌⡹⠿⠷⠻⢤⡀\n" +
                    "⢰⠋⠈⠉⠘⠋⠁⠀⠀⠈⠙⠳⢄⣀⡴⠉⠀⠀⠀⠀⠙⠂⠀⠀⢀⡇\n" +
                    "⢸⡠⡀⠀⠒⠂⠐⠢⠀⣀⠀⠀⠀⠀⠀⢀⠤⠚⠀⠀⢸⣔⢄⠀⢾⠀\n" +
                    "⠀⠑⠸⢿⠀⠀⠀⠀⢈⡗⠭⣖⡒⠒⢊⣱⠀⠀⠀⠀⢨⠟⠂⠚⠋⠀\n" +
                    "⠀⠀⠀⠘⠦⣄⣀⣠⠞⠀⠀⠀⠈⠉⠉⠀⠳⠤⠤⡤⠞⠀⠀⠀⠀⠀"

        }
        _uiState.update { it.copy(isLoading = false, error = errorMessage) }
    }
}