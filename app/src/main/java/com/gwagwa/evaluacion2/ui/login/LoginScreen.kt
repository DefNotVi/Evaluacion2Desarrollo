package com.gwagwa.evaluacion2.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gwagwa.evaluacion2.viewmodel.LoginRegisterViewModel


@Composable
fun LoginScreen(
    // AHORA RECIBE EL VIEWMODEL COMPLETO
    viewModel: LoginRegisterViewModel,
    onNavigateToRegister: () -> Unit
) {
    // SE OBTENE EL ESTADO DIRECTAMENTE DEL VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Email
        OutlinedTextField(
            value = uiState.email,
            // LLAMA AL EVENTO DEL VIEWMODEL
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = uiState.password,
            //  LLAMA AL EVENTO DEL VIEWMODEL
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.error != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Muestra el mensaje de error si existe
        uiState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón de Login
        Button(
            // LLAMA A LA FUNCIÓN DE LOGIN DEL VIEWMODEL
            onClick = { viewModel.login() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading // Deshabilitar mientras carga
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }

        // Botón para ir a Registro
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes una cuenta? Regístrate")
        }
    }
}
