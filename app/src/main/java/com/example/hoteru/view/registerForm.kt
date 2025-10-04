package com.example.hoteru.view
// Jetpack Compose core
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.User
import com.example.hoteru.model.UserType
import com.example.hoteru.viewModel.UserLogic

// Coroutines
import kotlinx.coroutines.launch

// Your data classes and local DB
// Make sure to import your User class and UserType enum


// Mongo-style Document
import org.bson.Document

@Composable
fun RegisterScreen(
    navController: NavController
) {
    val userLogic: UserLogic = viewModel()

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register")
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Basic validation
                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                    errorMessage = "Please fill all required fields"
                    return@Button
                }

                // Create new User
                val newUser = User(
                    id = System.currentTimeMillis().toString(),
                    email = email,
                    name = name,
                    phone = phone,
                    userType = UserType.RegisteredUser
                )

                // Call ViewModel's insertUser
                userLogic.insertUser(newUser, password)
                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = Color.Red)
        }
    }
}
