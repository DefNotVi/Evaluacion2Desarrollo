package com.gwagwa.evaluacion2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.gwagwa.evaluacion2.ui.navigation.AppNavigation // ⬅️ Importa AppNavigation
import com.gwagwa.evaluacion2.ui.theme.Evaluacion2Theme // ⬅️ Tu tema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Obtiene el controlador de navegación
            val navController = rememberNavController()

            Evaluacion2Theme { // ⬅️ Tu tema principal
                // 2. Llama al NavHost que maneja la navegación
                // Ya que AppNavigation ya contiene rememberNavController(),
                // puedes simplificarlo si lo deseas, o mantenerlo así:
                AppNavigation()
            }
        }
    }
}