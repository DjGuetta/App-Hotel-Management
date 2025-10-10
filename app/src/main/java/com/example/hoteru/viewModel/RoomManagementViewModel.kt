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
import android.util.Base64

class RoomManagementViewModel : ViewModel() {

    // --- StateFlows para la vista global ---
    private val _roomDetailsByHotel = MutableStateFlow<Map<String, List<RoomDetails>>>(emptyMap())
    val roomDetailsByHotel: StateFlow<Map<String, List<RoomDetails>>> = _roomDetailsByHotel.asStateFlow()

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

    // --- FUNCIÓN DE CARGA PRINCIPAL ---
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
    /**
     * Actualiza el estado de la habitación seleccionada.
     * La UI llamará a esto cada vez que un campo del formulario cambie.
     */
    fun updateSelectedRoom(updatedRoom: Room) {
        _selectedRoom.value = updatedRoom
    }

    /**
     * Recibe una lista de imágenes en formato ByteArray, las convierte a Base64
     * y las añade a la habitación que está actualmente seleccionada para edición.
     */
    fun addImages(imageByteArrays: List<ByteArray>) {
        val currentRoom = _selectedRoom.value ?: return

        // La lógica de conversión vive aquí, en el ViewModel.
        val base64Strings = imageByteArrays.map { byteArray ->
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        // Actualizamos el StateFlow. La UI se reconstruirá automáticamente.
        _selectedRoom.value = currentRoom.copy(
            images = currentRoom.images + base64Strings
        )
    }
    fun saveRoom(room: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Determinamos si es una creación o una actualización
                val isUpdate = _roomDetailsByHotel.value.values.flatten().any { it.room._id == room._id }

                val document = room.toDocument() // Asegúrate de que Room tenga una función toDocument()
                val success = if (isUpdate) {
                    MongoDBConnection.updateRoom(room._id, document)
                } else {
                    MongoDBConnection.insertRoom(document)
                }

                if (success) {
                    _toastMessage.value = if (isUpdate) "✅ Habitación actualizada" else "✅ Habitación creada"
                    loadAllRoomsGroupedByHotel() // Recargar la lista global
                    selectRoom(null) // Cierra el diálogo de edición
                } else {
                    _toastMessage.value = if (isUpdate) "❌ Error al actualizar" else "❌ Error al crear"
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
