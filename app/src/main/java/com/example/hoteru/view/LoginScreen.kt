package com.example.hoteru.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.R
import com.example.hoteru.viewModel.AuthViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Usamos el nuevo ViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    // Reacciona al estado de login exitoso
    LaunchedEffect(authState) {
        authState.loginSuccess?.let { user ->
            authViewModel.onEventHandled() // Limpia el estado para evitar re-navegación

            // Todos los usuarios, admin o no, van a "home" después del login
            navController.navigate("home") {
                popUpTo("login") { inclusive = true } // Limpia el historial para no volver al login
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Pasamos el estado y los eventos necesarios
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally) {

            // Header
            HeaderSection()

            Spacer(modifier = Modifier.padding(24.dp))

            LoginFormSection(
                email = email,
                password = password,
                onEmailChanged = { email = it },
                onPasswordChanged = { password = it },
                showError = authState.error
            )

            Spacer(modifier = Modifier.padding(24.dp))

            LoginButtonSection(
                isLoading = authState.isLoading,
                onLoginSelected = {
                    authViewModel.login(email, password)
                }
            )

            Spacer(modifier = Modifier.padding(16.dp))

            GuestOptionSection(
                onGuestSelected = {  },
                onRegisterSelected = { navController.navigate("register") } // <-- Navegación al registro
            )

            //Footer
            FooterSection()
        }
    }
}

@Composable
fun LoginButtonSection(
    isLoading: Boolean,
    onLoginSelected: () -> Unit
) {
    Button(
        onClick = onLoginSelected,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
            Text("Iniciar Sesión")
        }
    }
}

@Composable
fun GuestOptionSection(onGuestSelected: () -> Unit, onRegisterSelected: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Continuar como Invitado",
            modifier = Modifier
                .clickable { onGuestSelected() }
                .padding(8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF011C21)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = buildAnnotatedString {
                append("¿No tienes cuenta? ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Regístrate")
                }
            },
            modifier = Modifier.clickable { onRegisterSelected() },
            fontSize = 12.sp,
            color = Color(0xFF011C21)
        )
    }
}

@Composable
fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.icono),
            contentDescription = "AndeStay",
            modifier = Modifier.height(80.dp)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "AndeStay",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF011C21)
        )

        Text(
            text = "Gestión hotelera inteligente",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.padding(16.dp))

        Text(
            text = "Iniciar Sesión",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF011C21)
        )

        Text(
            text = "Accede a tu panel de gestión hotelera",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun LoginFormSection(
    email: String,
    password: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    showError: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Email Field
        Text(
            text = "Correo Electrónico",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF011C21),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        EmailField(email, onEmailChanged)

        Spacer(modifier = Modifier.padding(12.dp))

        // Password Field
        Text(
            text = "Contraseña",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF011C21),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        PasswordField(password, onPasswordChanged)

        // Error Message
        showError?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))

        // Forgot Password
        ForgotPassword(Modifier.align(Alignment.End))
    }
}

@Composable
fun EmailField(email:String, onTextFieldChanged: (String) -> Unit) {
    TextField(
        value = email,
        onValueChange = {onTextFieldChanged(it)},
        modifier = Modifier.fillMaxWidth(),
        placeholder = {Text(text = "Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.
        colors(Color(0xFF090909),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent)
    )
}
@Composable
fun PasswordField(password:String, onTextFieldChanged: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = {onTextFieldChanged(it)},
        placeholder = { Text(text = "Contraseña") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.
        colors(Color(0xFF090909),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent)
    )
}

@Composable
fun ForgotPassword(modifier: Modifier) {
    Text(text = "Olvidaste tu contraseña?",
        modifier = modifier.clickable{ },
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF011C21)
    )
}

@Composable
fun LoginButton(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(onClick = {onLoginSelected()},
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF011C21),
            disabledContainerColor = Color(0x94011C21),
            contentColor = Color.White,
            disabledContentColor = Color.White
        ), enabled = loginEnable
    ){
        Text(text = "Iniciar Sesión")
    }
}

@Composable
fun FooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = buildAnnotatedString {
                append("Al continuar, aceptas nuestros ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Términos de Servicio")
                }
                append(" y ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Política de Privacidad")
                }
            },
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.padding(16.dp))

        // Feature highlights
        Text(
            text = "Reservas • Servicios • Reseñas • Pagos",
            fontSize = 12.sp,
            color = Color(0xFF011C21),
            fontWeight = FontWeight.Medium
        )
    }
}