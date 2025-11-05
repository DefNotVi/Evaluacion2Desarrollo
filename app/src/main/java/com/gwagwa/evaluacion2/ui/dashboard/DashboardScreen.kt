package com.gwagwa.evaluacion2.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gwagwa.evaluacion2.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TravelGo SPA: Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text("Autenticación Exitosa!", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToProfile) {
            Text("Ir a Mi Perfil")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Cerrar Sesión (Logout)
        OutlinedButton(
            onClick = { viewModel.logout(onLogout) }
        ) {
            Text("Cerrar Sesión")
        }
    }
}