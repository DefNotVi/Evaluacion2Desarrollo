package com.gwagwa.evaluacion2.ui.profile

// Imports de Android
import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//Imports de Compose Activity / Lifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

// Imports de Compose UI y Material
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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

// Imports de Accompanist Permissions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

//Imports de Componentes y Tema
import com.gwagwa.evaluacion2.ui.components.ImagePickerDialog
import com.gwagwa.evaluacion2.ui.theme.*
import com.gwagwa.evaluacion2.viewmodel.ProfileViewModel

// Necesario para usar rememberMultiplePermissionsState
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(), // ⚠️ Cambia el ID según necesites //<-- sure will!
    onNavigateBack: () -> Unit
) {
    // Obtener el contexto local y el estado del ViewModel
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // Estados de la UI para manejar el dialogo y el URI temporal de la camara
    var showImagePicker by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) } // URI temporal para guardar la foto

    // Definir los permisos según la versión de Android
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        listOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionsState = rememberMultiplePermissionsState(permissions)

    // Launcher para capturar foto con cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // Si la foto se tomó correctamente, actualizar el avatar en el ViewModel
        if (success && tempCameraUri != null) {
            viewModel.updateAvatar(tempCameraUri)
        }
    }

    // Launcher para seleccionar imagen de galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Si se seleccionó una URI, actualizar el avatar en el ViewModel
        uri?.let { viewModel.updateAvatar(it) }
    }

    // Mostrar el diálogo de selección de imagen (Se muestra si showImagePicker es true)
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onCameraClick = {
                showImagePicker = false
                if (permissionsState.permissions.any {
                        it.permission == Manifest.permission.CAMERA && it.status.isGranted
                    }) {
                    // Crear archivo temporal para la foto
                    tempCameraUri = createImageUri(context)
                    tempCameraUri?.let { takePictureLauncher.launch(it) }
                } else {
                    // Solicitar permiso de cámara
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
                        it.permission == imagePermission && it.status.isGranted //
                    }) {
                    // Lanzar selector de galería
                    pickImageLauncher.launch("image/*")
                } else {
                    // Solicitar permiso de almacenamiento
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }

    // Cargar datos cuando la pantalla se abre (el LaunchedEffect ya estaba aquí)
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    // Contenedor principal de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            // Estado de Cargando con la animacion de "CircularProgressIndicator"
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Estado de Error (cuantas veces vi este mensaje 😭)
            state.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "❌ Error",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Perfil de Usuario",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        // Reemplazando ShadcnCard
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier.size(120.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                // Avatar principal (AsyncImage o Icon)
                                if (state.avatarUri != null) {
                                    // Mostrar imagen seleccionada con Coil
                                    AsyncImage(
                                        model = state.avatarUri,
                                        contentDescription = "Avatar del usuario",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .clickable { showImagePicker = true }
                                            .background(Primary), // Usa la constante Primary del tema
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Mostrar icono por defecto
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { showImagePicker = true },
                                        shape = CircleShape,
                                        color = Primary // Usa la constante Primary
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Seleccionar avatar",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(28.dp)
                                        )
                                    }
                                }

                                // Icono de cámara
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { showImagePicker = true },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface, // Usa colorScheme.surface
                                    shadowElevation = 2.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Cambiar foto",
                                        tint = Primary, // Usa la constante Primary
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // User Name
                            Text(
                                text = state.name, // Usa state.userName
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Foreground // Usa la constante Foreground
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // User Email
                            Text(
                                text = state.userEmail, // Usa state.userEmail
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ForegroundMuted // Usa la constante ForegroundMuted
                                )
                            )
                        }
                    }
                    // Fin del Avatar Card

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { viewModel.loadUser() }) {
                        Text("Refrescar")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Volver al Dashboard")
                    }
                }
            }
        }
    }
}


/**
 * Crea un URI temporal para guardar la foto capturada por la cámara.
 */
private fun createImageUri(context: Context): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "profile_avatar_$timeStamp.jpg"
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)

    return try {
        val imageFile = File(storageDir, imageFileName)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // ⚠️ IMPORTANTE: Asegúrate que esto coincida con el 'authorities' del Manifest.
            imageFile
        )
    } catch (e: Exception) {
        null
    }
}