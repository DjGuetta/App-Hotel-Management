package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.*
import kotlinx.coroutines.flow.collect
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

    private fun documentToHotel(document: Document): Hotel {
        return try {
            Hotel(
                _id = document.getObjectId("_id"),
                name = document.getString("name") ?: "",
                address = document.getString("address") ?: "",
                city = document.getString("city") ?: "",
                state = document.getString("state") ?: "Táchira",
                location = getLocationFromDocument(document),
                description = document.getString("description") ?: "",
                amenities = document.getList("amenities", String::class.java) ?: emptyList(),
                contactEmail = document.getString("contactEmail") ?: "",
                contactPhone = document.getString("contactPhone") ?: "",
                images = document.getList("images", String::class.java) ?: emptyList(),
                isActive = document.getBoolean("isActive") ?: true,
                roomCount = document.getInteger("roomCount") ?: 0,
                availableRooms = document.getInteger("availableRooms")
                    ?: document.getInteger("roomCount") ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo documento a hotel: ${e.message}")
            Hotel(
                name = "Error cargando hotel",
                description = "No se pudieron cargar los datos del hotel"
            )
        }
    }

    private fun getLocationFromDocument(document: Document): Location {
        return try {
            val locationDoc = document.get("location") as? Document

            if (locationDoc != null) {
                val rawCoordinates = locationDoc.getList("coordinates", Number::class.java)

                if (rawCoordinates != null && rawCoordinates.size >= 2) {
                    val coordinates = rawCoordinates.map { it.toDouble() }

                    Location(
                        type = locationDoc.getString("type") ?: "Point",
                        coordinates = coordinates
                    )
                } else {
                    Log.w(TAG, "Coordenadas no encontradas o incompletas en el documento.")
                    Location()
                }
            } else {
                Log.w(TAG, "Campo 'location' no encontrado en el documento.")
                Location()
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error crítico analizando location: ${e.message}", e)
            Location()
        }
    }

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
        }
    }

    fun selectHotel(hotel: Hotel) {
        _selectedHotel.value = hotel
        _isEditing.value = false
    }

    fun createNewHotel() {
        _selectedHotel.value = Hotel(
            name = "",
            address = "",
            city = "",
            state = "Táchira",
            roomCount = 0,
            availableRooms = 0
        )
        _isEditing.value = true
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
                val isUpdate = originalHotel != null && originalHotel.name.isNotBlank()

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
                    loadHotelsWithStats() // Usar la función reactiva
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
            try {
                val success = MongoDBConnection.deleteHotel(hotel._id)

                if (success) {
                    _toastMessage.value = "✅ Hotel eliminado"
                    _selectedHotel.value = null
                    loadHotelsWithStats()
                } else {
                    _toastMessage.value = "❌ Error eliminando el hotel"
                }
            } catch (e: Exception) {
                _toastMessage.value = "💥 Error: ${e.message}"
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