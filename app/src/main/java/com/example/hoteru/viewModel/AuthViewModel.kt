package com.example.hoteru.viewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import com.example.hoteru.model.User
import com.example.hoteru.model.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- Estados para la UI ---
data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: User? = null,
    val registrationSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(password: String): Boolean = password.length >= 6

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (!isValidEmail(email) || !isValidPassword(password)) {
            _authState.update { it.copy(error = "Email o contraseña no válidos.") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val user = MongoDBConnection.findUserByCredentials(email, password)
            _authState.update {
                it.copy(
                    isLoading = false,
                    loginSuccess = user,
                    error = if (user == null) "Credenciales incorrectas." else null
                )
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        isAdmin: Boolean
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.update { it.copy(error = "Nombre, apellido, email y contraseña son requeridos.") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            val newUser = User(
                // El ID se genera por defecto en el modelo
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                userType = if (isAdmin) UserType.Admin else UserType.RegisteredUser // Asigna el rol
            )

            val success = MongoDBConnection.insertUser(newUser, password)
            _authState.update {
                it.copy(
                    isLoading = false,
                    registrationSuccess = success,
                    error = if (!success) "El email ya está en uso o hubo un error." else null
                )
            }
        }
    }
    fun onEventHandled() {
        _authState.update { currentState ->
            currentState.copy(
                isLoading = false,
                error = null,
                registrationSuccess = false
            )
        }
    }
    fun logout() {
        _authState.update { AuthState() }
    }
}
