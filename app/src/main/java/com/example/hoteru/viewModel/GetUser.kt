package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import com.example.hoteru.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
// Import for launching coroutines scoped to the ViewModel's lifecycle
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Dispatchers
//import com.google.maps.android.compose.rememberCameraPositionState
// Import for coroutines with a specific dispatcher for background work
// Import for launching coroutines
import kotlinx.coroutines.launch
// Import for StateFlow (a cold asynchronous data stream for observing state changes)
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
// Import for working with MongoDB documents
import org.bson.Document
import kotlin.let
class GetUser : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user





    /**
     * Busca un usuario por su ID y actualiza los flows correspondientes
     */
    fun loadUserById(userId: String?) {
        viewModelScope.launch {
            val result = MongoDBConnection.findUserById(userId)
            println("view model $result")
            if (result != null) {
                _user.value = result
            } else {
                println("usuario no encontrado")
            }

        }
    }
}

