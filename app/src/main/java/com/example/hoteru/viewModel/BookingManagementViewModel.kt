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
                        // Asegurarse de que el loading se quita en caso de error en el Flow
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
                // Simplemente recargamos, la función loadActiveBookings ya maneja el estado de carga
                loadActiveBookings(hotelId)
            } else {
                Log.e(TAG, "Error al actualizar el estado de la reserva.")
            }
        }
    }

    // --- LÓGICA DE CREACIÓN ---
    fun createBooking(booking: Booking) {
        viewModelScope.launch {
            val success = MongoDBConnection.insertBooking(booking)
            if (success) {
                Log.d(TAG, "Reserva creada con éxito. Recargando...")
                // Después de insertar, simplemente llamamos a la función de carga.
                // Esta función se encargará de gestionar el estado de "isLoading" de principio a fin.
                loadActiveBookings(booking.hotelId.toHexString())
            } else {
                Log.e(TAG, "Error al crear la reserva.")
                // Si la creación falla, podríamos querer mostrar un mensaje de error.
            }
        }
    }
}
