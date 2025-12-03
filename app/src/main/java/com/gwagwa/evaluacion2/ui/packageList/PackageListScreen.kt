package com.gwagwa.evaluacion2.ui.packageList

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.remember

// Nuevos imports para manejar imágenes remotas
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow

//   PUNTO DE ENTRADA PRINCIPAL: La pantalla completa
@Composable
fun PackageListScreen(
    viewModel: PackageListViewModel = viewModel(),
    onPackageClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredPackages by viewModel.filteredPackages.collectAsState()

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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            // BÚSQUEDA Y FILTROS (Se habilita la lógica de chips)
            SearchAndFilterSection(
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                uniqueCategories = viewModel.getUniqueCategories(), // Ahora lista destinos reales
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            // El contenido de la pantalla cambia según el estado (Cargando, Error, Éxito)
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(message = uiState.error)
                }
                else -> {
                    // Si todo está bien, muestra la lista de paquetes
                    SuccessState(
                        packages = filteredPackages, // Usamos la lista filtrada
                        onPackageClick = onPackageClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    uniqueCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // 1. Barra de Búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Buscar paquete por nombre") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Chips de Categorías (se mostrarán si hay destinos reales en uniqueCategories)
        if (uniqueCategories.isNotEmpty() || selectedCategory != null) {
            Text("Filtrar por Destino:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // "Mostrar Todos" (Limpiar filtro)
                AssistChip(
                    onClick = { onCategorySelected(null) },
                    label = { Text("Todos") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (selectedCategory == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Chips para cada categoría (destino) única
                uniqueCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            val newCategory = if (selectedCategory == category) null else category
                            onCategorySelected(newCategory)
                        },
                        label = { Text(category) },
                        modifier = Modifier.wrapContentWidth(),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}


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
            Text("No se encontraron paquetes con los criterios de búsqueda actuales.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = packages,
                key = { it.id }
            ) { pkg ->
                PackageCard(packageDto = pkg, onClick = { onPackageClick(pkg.id.hashCode()) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageListTopBar(
    username: String?,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            val displayName = remember(username) { username?.substringBefore('@') }
            Text(if (displayName != null) "Hola, $displayName" else "Paquetes Turísticos")
        },


        actions = {

            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Ir al Perfil"
                )
            }

            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
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
    // DONDE ESTÁN ALOJADAS LAS IMÁGENES DE 'uploads/...'
    val BASE_URL_IMAGE = "https://travelgo-api-1.onrender.com/"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // IMAGEN (Si está disponible)
            if (packageDto.imageUrl != null) {

                // CONSTRUIMOS LA URL COMPLETA
                val fullImageUrl = if (packageDto.imageUrl.startsWith("http")) {
                    packageDto.imageUrl // Ya es una URL completa
                } else {
                    // Concatenamos la URL base y el path relativo (ej: "uploads/...")
                    BASE_URL_IMAGE + packageDto.imageUrl.trimStart('/')
                }

                AsyncImage(
                    model = fullImageUrl, // Usamos la URL completa
                    contentDescription = "Imagen de ${packageDto.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // Altura fija para la imagen
                    contentScale = ContentScale.Crop, // Asegura que la imagen cubra el área
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Título
            Text(text = packageDto.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // Destino (Categoría)
            Text(text = "Destino: ${packageDto.category}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            // Precio
            Text(text = "Precio: $${packageDto.price}", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.height(8.dp))

            // 2. DESCRIPCIÓN (Usada como itinerario)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Descripción / Itinerario: ",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = packageDto.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3, // Limita la descripción para que no ocupe demasiado espacio en la lista.
                overflow = TextOverflow.Ellipsis // Agrega "..." si se trunca
            )

            // NOTA: Para ver la descripción completa, el usuario tendrá que hacer clic en la tarjeta (onClick)
            // lo que llevaría a la pantalla de detalle del paquete.
        }
    }
}