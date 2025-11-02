package com.gwagwa.evaluacion2.ui.navigation

composable("profile") {
    ProfileScreen()
}

Button(onClick = { navController.navigate("profile") }) {
    Text("Ver Perfil")
}