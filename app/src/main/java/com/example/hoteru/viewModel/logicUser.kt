package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import com.example.hoteru.model.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.bson.Document
import kotlin.collections.forEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class UserLogic : ViewModel() {
    private val _validUser = MutableStateFlow<Boolean?>(null)
    val validUser: StateFlow<Boolean?> = _validUser

    fun insertUser(user: User, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Call your singleton function
            MongoDBConnection.insertUserDb(user, password)

            // Return the result to the main thread

        }
    }

    fun getUser(email: String, password: String){
        viewModelScope.launch(Dispatchers.IO) {
            // Call your singleton function
            val validUser = MongoDBConnection.consultUserDb(email, password)
            if (validUser){
                _validUser.value = true
            }

            // Return the result to the main thread

        }
    }

    fun resetValidUser() {
        _validUser.value = null
    }


}