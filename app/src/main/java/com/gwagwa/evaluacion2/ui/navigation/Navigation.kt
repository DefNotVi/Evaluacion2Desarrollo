package com.gwagwa.evaluacion2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gwagwa.evaluacion2.ui.profile.ProfileScreen // ⬅️ Asegúrate de importar tu pantalla

@Composable
fun AppNavigation() {
    // 1. Crear el controlador de navegación
    val navController = rememberNavController()

    // 2. Definir el NavHost
    NavHost(
        navController = navController,
        startDestination = "home" // ⬅️ Define tu pantalla inicial (deberías tener una "home" o similar)
    ) {
        // 3. Agregar la ruta 'profile' (Tu PASO 10.1)
        composable("profile") {
            ProfileScreen()
        }

        // 4. Agregar la ruta de inicio (ejemplo: si tienes una pantalla principal)
        composable("home") {
            HomeScreen(navController = navController) // ⬅️ Reemplaza con tu pantalla principal
        }

        // ... otras rutas
    }
}