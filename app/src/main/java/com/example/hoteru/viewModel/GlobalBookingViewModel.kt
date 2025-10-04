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
        loadAllActiveBookings()
    }

    fun loadAllActiveBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            MongoDBConnection.getAllActiveBookingDetails()
                .catch { e ->
                    Log.e(TAG, "Error en el flow de detalles de reservas: ${e.message}", e)
                    _isLoading.value = false
                }
                .collect { detailsList ->
                    // Agrupamos las reservas por nombre de hotel para la UI
                    _bookingDetails.value = detailsList.groupBy { it.hotelName }
                    _isLoading.value = false
                    Log.d(TAG, "Reservas agrupadas por hotel: ${_bookingDetails.value.size} hoteles.")
                }
        }
    }

    fun updateBookingStatus(bookingId: ObjectId, newStatus: String) {
        viewModelScope.launch {
            val success = MongoDBConnection.updateBookingStatus(bookingId, newStatus)
            if (success) {
                Log.d(TAG, "Estado de reserva actualizado. Recargando...")
                loadAllActiveBookings() // Recargar toda la lista
            } else {
                Log.e(TAG, "Error al actualizar el estado de la reserva.")
            }
        }
    }
}
