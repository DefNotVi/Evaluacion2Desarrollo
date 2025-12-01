package com.gwagwa.evaluacion2 // Tu paquete principal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gwagwa.evaluacion2.data.remote.RetrofitClient // <-- 1. Importa RetrofitClient
import com.gwagwa.evaluacion2.ui.navigation.AppNavigation
import com.gwagwa.evaluacion2.ui.theme.Evaluacion2Theme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. LLAMA A LA FUNCIÓN 'create' AQUÍ, ANTES DE 'setContent'
        RetrofitClient.create(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setContent {
            Evaluacion2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
