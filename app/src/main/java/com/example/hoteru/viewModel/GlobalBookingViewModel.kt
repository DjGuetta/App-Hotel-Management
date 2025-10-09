package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.BookingDetails
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class GlobalBookingViewModel : ViewModel() {

    private val _bookingDetails = MutableStateFlow<Map<String, List<BookingDetails>>>(emptyMap())
    val bookingDetails: StateFlow<Map<String, List<BookingDetails>>> = _bookingDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "GlobalBookingVM"

    init {
        loadAllBookings()
    }

    // Renombramos la función para que sea más genérica, ya que carga todas las reservas
    fun loadAllBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            // --- CORRECCIÓN AQUÍ ---
            // Llamamos a la función que realmente existe en MongoDBConnection
            MongoDBConnection.getAllBookingDetails()
                .catch { e ->
                    Log.e(TAG, "Error en el flow de detalles de reservas: ${e.message}", e)
                    _isLoading.value = false
                }
                .collect { detailsList ->
                    // Filtramos aquí para mostrar solo las activas, si es necesario,
                    // o puedes ajustar la función en MongoDBConnection.
                    // Por ahora, las agrupamos todas.
                    val activeBookings = detailsList.filter {
                        it.booking.status == "CONFIRMED" || it.booking.status == "CHECKED_IN"
                    }
                    _bookingDetails.value = activeBookings.groupBy { it.hotelName }
                    _isLoading.value = false
                    Log.d(TAG, "Reservas agrupadas por hotel: ${_bookingDetails.value.size} hoteles.")
                }
        }
    }

    fun updateBookingStatus(bookingId: ObjectId, newStatus: String) {
        viewModelScope.launch {
            // Esta llamada es correcta, asumiendo que la función en MongoDBConnection está bien definida.
            val success = MongoDBConnection.updateBookingStatus(bookingId, newStatus)
            if (success) {
                Log.d(TAG, "Estado de reserva actualizado. Recargando...")
                loadAllBookings() // Recargar toda la lista
            } else {
                Log.e(TAG, "Error al actualizar el estado de la reserva.")
            }
        }
    }
}
