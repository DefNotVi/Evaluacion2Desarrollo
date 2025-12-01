package com.gwagwa.evaluacion2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gwagwa.evaluacion2.ui.login.LoginScreen
import com.gwagwa.evaluacion2.ui.login.RegisterScreen
import com.gwagwa.evaluacion2.ui.packageList.PackageListScreen
import com.gwagwa.evaluacion2.ui.profile.ProfileScreen
import com.gwagwa.evaluacion2.viewmodel.LoginRegisterViewModel

object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PACKAGE_LIST = "package_list"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    //    INICIALIZA EL VIEWMODEL
    val authViewModel: LoginRegisterViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    //    EFECTO PARA MANEJAR LA NAVEGACIÓN BASADA EN EL ESTADO DE AUTENTICACIÓN
    LaunchedEffect(authState.authSuccess) {
        if (authState.authSuccess) {
            navController.navigate(AppDestinations.PACKAGE_LIST) {
                // Limpia todo el backstack hasta el inicio del grafo para que el usuario
                // no pueda volver a la pantalla de login con el botón de "atrás".
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }
    }

    //    EFECTO PARA COMPROBAR SI EL USUARIO YA ESTÁ LOGUEADO AL INICIAR
    //    Se ejecuta solo una vez cuando el NavHost se compone por primera vez
    LaunchedEffect(Unit) {
        authViewModel.checkIfUserIsLoggedIn()
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN // Siempre empezamos en Login
    ) {
        // --- Pantalla de Login ---
        composable(AppDestinations.LOGIN) {
            // Le pasamos el ViewModel compartido a la pantalla de Login
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER)
                }
                // Ya no necesito onLoginSuccess, el `LaunchedEffect` se encarga de eso
            )
        }

        // --- Pantalla de Registro ---
        composable(AppDestinations.REGISTER) {
            // Le pasamos el MISMO ViewModel a la pantalla de Registro
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
                // Ya no necesito el onRegistrationSuccess
            )
        }

        // --- Pantalla de Lista de Paquetes ---
        composable(AppDestinations.PACKAGE_LIST) {
            PackageListScreen(
                onPackageClick = { packageId ->
                    // Futuro: navegar al detalle del paquete
                },
                onLogout = {
                    // Limpiar el estado del ViewModel y navegar de vuelta a Login
                    authViewModel.resetState()
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                //
                onProfileClick = {
                    navController.navigate(AppDestinations.PROFILE)
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
