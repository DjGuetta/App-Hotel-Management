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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.example.hoteru.viewModel.LoginViewModel
import com.example.hoteru.viewModel.UserLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {

    val navigateToAdmin by viewModel.navigateToAdmin.observeAsState(false)
    val navigateToGuest by viewModel.navigateToGuest.observeAsState(false)

    LaunchedEffect(navigateToAdmin) {
        if (navigateToAdmin) {
            navController.navigate("admin_dashboard") {
                popUpTo("login") { inclusive = true }
            }
            viewModel.onNavigationCompleted()
        }
    }

    LaunchedEffect(navigateToGuest) {
        if (navigateToGuest) {
            navController.navigate("guest_dashboard") {
                popUpTo("login") { inclusive = true }
            }
            viewModel.onNavigationCompleted()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LoginContent(Modifier.align(Alignment.Center), viewModel, navController )
    }
}

@Composable
fun LoginContent(modifier: Modifier, viewModel: LoginViewModel, navController: NavController) {
    val email: String by viewModel.email.observeAsState("")
    val password: String by viewModel.password.observeAsState("")
    val loginEnable: Boolean by viewModel.loginEnable.observeAsState(false)
    val isLoading: Boolean by viewModel.isLoading.observeAsState(false)
    val showError: String? by viewModel.showError.observeAsState(null)
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // Header
        HeaderSection()

        Spacer(modifier = Modifier.padding(24.dp))

        // Login Form
        LoginFormSection(
            email = email,
            password = password,
            onEmailChanged = { viewModel.onLoginChanged(it, password) },
            onPasswordChanged = { viewModel.onLoginChanged(email, it) },
            showError = showError,
            navController
        )

        Spacer(modifier = Modifier.padding(16.dp))

        // Login Button
//        LoginButtonSection(
//            loginEnable = loginEnable,
//            isLoading = isLoading,
//            onLoginSelected = {
//                coroutineScope.launch {
//                    viewModel.onLoginSelected()
//                }
//            }
//        )

        Spacer(modifier = Modifier.padding(16.dp))

        // Guest Option
        GuestOptionSection(
            onGuestSelected = { viewModel.onGuestSelected() }
        )

        Spacer(modifier = Modifier.padding(24.dp))

        // Footer
        FooterSection()
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
            contentDescription = "HotelHub Logo",
            modifier = Modifier.height(80.dp)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "HotelHub",
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
    showError: String?,
    navController: NavController
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

        LoginButtonSection(email, password, navController )

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
fun LoginButton(
    email: String,
    password: String,
    coroutineScope: CoroutineScope,
    userLogic: UserLogic
) {
    Button(
        onClick = {
            coroutineScope.launch {
                userLogic.getUser(email, password)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF011C21),
            disabledContainerColor = Color(0x94011C21),
            contentColor = Color.White,
            disabledContentColor = Color.White
        )
    ){
        Text(text = "Iniciar Sesión")
    }
}

@Composable
fun LoginButtonSection(
    email: String, password: String, navController: NavController
) {
//    if (isLoading) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(48.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator(color = Color.White)
//        }
//    } else {
    val userLogic: UserLogic = viewModel()
    val isValid by userLogic.validUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Navigate when login is valid
    LaunchedEffect(isValid) {
        if (isValid == true) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
            // Reset state after navigation to avoid multiple triggers
            userLogic.resetValidUser()
        }
    }

    LoginButton(email, password, coroutineScope, userLogic)
//    }
}

@Composable
fun GuestOptionSection(onGuestSelected: () -> Unit) {
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
            modifier = Modifier.clickable { /* Navigate to register */ },
            fontSize = 12.sp,
            color = Color(0xFF011C21)
        )
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