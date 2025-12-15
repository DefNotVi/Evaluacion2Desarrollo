package com.gwagwa.evaluacion2.ui.createPackages

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gwagwa.evaluacion2.viewmodel.CreatePackageViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePackageScreen(
    // Función para manejar la navegación de vuelta al dashboard del Admin
    onNavigateBack: () -> Unit,
    viewModel: CreatePackageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Side Effects (Manejo de Éxito y Errores) ---

    // Mostrar Snackbar en caso de creación exitosa (es como una barra que aparece abajo como notif)
    LaunchedEffect(uiState.isPackageCreated) {
        if (uiState.isPackageCreated) {
            snackbarHostState.showSnackbar(
                message = "✅ Paquete '${uiState.nombre}' creado exitosamente!",
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            // Navegar hacia atrás automáticamente después de un retraso pequeño
            delay(1500)
            onNavigateBack()
        }
    }

    // Mostrar Snackbar en caso de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = "Error: $it",
                actionLabel = "Cerrar",
                duration = SnackbarDuration.Short
            )
        }
    }

    // --- Interfaz de Usuario ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Crear Nuevo Paquete") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre del Paquete
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.updateField(it, "nombre") },
                label = { Text("Nombre del Paquete") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Destino
            OutlinedTextField(
                value = uiState.destino,
                onValueChange = { viewModel.updateField(it, "destino") },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Precio (Solo números)
            OutlinedTextField(
                value = uiState.precio,
                onValueChange = { viewModel.updateField(it, "precio") },
                label = { Text("Precio ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Duración en Días (Solo números)
            OutlinedTextField(
                value = uiState.duracionDias,
                onValueChange = { viewModel.updateField(it, "duracionDias") },
                label = { Text("Duración (en días)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Descripción (Usada como Itinerario)
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.updateField(it, "descripcion") },
                label = { Text("Descripción/Itinerario") },
                placeholder = { Text("Detalle de las actividades del paquete...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de Creación
            Button(
                onClick = viewModel::createPackage,
                // Deshabilitar si está cargando o si ya se creó (evita doble click)
                enabled = !uiState.isLoading && !uiState.isPackageCreated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Crear Paquete Turístico")
                }
            }
        }
    }
}