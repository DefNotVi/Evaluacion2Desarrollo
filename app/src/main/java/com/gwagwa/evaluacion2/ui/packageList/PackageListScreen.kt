package com.gwagwa.evaluacion2.ui.packageList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gwagwa.evaluacion2.data.remote.dto.PackageDto
import com.gwagwa.evaluacion2.viewmodel.PackageListViewModel
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle

//   PUNTO DE ENTRADA PRINCIPAL: La pantalla completa
@Composable
fun PackageListScreen(
    // El ViewModel se inyecta automáticamente con `viewModel()`
    viewModel: PackageListViewModel = viewModel(),
    // Funciones de navegación para desacoplar la lógica de la UI
    onPackageClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    // Observamos el estado completo de la UI desde el ViewModel.
    // `uiState` contiene todo lo que necesito: carga, error, perfil, paquetes, etc.
    val uiState by viewModel.uiState.collectAsState()

    // `Scaffold` es el esqueleto de la pantalla (barra superior, contenido, etc.)
    Scaffold(
        topBar = {
            PackageListTopBar(
                username = uiState.profile?.email,
                onLogoutClick = {
                    viewModel.logout()
                    onLogout()
                },
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        // Contenedor principal que respeta el espacio de la TopBar
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // El contenido de la pantalla cambia según el estado (Cargando, Error, Éxito)
            when {
                uiState.isLoading -> {
                    // Muestra un indicador de carga mientras se obtienen los datos
                    LoadingState()
                }
                uiState.error != null -> {
                    // Muestra un mensaje de error si algo falló
                    ErrorState(message = uiState.error)
                }
                else -> {
                    // Si todo está bien, muestra la lista de paquetes
                    SuccessState(
                        packages = uiState.packages,
                        onPackageClick = onPackageClick
                    )
                }
            }
        }
    }
}

//    COMPONENTES DE ESTADO: Pequeños Composables para cada estado de la UI

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Cargando paquetes...")
    }
}

@Composable
private fun ErrorState(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message ?: "Ocurrió un error inesperado. Inténtalo de nuevo.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SuccessState(
    packages: List<PackageDto>,
    onPackageClick: (Int) -> Unit
) {
    if (packages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay paquetes disponibles en este momento.")
        }
    } else {
        // `LazyColumn` es la forma eficiente de mostrar listas en Compose
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio automático entre ítems
        ) {
            items(
                items = packages,
                key = { it.id } // Clave única para optimizar el rendimiento de la lista
            ) { pkg ->
                PackageCard(packageDto = pkg, onClick = { onPackageClick(pkg.id) })
            }
        }
    }
}

// COMPONENTES REUTILIZABLES de la UI como la barra superior y las tarjetas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageListTopBar(
    username: String?,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        // EN PackageListTopBar
        title = {
            val displayName = username?.substringBefore('@') // Extrae "admin" de "admin@sistema.com"
            Text(if (displayName != null) "Hola, $displayName" else "Paquetes Turísticos")
        },


        actions = {

            IconButton(onClick = onProfileClick) {
                Icon(
                    // Importa el ícono si es necesario: import androidx.compose.material.icons.filled.AccountCircle
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Ir al Perfil"
                )
            }

            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout, // <-- Uso NUEVO y correcto
                    contentDescription = "Cerrar Sesión"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageCard(
    packageDto: PackageDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = packageDto.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Categoría: ${packageDto.category}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Precio: $${packageDto.price}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
