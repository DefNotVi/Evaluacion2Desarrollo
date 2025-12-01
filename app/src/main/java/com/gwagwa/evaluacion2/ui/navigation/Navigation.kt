package com.gwagwa.evaluacion2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gwagwa.evaluacion2.ui.Paquetes.PackageListScreen
import com.gwagwa.evaluacion2.ui.login.LoginScreen
import com.gwagwa.evaluacion2.ui.login.RegisterScreen
import com.gwagwa.evaluacion2.ui.profile.ProfileScreen
import com.gwagwa.evaluacion2.viewmodel.PackageListViewModel

// Rutas de la aplicación (esto se queda igual)
object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PACKAGE_LIST = "package_list"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // NO creamos el ViewModel aquí

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN
    ) {
        // --- Pantalla de Login ---
        composable(AppDestinations.LOGIN) {
            // LoginScreen se encargará de crear su propio ViewModel.
            // Solo le decimos qué hacer cuando el login sea exitoso.
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.PACKAGE_LIST) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER)
                }
            )
        }

        // --- Pantalla de Registro ---
        composable(AppDestinations.REGISTER) {
            // RegisterScreen también manejará su propio ViewModel.
            RegisterScreen(
                onRegistrationSuccess = {
                    // Si el registro es exitoso, volvemos a Login.
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- Pantalla de Lista de Paquetes ---
        composable(AppDestinations.PACKAGE_LIST) {
            // PackageListScreen usa su propio ViewModel, que también maneja el logout.
            PackageListScreen(
                onPackageClick = { packageId ->
                    // Futuro: navegar al detalle del paquete
                },
                onLogout = {
                    // Navega de vuelta a Login y limpia el backstack.
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.PACKAGE_LIST) { inclusive = true }
                    }
                }
            )
        }

        // --- Pantalla de Perfil ---
        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
