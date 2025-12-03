package com.gwagwa.evaluacion2.ui.packageList

// Importaciones necesarias
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Locale
import java.text.NumberFormat // Importación para formatear el Double a Moneda

// ----------------------------------------------------------------------
// 1. PUNTO DE ENTRADA PRINCIPAL: PackageListScreen
// ----------------------------------------------------------------------

@Composable
fun PackageListScreen(
    viewModel: PackageListViewModel = viewModel(),
    // *** CORRECCIÓN CRÍTICA: onPackageClick ahora espera un String para el ID ***
    onPackageClick: (String) -> Unit,
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

            // BÚSQUEDA Y FILTROS
            SearchAndFilterSection(
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                uniqueCategories = viewModel.getUniqueCategories(),
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            // Contenido de la pantalla (Cargando, Error, Éxito)
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(message = uiState.error)
                }
                else -> {
                    SuccessState(
                        packages = filteredPackages,
                        onPackageClick = onPackageClick
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// 2. COMPONENTES AUXILIARES
// ----------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageListTopBar(
    username: String?,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            val displayName = remember(username) { username?.substringBefore('@')?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } }
            Column(horizontalAlignment = Alignment.Start) {
                Text("TravelApp", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (displayName != null) "Hola, $displayName" else "Paquetes Turísticos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
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
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
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
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Buscar paquete por nombre o destino") },
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

        Spacer(modifier = Modifier.height(12.dp))

        if (uniqueCategories.isNotEmpty() || selectedCategory != null) {
            Text(
                "Filtrar por Destino:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                val isAllSelected = selectedCategory == null
                AssistChip(
                    onClick = { onCategorySelected(null) },
                    label = { Text("Todos") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                uniqueCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            val newCategory = if (selectedCategory == category) null else category
                            onCategorySelected(newCategory)
                        },
                        label = { Text(category) },
                        modifier = Modifier.wrapContentWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cargando paquetes...")
        }
    }
}

@Composable
private fun ErrorState(message: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
    // onPackageClick espera String
    onPackageClick: (String) -> Unit
) {
    if (packages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No se encontraron paquetes con los criterios de búsqueda actuales.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = packages,
                key = { it.id } // Clave robusta (String)
            ) { pkg ->
                // *** CORRECCIÓN FINAL: onPackageClick recibe pkg.id (String) ***
                PackageCard(packageDto = pkg, onClick = { onPackageClick(pkg.id) })
            }
        }
    }
}

// ----------------------------------------------------------------------
// 3. COMPONENTE CARD (Con manejo de Double para el precio)
// ----------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageCard(
    packageDto: PackageDto,
    onClick: () -> Unit
) {
    // URL BASE DE LA IMAGEN
    val BASE_URL_IMAGE = "https://travelgo-api-1.onrender.com/"

    // Formateador de moneda (ejemplo para usar con el Double)
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")) // Puedes ajustar la Locale
    }

    // Prepara la URL completa de la imagen
    val fullImageUrl = remember(packageDto.imageUrl) {
        if (packageDto.imageUrl?.startsWith("http") == true) {
            packageDto.imageUrl
        } else if (packageDto.imageUrl != null) {
            BASE_URL_IMAGE + packageDto.imageUrl.trimStart('/')
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onClick
    ) {
        Column {
            // IMAGEN O FALLBACK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                if (fullImageUrl != null) {
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Imagen de ${packageDto.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    // Alternativa si no hay URL de imagen (Fallback)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏞️ Sin imagen disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }


            Column(modifier = Modifier.padding(16.dp)) {

                // Destino (Categoría como etiqueta)
                AssistChip(
                    onClick = { /* No-op */ },
                    label = { Text(packageDto.category) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.wrapContentWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Título
                Text(
                    text = packageDto.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Precio (Destacado)
                Text(
                    // Usamos el Double y lo formateamos a String de moneda
                    text = currencyFormatter.format(packageDto.price),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // DESCRIPCIÓN (Itinerario)
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Itinerario:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = packageDto.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}