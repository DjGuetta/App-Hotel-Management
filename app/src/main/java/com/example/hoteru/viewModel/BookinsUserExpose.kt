package com.example.hoteru.viewModel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.Booking
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class BookinsUserExpose : ViewModel() {

    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- Method to fetch bookings for a specific user ---
    fun loadBookingsByUser(userId: String?) {
        if (userId.isNullOrBlank()) {
            _userBookings.value = emptyList()
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val bookings = MongoDBConnection.findBookingsByUserId(userId)
                _userBookings.value = bookings
            } catch (e: Exception) {
                _error.value = "Error al cargar reservas: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
