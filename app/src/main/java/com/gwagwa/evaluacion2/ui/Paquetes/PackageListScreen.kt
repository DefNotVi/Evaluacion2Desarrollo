package com.gwagwa.evaluacion2.ui.Paquetes // O ui.packages, como lo hayas nombrado

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons // <-- CAMBIO
import androidx.compose.material.icons.filled.Logout // <-- CAMBIO: Importamos el nuevo icono
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

@OptIn(ExperimentalMaterial3Api::class) // <-- CAMBIO: Anotación añadida aquí
@Composable
fun PackageListScreen(
    viewModel: PackageListViewModel = viewModel(),
    onPackageClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    // Observamos el estado de la UI desde el ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paquetes Turísticos") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            // Sección de Filtros
            FilterControls(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                // Extraemos las categorías únicas de la lista completa
                categories = uiState.allPackages.map { it.category }.distinct(),
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )

            // Contenido principal de la pantalla
            when {
                // Estado de Carga
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Estado de Error
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.error ?: "Ocurrió un error",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Estado de Éxito (muestra la lista)
                else -> {
                    PackageList(
                        packages = uiState.filteredPackages, // Usamos la lista filtrada
                        onPackageClick = onPackageClick
                    )
                }
            }
        }
    }
}

// ... (El resto del archivo no necesita cambios, pero asegúrate de que PackageCard
// también tenga la anotación @OptIn(ExperimentalMaterial3Api::class) si usa Card, como ya lo tienes)

@Composable
private fun FilterControls(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Campo de texto para buscar por nombre
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Buscar por nombre...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Aquí podrías agregar un DropdownMenu para las categorías
        Text("Filtros por categoría (implementar Dropdown):", style = MaterialTheme.typography.titleSmall)
        Row {
            // Botón para limpiar filtro de categoría
            Button(onClick = { onCategorySelected(null) }, enabled = selectedCategory != null) {
                Text("Todos")
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Botones simples como ejemplo de filtro de categoría
            categories.take(2).forEach { category ->
                Button(onClick = { onCategorySelected(category) }) {
                    Text(category)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun PackageList(
    packages: List<PackageDto>,
    onPackageClick: (Int) -> Unit
) {
    if (packages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontraron paquetes con esos filtros.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(packages) { pkg ->
                PackageCard(packageDto = pkg, onClick = { onPackageClick(pkg.id) })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
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
