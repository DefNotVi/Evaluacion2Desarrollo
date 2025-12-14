package com.gwagwa.evaluacion2.ui.packageList


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gwagwa.evaluacion2.viewmodel.AdminPackageListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPackageScreen(
    onManageUsersClick: () -> Unit,
    onCreatePackageClick: () -> Unit, // Lo dejo preparado para el futuro
    onLogout: () -> Unit,
    adminViewModel: AdminPackageListViewModel = viewModel()
) {
    val uiState by adminViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
                uiState.user != null -> {
                    AdminDashboardContent(
                        // Pasa el email del DTO si el nombre no existe
                        adminName = uiState.user?.email ?: "Admin",
                        onManageUsersClick = onManageUsersClick,
                        onCreatePackageClick = onCreatePackageClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    adminName: String,
    onManageUsersClick: () -> Unit,
    onCreatePackageClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "¡Bienvenido, $adminName!",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Botón para gestionar usuarios
        Button(
            onClick = onManageUsersClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Gestionar Usuarios")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para crear paquetes
        Button(
            onClick = onCreatePackageClick,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Crear Nuevo Paquete")
        }
    }
}
