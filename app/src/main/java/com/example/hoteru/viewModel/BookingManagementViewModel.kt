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

    fun performCheckIn(bookingId: ObjectId, hotelId: String) {
        viewModelScope.launch {
            val success = MongoDBConnection.setBookingCheckIn(bookingId)
            if (success) {
                Log.d(TAG, "Check-In realizado con éxito para la reserva $bookingId.")
                loadActiveBookings(hotelId)
            } else {
                Log.e(TAG, "Error al realizar el Check-In para la reserva $bookingId.")
            }
        }
    }

    fun performCheckOut(bookingId: ObjectId, hotelId: String) {
        viewModelScope.launch {
            val success = MongoDBConnection.setBookingCheckOut(bookingId)
            if (success) {
                Log.d(TAG, "Check-Out realizado con éxito para la reserva $bookingId.")
                loadActiveBookings(hotelId)
            } else {
                Log.e(TAG, "Error al realizar el Check-Out para la reserva $bookingId.")
            }
        }
    }

    // --- LÓGICA DE GUARDADO/CREACIÓN UNIFICADA ---
    fun saveBooking(booking: Booking) {
        viewModelScope.launch {
            // Llama a la nueva función inteligente en MongoDBConnection.
            // Esta función sabe si debe insertar o actualizar.
            val success = MongoDBConnection.saveBooking(booking)
            if (success) {
                Log.d(TAG, "Reserva guardada (creada/actualizada) con éxito. Recargando...")
                // Después de guardar, recargamos la lista para que la UI se actualice.
                loadActiveBookings(booking.hotelId.toString())
            } else {
                Log.e(TAG, "Error al guardar la reserva.")
            }
        }
    }


}
