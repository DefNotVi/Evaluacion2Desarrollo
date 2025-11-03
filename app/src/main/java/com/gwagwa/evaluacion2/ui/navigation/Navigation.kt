package com.gwagwa.evaluacion2.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Importar la pantalla de perfil
import com.gwagwa.evaluacion2.ui.profile.ProfileScreen


// El composable que actuará como tu pantalla principal (HomeScreen)
@Composable
fun StartScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡App Inicial lista!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("profile") }
        ) {
            Text("Ver Perfil")
        }
    }
}


@Composable
fun AppNavigation() {
    // Inicializa el controlador de navegación
    val navController = rememberNavController()

    // NavHost: Define el área donde se mostrarán las pantallas
    NavHost(
        navController = navController,
        startDestination = "start" // ⬅️ Usamos la ruta "start" como la inicial
    ) {

        // Ruta "start": Tu pantalla principal
        composable("start") {
            StartScreen(navController = navController)
        }

        // Ruta "profile": Tu pantalla de perfil
        composable("profile") {
            ProfileScreen()
        }
    }
}