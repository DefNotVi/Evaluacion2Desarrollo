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
import com.gwagwa.evaluacion2.ui.createPackages.CreatePackageScreen
import com.gwagwa.evaluacion2.ui.login.LoginScreen
import com.gwagwa.evaluacion2.ui.login.RegisterScreen
import com.gwagwa.evaluacion2.ui.packageList.AdminPackageScreen
import com.gwagwa.evaluacion2.ui.packageList.PackageListScreen
import com.gwagwa.evaluacion2.ui.profile.ProfileScreen
import com.gwagwa.evaluacion2.ui.profile.EditProfileScreen
import com.gwagwa.evaluacion2.ui.userList.UserListScreen
import com.gwagwa.evaluacion2.viewmodel.LoginRegisterViewModel


object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PACKAGE_LIST = "package_list"
    const val PROFILE = "profile"
    const val USER_LIST = "user_list"
    const val ADMIN_PACKAGE_LIST = "admin_package_list"
    const val CREATE_PACKAGE = "create_package"
    const val EDIT_PROFILE = "edit_profile"
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
            val destination = when (authState.userRole) {
                "ADMIN" -> AppDestinations.ADMIN_PACKAGE_LIST // si es Admin, va a su pantalla
                else -> AppDestinations.PACKAGE_LIST           // si es Cliente, a la suya
            }
            navController.navigate(destination) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
            // CORRECCIÓN: Eliminamos la llamada a resetAuthSuccess() que no existe.
        }
    }

    //    EFECTO PARA COMPROBAR SI EL USUARIO YA ESTÁ LOGUEADO AL INICIAR
    LaunchedEffect(Unit) {
        authViewModel.checkIfUserIsLoggedIn()
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN // Siempre empezamos en Login
    ) {
        // --- Pantalla de Login ---
        composable(AppDestinations.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER)
                }
            )
        }

        // --- Pantalla de Registro ---
        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- Pantalla de Lista de Paquetes ---
        composable(AppDestinations.PACKAGE_LIST) {
            PackageListScreen(
                onPackageClick = { packageId ->
                },
                onLogout = {
                    // Limpiar el estado del ViewModel y navegar de vuelta a Login
                    authViewModel.resetState() // Usamos la función que SÍ existe
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(AppDestinations.PROFILE)
                }
            )
        }

        // --- Pantalla de Perfil ---
        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(AppDestinations.EDIT_PROFILE)
                }
            )
        }

        // --- Pantalla Principal del Admin ---
        composable(AppDestinations.ADMIN_PACKAGE_LIST) {
            AdminPackageScreen(
                onManageUsersClick = {
                    navController.navigate(AppDestinations.USER_LIST)
                },
                onCreatePackageClick = {
                    navController.navigate(AppDestinations.CREATE_PACKAGE)
                },
                onLogout = {
                    authViewModel.resetState() // CORRECCIÓN: Usamos la función que SÍ existe
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA DE CREACION DE PAQUETES
        composable(AppDestinations.CREATE_PACKAGE) {
            CreatePackageScreen(
                onNavigateBack = {
                    // Cuando se completa la creación o el usuario quiere volver,
                    // lo lleva de vuelta al dashboard del admin.
                    navController.popBackStack()
                }
            )
        }


        composable(AppDestinations.USER_LIST) {
            UserListScreen(
                onNavigateBack = {
                    navController.popBackStack() // vuelve al panel de admin
                }
            )
        }

        // PANTALLA DE EDICIÓN DE PERFIL
        composable(AppDestinations.EDIT_PROFILE) {
            // Nota: Usamos el mismo ProfileViewModel si queremos compartir datos
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProfileUpdated = {
                    // Cuando el guardado es exitoso, se vuelve a la pantalla de vista
                    navController.popBackStack()
                }
            )
        }
    }
}
