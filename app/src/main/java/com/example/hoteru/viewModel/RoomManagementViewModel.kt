package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import com.example.hoteru.model.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class RoomManagementViewModel : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _selectedRoom = MutableStateFlow<Room?>(null)
    val selectedRoom: StateFlow<Room?> = _selectedRoom

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentHotelId: ObjectId? = null

    private val TAG = "RoomManagementViewModel"

    // Cargar habitaciones por hotel desde MongoDB
    fun loadRoomsByHotel(hotelId: ObjectId) {
        currentHotelId = hotelId
        viewModelScope.launch {
            _isLoading.value = true
            try {

                // en lugar de collect() que se queda esperando para siempre
                val roomsList = MongoDBConnection.getRoomsByHotel(hotelId).first()
                _rooms.value = roomsList
                _errorMessage.value = null
                Log.d(TAG, "✅ Habitaciones cargadas: ${roomsList.size} para hotel: $hotelId")
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar habitaciones: ${e.message}"
                _rooms.value = emptyList()
                Log.e(TAG, "❌ Error cargando habitaciones: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crear nueva habitación en MongoDB
    fun createRoom(room: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = MongoDBConnection.insertRoom(room.toDocument())
                if (success) {
                    // Recargar la lista después de crear
                    currentHotelId?.let { loadRoomsByHotel(it) }
                    _errorMessage.value = "Habitación creada exitosamente"
                    Log.d(TAG, "✅ Habitación creada: ${room.roomNumber}")
                } else {
                    _errorMessage.value = "Error al crear habitación en la base de datos"
                    Log.e(TAG, "❌ Error creando habitación")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear habitación: ${e.message}"
                Log.e(TAG, "❌ Error creando habitación: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Actualizar habitación en MongoDB
    fun updateRoom(updatedRoom: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = MongoDBConnection.updateRoom(updatedRoom._id, updatedRoom.toDocument())
                if (success) {
                    // Recargar la lista después de actualizar
                    currentHotelId?.let { loadRoomsByHotel(it) }
                    _errorMessage.value = "Habitación actualizada exitosamente"
                    Log.d(TAG, "✅ Habitación actualizada: ${updatedRoom.roomNumber}")
                } else {
                    _errorMessage.value = "Error al actualizar habitación en la base de datos"
                    Log.e(TAG, "❌ Error actualizando habitación")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar habitación: ${e.message}"
                Log.e(TAG, "❌ Error actualizando habitación: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRoomStatus(roomId: ObjectId, newStatus: String) {
        viewModelScope.launch {
            try {
                val success = MongoDBConnection.updateRoomStatus(roomId, newStatus)

                if (success) {
                    // Actualizar lista local
                    val updatedList = _rooms.value.map { room ->
                        if (room._id == roomId) {
                            room.copy(status = newStatus)
                        } else {
                            room
                        }
                    }
                    _rooms.value = updatedList
                    _errorMessage.value = "Estado de la habitación actualizado"
                    Log.d(TAG, "✅ Estado actualizado a: $newStatus para habitación: $roomId")
                } else {
                    _errorMessage.value = "Error al actualizar el estado"
                    Log.e(TAG, "❌ Error actualizando estado")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e(TAG, "❌ Error actualizando estado: ${e.message}")
            }
        }
    }

    // Eliminar habitación de MongoDB
    fun deleteRoom(roomId: ObjectId) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = MongoDBConnection.deleteRoom(roomId)
                if (success) {
                    // Recargar la lista después de eliminar
                    currentHotelId?.let { loadRoomsByHotel(it) }
                    _errorMessage.value = "Habitación eliminada exitosamente"
                    Log.d(TAG, "✅ Habitación eliminada: $roomId")
                } else {
                    _errorMessage.value = "Error al eliminar habitación de la base de datos"
                    Log.e(TAG, "❌ Error eliminando habitación")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar habitación: ${e.message}"
                Log.e(TAG, "❌ Error eliminando habitación: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Seleccionar habitación para editar
    fun selectRoom(room: Room?) {
        _selectedRoom.value = room
        Log.d(TAG, "Habitación seleccionada: ${room?.roomNumber}")
    }

    // Limpiar mensajes de error
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}