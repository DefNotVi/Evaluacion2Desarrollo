package com.gwagwa.evaluacion2.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gwagwa.evaluacion2.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit, // Para volver después de guardar
    // Usa el mismo ViewModel que la pantalla de vista para compartir el estado
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Manejo de Éxito y Errores ---

    //  Efecto para manejar el guardado exitoso
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "✅ Perfil actualizado exitosamente!",
                duration = SnackbarDuration.Short
            )
            // Llama al callback de navegación
            onProfileUpdated()
            // Limpia el estado de éxito (opcional, si no lo hace el ViewModel)
            // viewModel.resetSaveSuccess()
        }
    }

    // Efecto para mostrar errores
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                message = "Error: $it",
                actionLabel = "Cerrar",
                duration = SnackbarDuration.Short
            )
        }
    }

    // --- UI Principal ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Campo Nombre
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre Completo") },
                placeholder = { Text("Ej: María Fernanda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo Teléfono
            OutlinedTextField(
                // Usa takeIf para mostrar vacío si tiene el texto por defecto
                value = state.telefono.takeIf { it != "No disponible" } ?: "",
                onValueChange = viewModel::onTelefonoChange,
                label = { Text("Teléfono") },
                placeholder = { Text("Ej: +56 9 XXXXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo Dirección
            OutlinedTextField(
                value = state.direccion.takeIf { it != "No disponible" } ?: "",
                onValueChange = viewModel::onDireccionChange,
                label = { Text("Dirección") },
                placeholder = { Text("Ej: Calle Falsa 123, Comuna") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo Documento de Identidad
            OutlinedTextField(
                value = state.documentoIdentidad,
                onValueChange = viewModel::onDocumentoChange,
                label = { Text("Documento de Identidad") },
                placeholder = { Text("Ej: 19.xxx.xxx-x") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Aquí iría el coso para editar la lista de Preferencias o algo

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Guardar
            Button(
                onClick = viewModel::saveProfile,
                // Deshabilita el botón si está guardando
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}