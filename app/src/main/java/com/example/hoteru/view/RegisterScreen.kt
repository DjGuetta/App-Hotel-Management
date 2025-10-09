package com.example.hoteru.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.viewModel.AuthViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // --- ESTADOS PARA LOS CAMPOS ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) } // Estado para el Checkbox

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState.registrationSuccess) {
            authViewModel.onEventHandled()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registro de Nuevo Usuario", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))

        // --- FORMULARIO ---
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Nombre") }, // Campo para Nombre
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Apellido") }, // Campo para Apellido
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono (Opcional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- SELECTOR DE ROL ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAdmin,
                onCheckedChange = { isAdmin = it }
            )
            Text(
                text = "Es administrador",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                authViewModel.register(firstName, lastName, email, password, phone, isAdmin)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !authState.isLoading
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Registrarse")
            }
        }

        authState.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
