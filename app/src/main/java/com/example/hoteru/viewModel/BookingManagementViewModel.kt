// En viewModel/BookingManagementViewModel.kt
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

class BookingManagementViewModel : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "BookingViewModel"

    fun loadActiveBookings(hotelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val objectId = ObjectId(hotelId)
                MongoDBConnection.getActiveBookingsByHotel(objectId)
                    .catch { e ->
                        Log.e(TAG, "Error en el flow de reservas: ${e.message}", e)
                        _isLoading.value = false
                    }
                    .collect { bookingList ->
                        _bookings.value = bookingList
                        _isLoading.value = false
                        Log.d(TAG, "Reservas activas cargadas: ${bookingList.size}")
                    }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Hotel ID inválido: $hotelId", e)
                _isLoading.value = false
            }
        }
    }

    fun updateBookingStatus(bookingId: ObjectId, newStatus: String, hotelId: String) {
        viewModelScope.launch {
            val success = MongoDBConnection.updateBookingStatus(bookingId, newStatus)
            if (success) {
                Log.d(TAG, "Estado de reserva actualizado con éxito.")
                // Recargar la lista para reflejar el cambio
                loadActiveBookings(hotelId)
            } else {
                Log.e(TAG, "Error al actualizar el estado de la reserva.")
            }
        }
    }
}
