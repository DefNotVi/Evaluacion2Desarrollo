package com.gwagwa.evaluacion2.ui.profile

// Imports de Android (Sin cambios)
import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//Imports de Compose Activity / Lifecycle (Sin cambios)
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

// Imports de Compose UI y Material (Algunos nuevos)
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// Imports de Accompanist Permissions (Sin cambios)
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

//Imports de Componentes y Tema (Asegúrate de que tus colores Primary, Foreground, etc. sean accesibles)
import com.gwagwa.evaluacion2.ui.components.ImagePickerDialog
import com.gwagwa.evaluacion2.viewmodel.ProfileViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var showImagePicker by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Definición de permisos (Sin cambios)
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        listOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionsState = rememberMultiplePermissionsState(permissions)

    // Launchers (Sin cambios significativos en funcionalidad)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.updateAvatar(tempCameraUri)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateAvatar(it) }
    }

    // Mostrar el diálogo de selección de imagen (Sin cambios en lógica)
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onCameraClick = {
                showImagePicker = false
                if (permissionsState.permissions.any {
                        it.permission == Manifest.permission.CAMERA && it.status.isGranted
                    }) {
                    tempCameraUri = createImageUri(context)
                    tempCameraUri?.let { takePictureLauncher.launch(it) }
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            onGalleryClick = {
                showImagePicker = false
                val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                if (permissionsState.permissions.any {
                        it.permission == imagePermission && it.status.isGranted
                    }) {
                    pickImageLauncher.launch("image/*")
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }

    // Cargar datos
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToEdit) {
                        Text("Editar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                // Estado de Cargando
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Estado de Error
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌ Error al cargar",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "Error desconocido.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUser() }) {
                            Text("Reintentar")
                        }
                    }
                }

                // Estado de Datos cargados
                else -> {
                    Column(
                        modifier = Modifier.align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // TARJETA DE PERFIL MEJORADA
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // Más elevación
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar Circle con Borde
                                Box(
                                    modifier = Modifier.size(140.dp), // Un poco más grande
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    // Avatar principal (AsyncImage o Icon)
                                    val avatarModifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape) // Borde primario
                                        .clickable { showImagePicker = true }
                                        .background(MaterialTheme.colorScheme.primaryContainer)

                                    if (state.avatarUri != null) {
                                        AsyncImage(
                                            model = state.avatarUri,
                                            contentDescription = "Avatar del usuario",
                                            modifier = avatarModifier,
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            modifier = avatarModifier,
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Seleccionar avatar",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(32.dp)
                                            )
                                        }
                                    }

                                    // Icono de cámara
                                    Surface(
                                        modifier = Modifier
                                            .size(40.dp) // Un poco más grande
                                            .offset(x = 4.dp, y = 4.dp) // Posicionamiento ligero
                                            .clickable { showImagePicker = true },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiary, // Un color de acento
                                        shadowElevation = 4.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Cambiar foto",
                                            tint = MaterialTheme.colorScheme.onTertiary,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // User Name
                                Text(
                                    text = state.name,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // User Email
                                Text(
                                    text = state.userEmail,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                // User Telefono
                                Text(
                                    text = state.telefono,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                // User Direccion
                                Text(
                                    text = state.direccion,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                            }
                        }

                        // Acciones de la pantalla
                        Button(
                            onClick = { viewModel.loadUser() },
                            modifier = Modifier.fillMaxWidth(0.8f) // Botón un poco más pequeño
                        ) {
                            Text("Refrescar Datos")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Crea un URI temporal para guardar la foto capturada por la cámara. (Sin cambios)
 */
private fun createImageUri(context: Context): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "profile_avatar_$timeStamp.jpg"
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)

    return try {
        val imageFile = File(storageDir, imageFileName)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Asegúrate de que esto coincida con el 'authorities' del Manifest.
            imageFile
        )
    } catch (e: Exception) {
        null
    }
}