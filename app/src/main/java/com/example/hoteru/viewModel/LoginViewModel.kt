package com.example.hoteru.viewModel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hoteru.model.User
import com.example.hoteru.model.UserType
import kotlinx.coroutines.delay

class LoginViewModel : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _loginEnable = MutableLiveData<Boolean>()
    val loginEnable: LiveData<Boolean> = _loginEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _navigateToAdmin = MutableLiveData<Boolean>()
    val navigateToAdmin: LiveData<Boolean> = _navigateToAdmin

    private val _navigateToGuest = MutableLiveData<Boolean>()
    val navigateToGuest: LiveData<Boolean> = _navigateToGuest

    private val _showError = MutableLiveData<String?>()
    val showError: LiveData<String?> = _showError

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        _navigateToAdmin.value = false
        _navigateToGuest.value = false
    }

    fun onLoginChanged(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(password: String): Boolean =
        password.length > 6

    suspend fun onLoginSelected() {
        _isLoading.value = true
        delay(2000) // Simulación de llamada API

        // Simulación de login exitoso (siempre admin por ahora)
        val user = User(
            id = "1",
            email = _email.value ?: "",
            name = "Administrador",
            userType = UserType.Admin,
            hotelId = "hotel_123"
        )

        _currentUser.value = user
        _isLoading.value = false
        _navigateToAdmin.value = true
    }

    fun onGuestSelected() {
        _navigateToGuest.value = true
    }

    fun onNavigationCompleted() {
        _navigateToAdmin.value = false
        _navigateToGuest.value = false
        _showError.value = null
    }
}