package com.example.hoteru.viewModel

import android.util.Log
import com.example.hoteru.model.Hotel
import androidx.compose.foundation.layout.size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import com.example.hoteru.model.Room
import com.example.hoteru.model.RoomDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class RoomManagementViewModel : ViewModel() {

    // --- NUEVOS StateFlows para la vista global ---
    private val _roomDetailsByHotel = MutableStateFlow<Map<String, List<RoomDetails>>>(emptyMap())
    val roomDetailsByHotel: StateFlow<Map<String, List<RoomDetails>>> = _roomDetailsByHotel.asStateFlow()

    // --- StateFlows existentes (aún útiles para diálogos de edición/creación) ---
    private val _selectedRoom = MutableStateFlow<Room?>(null)
    val selectedRoom: StateFlow<Room?> = _selectedRoom.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val TAG = "RoomManagementViewModel"
    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    init {
        loadAllData() // Cambiamos la función inicial para que cargue todo
    }

    // --- NUEVA FUNCIÓN DE CARGA PRINCIPAL ---
    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Carga tanto las habitaciones como la lista de hoteles en paralelo
            launch { loadAllRoomsGroupedByHotel() }
            launch { loadHotels() }
        }
    }
    private fun loadHotels() {
        viewModelScope.launch {
            try {
                _hotels.value = MongoDBConnection.getHotels().first()
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando la lista de hoteles: ${e.message}")
                _toastMessage.value = "Error al obtener la lista de hoteles."
            }
        }
    }

    // Carga todas las habitaciones de todos los hoteles y las agrupa.
    fun loadAllRoomsGroupedByHotel() {
        viewModelScope.launch {
            _isLoading.value = true
            MongoDBConnection.getAllRoomDetails()
                .catch { e ->
                    Log.e(TAG, "Error en el flow de detalles de habitaciones: ${e.message}", e)
                    _toastMessage.value = "Error cargando habitaciones: ${e.message}"
                    _isLoading.value = false
                }
                .collect { detailsList ->
                    // Agrupamos las habitaciones por el nombre del hotel
                    _roomDetailsByHotel.value = detailsList.groupBy { it.hotelName }
                    _isLoading.value = false
                    Log.d(TAG, "Habitaciones agrupadas para ${_roomDetailsByHotel.value.size} hoteles.")
                }
        }
    }

    // --- FUNCIONES CRUD ADAPTADAS ---
    // Ahora, después de cada operación, recargamos la lista global.

    fun createRoom(room: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = MongoDBConnection.insertRoom(room.toDocument())
                if (success) {
                    _toastMessage.value = "✅ Habitación creada exitosamente"
                    loadAllRoomsGroupedByHotel() // Recargar la lista global
                } else {
                    _toastMessage.value = "❌ Error al crear habitación"
                }
            } catch (e: Exception) {
                _toastMessage.value = "💥 Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRoom(updatedRoom: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = MongoDBConnection.updateRoom(updatedRoom._id, updatedRoom.toDocument())
                if (success) {
                    _toastMessage.value = "✅ Habitación actualizada"
                    loadAllRoomsGroupedByHotel() // Recargar la lista global
                } else {
                    _toastMessage.value = "❌ Error al actualizar habitación"
                }
            } catch (e: Exception) {
                _toastMessage.value = "💥 Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRoom(roomId: ObjectId) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = MongoDBConnection.deleteRoom(roomId)
                if (success) {
                    _toastMessage.value = "✅ Habitación eliminada"
                    loadAllRoomsGroupedByHotel() // Recargar la lista global
                } else {
                    _toastMessage.value = "❌ Error al eliminar habitación"
                }
            } catch (e: Exception) {
                _toastMessage.value = "💥 Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRoomStatus(roomId: ObjectId, newStatus: String) {
        viewModelScope.launch {
            val success = MongoDBConnection.updateRoomStatus(roomId, newStatus)
            if (success) {
                _toastMessage.value = "✅ Estado actualizado"
                // Para una actualización de estado, es más eficiente recargar
                loadAllRoomsGroupedByHotel()
            } else {
                _toastMessage.value = "❌ Error al actualizar el estado"
            }
        }
    }

    // Seleccionar habitación para editar
    fun selectRoom(room: Room?) {
        _selectedRoom.value = room
    }

    // Limpiar mensajes de error/toast
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // --- MANTENEMOS LAS FUNCIONES ANTIGUAS POR SI SON NECESARIAS EN OTRO LADO ---
    // Pero la pantalla principal ya no las usará.
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()
    private var currentHotelId: ObjectId? = null

    fun loadRoomsByHotel(hotelId: ObjectId) {
        currentHotelId = hotelId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val roomsList = MongoDBConnection.getRoomsByHotel(hotelId).first()
                _rooms.value = roomsList
                Log.d(TAG, "✅ Habitaciones cargadas: ${roomsList.size} para hotel: $hotelId")
            } catch (e: Exception) {
                _toastMessage.value = "Error al cargar habitaciones: ${e.message}"
                _rooms.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
