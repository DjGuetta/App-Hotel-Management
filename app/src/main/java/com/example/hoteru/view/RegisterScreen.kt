package com.example.hoteru.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.R
import com.example.hoteru.viewModel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // --- ESTADOS PARA LOS CAMPOS ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    // --- LÓGICA DE VALIDACIÓN ACTUALIZADA ---
    val isFormValid by remember(firstName, lastName, email, password, confirmPassword, termsAccepted) {
        mutableStateOf(
            firstName.isNotBlank() &&
                    lastName.isNotBlank() &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                    password.length >= 8 &&
                    password == confirmPassword &&
                    termsAccepted
        )
    }

    LaunchedEffect(authState) {
        if (authState.registrationSuccess) {
            authViewModel.onEventHandled()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader()
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Completa los campos para seguir a tu app",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- CAMPO DE NOMBRE Y APELLIDO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = "Nombre",
                                placeholder = "Juan",
                                leadingIcon = Icons.Default.Person
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = "Apellido",
                                placeholder = "Pérez",
                                leadingIcon = Icons.Default.Person
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    RegisterTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo Electrónico",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        isError = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    RegisterTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Teléfono",
                        placeholder = "+58 412-000-0000",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        placeholder = "Mínimo 8 caracteres",
                        isVisible = passwordVisible,
                        onVisibilityChange = { passwordVisible = !passwordVisible },
                        isError = password.isNotEmpty() && password.length < 8
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar Contraseña",
                        placeholder = "Repite tu contraseña",
                        isVisible = confirmPasswordVisible,
                        onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword
                    )
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    TermsAndConditionsCheckbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it }
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // --- LLAMADA AL VIEWMODEL  ---
                            authViewModel.register(firstName, lastName, email, password, phone, false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = isFormValid && !authState.isLoading,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Crear Cuenta", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("¿Ya tienes cuenta? ", color = Color.Gray)
                Text(
                    text = "Inicia Sesión",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            FeaturesFooter()
        }
    }
}

@Composable
private fun AppHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.icono),
            contentDescription = "Logo de la App",
            modifier = Modifier
                .size(80.dp)
                .padding(12.dp),
        )
        Text("HotelHub", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Todo en hoteleria Tachirense", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        isError = isError,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit,
    isError: Boolean = false
) {
    Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Icon(
                    imageVector = if (isVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = "Toggle password visibility"
                )
            }
        },
        singleLine = true,
        isError = isError,
        shape = MaterialTheme.shapes.medium,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
private fun TermsAndConditionsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = buildAnnotatedString {
                append("Acepto los ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                    append("Términos y Condiciones")
                }
                append(" y la ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                    append("Política de Privacidad")
                }
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FeaturesFooter() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FeatureItem(icon = Icons.Default.Shield, text = "Datos Seguros")
        FeatureItem(icon = Icons.Default.VerifiedUser, text = "Verificación")
        FeatureItem(icon = Icons.Default.Shield, text = "Protección")
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ){
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}
