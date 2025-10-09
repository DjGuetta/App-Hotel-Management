package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

// ViewModel para la lógica del formulario de creación/edición de reservas
class BookingFormViewModel : ViewModel() {

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    private val _availableRooms = MutableStateFlow<List<Room>>(emptyList())
    val availableRooms: StateFlow<List<Room>> = _availableRooms.asStateFlow()

    // Para cargar datos al editar
    val initialHotel = MutableStateFlow<Hotel?>(null)
    val initialRoom = MutableStateFlow<Room?>(null)

    init {
        // Cargar la lista de todos los hoteles cuando el ViewModel se inicia
        viewModelScope.launch {
            MongoDBConnection.getHotels().collect { hotelList ->
                _hotels.value = hotelList
            }
        }
    }

    // Carga las habitaciones DISPONIBLES de un hotel específico
    fun loadAvailableRoomsForHotel(hotelId: ObjectId) {
        viewModelScope.launch {
            MongoDBConnection.getRoomsByHotel(hotelId).collect { roomList ->
                _availableRooms.value = roomList.filter { it.status.equals("DISPONIBLE", ignoreCase = true) }
            }
        }
    }

    // Carga los datos iniciales cuando se edita una reserva existente
    fun loadDataForEditing(booking: Booking) {
        viewModelScope.launch {
            // Usamos la función que existe en MongoDBConnection.
            // Esta función devuelve un Flow, por lo que usamos .firstOrNull() para obtener el primer valor.
            MongoDBConnection.getHotelWithRoomStats(booking.hotelId).firstOrNull()?.let { pair ->
                val hotel = pair.first
                initialHotel.value = hotel

                // Cargar la habitación de la reserva (incluso si no está disponible, para mostrarla)
                MongoDBConnection.getRoomsByHotel(booking.hotelId).firstOrNull()?.let { allRooms ->
                    initialRoom.value = allRooms.find { room -> room._id == booking.roomId }
                }
            }
        }
    }
}
