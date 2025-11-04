package com.gwagwa.evaluacion2.ui.components

import com.gwagwa.evaluacion2.ui.theme.Foreground
import com.gwagwa.evaluacion2.ui.theme.ForegroundMuted
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ✅ Importación de tu tema, asumiendo que contiene las constantes de color
import com.gwagwa.evaluacion2.ui.theme.*

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // Usamos MaterialTheme.colorScheme.surface ya que 'Surface' podría no ser una constante de color simple
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Seleccionar imagen",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    // ✅ Uso de la constante 'Foreground' de tu tema
                    color = Foreground
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Elige cómo deseas seleccionar tu imagen:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        // ✅ Uso de la constante 'ForegroundMuted' de tu tema
                        color = ForegroundMuted
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opción de Cámara
                ImagePickerOption(
                    icon = Icons.Filled.CameraAlt,
                    title = "Tomar foto",
                    description = "Abre la cámara para capturar una nueva foto",
                    onClick = onCameraClick
                )

                // ✅ Reemplazo de ShadcnDivider por Divider de Material 3
                HorizontalDivider()

                // Opción de Galería
                ImagePickerOption(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "Elegir de galería",
                    description = "Selecciona una imagen de tu dispositivo",
                    onClick = onGalleryClick
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            // ✅ Reemplazo de ShadcnButton por OutlinedButton de Material 3
            OutlinedButton(
                onClick = onDismiss,
                // Parámetros 'variant' y 'size' eliminados ya que no existen en OutlinedButton estándar
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ImagePickerOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    // ✅ Reemplazo de ShadcnCard por Card de Material 3
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Hacemos la Card clickeable, ya que Card de Material 3 no siempre lo es por defecto
            .clickable(onClick = onClick),
        // Parámetro 'elevation' eliminado (usa el valor por defecto o ContentElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                // ✅ Uso de la constante 'Primary' de tu tema
                tint = Primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        // ✅ Uso de la constante 'Foreground' de tu tema
                        color = Foreground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        // ✅ Uso de la constante 'ForegroundMuted' de tu tema
                        color = ForegroundMuted
                    )
                )
            }
        }
    }
}