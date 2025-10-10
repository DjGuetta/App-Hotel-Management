package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.bson.Document
import org.bson.types.ObjectId

class HotelManagementViewModel : ViewModel() {

    private val _hotelUiState = MutableLiveData<HotelUiState>()
    val hotelUiState: LiveData<HotelUiState> = _hotelUiState

    private val _selectedHotel = MutableLiveData<Hotel?>()
    val selectedHotel: LiveData<Hotel?> = _selectedHotel

    private val _isEditing = MutableLiveData<Boolean>()
    val isEditing: LiveData<Boolean> = _isEditing

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _hotelsWithStats = MutableLiveData<List<Pair<Hotel, RoomStats>>>()
    val hotelsWithStats: LiveData<List<Pair<Hotel, RoomStats>>> = _hotelsWithStats

    private val TAG = "HotelManagementViewModel"

    init {
        loadHotelsWithStats()
    }

    // Cargar hoteles con estadísticas en tiempo real
    fun loadHotelsWithStats() {
        viewModelScope.launch {
            _hotelUiState.value = HotelUiState.Loading
            try {
                // Recolectar continuamente el Flow
                MongoDBConnection.getHotelsWithRoomStats().collect { hotelsWithStats ->
                    _hotelsWithStats.value = hotelsWithStats

                    if (hotelsWithStats.isEmpty()) {
                        _hotelUiState.value = HotelUiState.Empty
                    } else {
                        val hotels = hotelsWithStats.map { it.first }
                        _hotelUiState.value = HotelUiState.Success(hotels)
                        Log.d(TAG, "Hoteles cargados: ${hotels.size}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error con stream reactivo: ${e.message}")
                _hotelUiState.value = HotelUiState.Error("Error cargando hoteles: ${e.message}")
                // Fallback: cargar solo hoteles sin estadísticas
                loadHotelsFallback()
            }
        }
    }
    // Carga los datos de un único hotel por su ID y los publica en _selectedHotel.
    fun loadHotelById(hotelId: ObjectId) {
        viewModelScope.launch {
            try {
                // Usamos la función de Flow que ya existe para obtener los datos.
                // firstOrNull() toma solo el primer valor emitido por el Flow.
                val hotelData = MongoDBConnection.getHotelWithRoomStats(hotelId).firstOrNull()
                _selectedHotel.value = hotelData?.first
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando el hotel por ID: $hotelId", e)
                _toastMessage.value = "Error al cargar los datos del hotel."
            }
        }
    }


    // Fallback en caso de error con el stream reactivo
    private fun loadHotelsFallback() {
        viewModelScope.launch {
            try {
                _hotelUiState.value = HotelUiState.Loading
                // Método simple sin Flows
                val hotels = getHotelsSync()

                if (hotels.isEmpty()) {
                    _hotelUiState.value = HotelUiState.Empty
                } else {
                    _hotelUiState.value = HotelUiState.Success(hotels)
                    // Cargar estadísticas por separado
                    loadRoomsForStats(hotels)
                }
            } catch (e: Exception) {
                _hotelUiState.value = HotelUiState.Error("Error cargando hoteles: ${e.message}")
            }
        }
    }

    // Método síncrono para obtener hoteles
    private suspend fun getHotelsSync(): List<Hotel> {
        return try {
            var hotelsList = emptyList<Hotel>()
            MongoDBConnection.getHotels().collect { hotels ->
                hotelsList = hotels
            }
            hotelsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Cargar habitaciones para calcular estadísticas (modo fallback)
    private suspend fun loadRoomsForStats(hotels: List<Hotel>) {
        try {
            var allRooms = emptyList<Room>()
            MongoDBConnection.getAllRooms().collect { rooms ->
                allRooms = rooms
            }

            val hotelsWithStats = hotels.map { hotel ->
                val hotelRooms = allRooms.filter { it.hotelId == hotel._id }
                val stats = hotel.getRoomStats(hotelRooms)
                hotel to stats
            }
            _hotelsWithStats.value = hotelsWithStats
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando habitaciones para estadísticas: ${e.message}")
        }
    }

    // Función para obtener estadísticas actualizadas de un hotel
    fun getCurrentRoomStats(hotelId: ObjectId): RoomStats? {
        return _hotelsWithStats.value
            ?.find { it.first._id == hotelId }
            ?.second
    }

    // Función para actualizar el contador de habitaciones cuando cambia el estado
    fun updateHotelRoomCount(hotelId: ObjectId) {
        viewModelScope.launch {
            try {
                // Forzar recarga de estadísticas
                loadHotelsWithStats()
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando contador de habitaciones: ${e.message}")
            }
        }
    }

    // Función para obtener hoteles de manera tradicional (sin estadísticas)
    private suspend fun getAllHotelsFromMongoDB(): List<Hotel> {
        return try {
            var hotelsList = emptyList<Hotel>()
            MongoDBConnection.getHotels().collect { hotels ->
                hotelsList = hotels
            }
            hotelsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Esta función local puede ser eliminada si la de MongoDBConnection ya es suficiente.
    private fun documentToHotel(document: Document): Hotel {
        // ... (esta función ya no es necesaria aquí si MongoDBConnection la tiene)
        return Hotel(name = "Deprecated")
    }

    // Esta función local puede ser eliminada.
    private fun getLocationFromDocument(document: Document): Location {
        // ... (esta función ya no es necesaria aquí si MongoDBConnection la tiene)
        return Location()
    }

    // <<< 2. FUNCIÓN ACTUALIZADA >>>
    private fun hotelToDocument(hotel: Hotel): Document {
        // Obtener estadísticas actuales si están disponibles
        val currentStats = getCurrentRoomStats(hotel._id)
        val finalAvailableRooms = currentStats?.available ?: hotel.availableRooms

        return Document().apply {
            put("name", hotel.name)
            put("address", hotel.address)
            put("city", hotel.city)
            put("state", hotel.state)
            put("location", Document().apply {
                put("type", "Point")
                put("coordinates", listOf(hotel.location.longitude, hotel.location.latitude))
            })
            put("description", hotel.description)
            put("amenities", hotel.amenities)
            put("contactEmail", hotel.contactEmail)
            put("contactPhone", hotel.contactPhone)
            put("images", hotel.images)
            put("isActive", hotel.isActive)
            put("roomCount", hotel.roomCount)
            put("availableRooms", finalAvailableRooms)
            put("adminId", hotel.adminId)
            put("createdAt", hotel.createdAt)
            // <<< Añadir los nuevos campos para que se guarden en la base de datos >>>
            put("checkInTime", hotel.checkInTime)
            put("checkOutTime", hotel.checkOutTime)
        }
    }

    fun selectHotel(hotel: Hotel) {
        _selectedHotel.value = hotel
        _isEditing.value = false
    }

    fun createNewHotel(adminId: String) {
        // Creamos un hotel nuevo, vacío pero estructuralmente completo.
        val newEmptyHotel = Hotel(
            _id = ObjectId(),           // Generamos un nuevo ID para este hotel
            adminId = adminId,          // Asignamos el ID del admin
            name = "",
            address = "",
            city = "",
            state = "Táchira",
            description = "",
            contactEmail = "",
            contactPhone = "",
            roomCount = 0,
            availableRooms = 0,
            amenities = emptyList(),
            images = emptyList(),
            isActive = true,
            createdAt = System.currentTimeMillis()
            // Los nuevos campos checkInTime y checkOutTime tomarán sus valores por defecto "15:00" y "12:00"
        )
        _selectedHotel.value = newEmptyHotel // Lo ponemos como el hotel seleccionado
        _isEditing.value = true              // Activamos el modo edición para que se abra el formulario
    }

    fun editHotel() {
        _isEditing.value = true
    }

    fun saveHotel(hotel: Hotel) {
        viewModelScope.launch {
            Log.d(TAG, "💾 PROCESANDO GUARDADO PARA: ${hotel.name}")
            try {
                val document = hotelToDocument(hotel)
                var success: Boolean

                val originalHotel = _selectedHotel.value
                // Se considera actualización si el hotel ya existe en la lista que cargamos
                val isUpdate = _hotelsWithStats.value?.any { it.first._id == hotel._id } ?: false

                if (isUpdate) {
                    Log.d(TAG, "🔄 Actualizando hotel con ID: ${hotel._id}")
                    success = MongoDBConnection.updateHotel(hotel._id, document)
                } else {
                    Log.d(TAG, "✨ Creando nuevo hotel: ${hotel.name}")
                    success = MongoDBConnection.insertHotel(document)
                }

                if (success) {
                    val message = if (isUpdate) "✅ Hotel actualizado" else "✅ Hotel creado"
                    Log.d(TAG, message)
                    _toastMessage.value = message

                    cancelEdit()
                    loadHotelsWithStats() // Recarga toda la lista
                } else {
                    val message = if (isUpdate) "❌ Error al actualizar" else "❌ Error al crear"
                    Log.e(TAG, message)
                    _toastMessage.value = message
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 ERROR CRÍTICO AL GUARDAR: ${e.message}", e)
                _toastMessage.value = "💥 Error: ${e.message}"
            }
        }
    }

    fun deleteHotel(hotel: Hotel) {
        viewModelScope.launch {
            Log.d(TAG, "INICIANDO ELIMINACIÓN EN CASCADA PARA: ${hotel.name} (${hotel._id})")
            try {
                // Paso 1: Eliminar todas las habitaciones del hotel.
                val roomsDeleted = MongoDBConnection.deleteRoomsByHotelId(hotel._id)
                if (!roomsDeleted) {
                    Log.w(TAG, "Hubo un problema eliminando las habitaciones del hotel ${hotel._id}")
                }

                // Paso 2: Eliminar todas las reservas del hotel.
                val bookingsDeleted = MongoDBConnection.deleteBookingsByHotelId(hotel._id)
                if (!bookingsDeleted) {
                    Log.w(TAG, "Hubo un problema eliminando las reservas del hotel ${hotel._id}")
                }

                // Paso 3: Eliminar el hotel en sí.
                val hotelDeleted = MongoDBConnection.deleteHotel(hotel._id)

                if (hotelDeleted) {
                    _toastMessage.value = "✅ Hotel y todos sus datos eliminados"
                    _selectedHotel.value = null
                    loadHotelsWithStats() // Recargar la lista para que la UI se actualice
                } else {
                    _toastMessage.value = "❌ Error final al eliminar el hotel."
                }
            } catch (e: Exception) {
                _toastMessage.value = "💥 Error crítico durante la eliminación: ${e.message}"
                Log.e(TAG, "Error crítico durante la eliminación en cascada", e)
            }
        }
    }

    fun cancelEdit() {
        _isEditing.value = false
        _selectedHotel.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
