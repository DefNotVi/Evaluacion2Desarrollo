package com.gwagwa.evaluacion2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gwagwa.evaluacion2.ui.login.LoginScreen
import com.gwagwa.evaluacion2.ui.login.RegisterScreen
import com.gwagwa.evaluacion2.ui.dashboard.DashboardScreen
import com.gwagwa.evaluacion2.ui.profile.ProfileScreen

// Rutas de la aplicación
object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN // Empieza en Login
    ) {
        // --- 1. Login ---
        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.DASHBOARD) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(AppDestinations.REGISTER) }
            )
        }

        // --- 2. Registro ---
        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onRegistrationSuccess = {
                    navController.popBackStack() // Vuelve a Login
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- 3. Dashboard (Pantalla Principal) ---
        composable(AppDestinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToProfile = { navController.navigate(AppDestinations.PROFILE) },
                onLogout = {
                    // Al cerrar sesión, vuelve a Login
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        // --- 4. Perfil ---
        composable(AppDestinations.PROFILE) {
            ProfileScreen()
        }
    }
}