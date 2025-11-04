package com.gwagwa.evaluacion2 // Tu paquete principal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gwagwa.evaluacion2.ui.navigation.AppNavigation
import com.gwagwa.evaluacion2.ui.theme.Evaluacion2Theme // Asegúrate que el nombre de tu tema sea correcto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Asegúrate de usar el tema que tienes definido
            Evaluacion2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // ⬅️ ¡Tu app arranca aquí!
                }
            }
        }
    }

}

