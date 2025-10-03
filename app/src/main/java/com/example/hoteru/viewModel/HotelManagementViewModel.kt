package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.Hotel
import com.example.hoteru.model.HotelUiState
import com.example.hoteru.model.Location
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.launch
import org.bson.Document

class HotelManagementViewModel : ViewModel() {

    private val _hotelUiState = MutableLiveData<HotelUiState>()
    val hotelUiState: LiveData<HotelUiState> = _hotelUiState

    private val _selectedHotel = MutableLiveData<Hotel?>()
    val selectedHotel: LiveData<Hotel?> = _selectedHotel

    private val _isEditing = MutableLiveData<Boolean>()
    val isEditing: LiveData<Boolean> = _isEditing

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val TAG = "HotelManagementViewModel"

    init {
        loadHotels()
    }

    fun loadHotels() {
        viewModelScope.launch {
            _hotelUiState.value = HotelUiState.Loading
            try {
                val hotels = getAllHotelsFromMongoDB()
                if (hotels.isEmpty()) {
                    _hotelUiState.value = HotelUiState.Empty
                } else {
                    _hotelUiState.value = HotelUiState.Success(hotels)
                }
            } catch (e: Exception) {
                _hotelUiState.value = HotelUiState.Error("Error cargando hoteles: ${e.message}")
            }
        }
    }

    private suspend fun getAllHotelsFromMongoDB(): List<Hotel> {
        return try {
            val documents = MongoDBConnection.getAllHotels()
            documents.map { documentToHotel(it) }
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
            ) // Retorna Hotel por defecto en caso de error
        }
    }

    private fun getLocationFromDocument(document: Document): Location {
        return try {
            val locationDoc = document.get("location") as? Document

            if (locationDoc != null) {
                // 1. Lee la lista como una lista de Number, que es más genérico.
                val rawCoordinates = locationDoc.getList("coordinates", Number::class.java)

                if (rawCoordinates != null && rawCoordinates.size >= 2) {
                    // 2. Convierte cada Number a Double.
                    val coordinates = rawCoordinates.map { it.toDouble() }

                    Location(
                        type = locationDoc.getString("type") ?: "Point",
                        coordinates = coordinates
                    )
                } else {
                    Log.w(TAG, "Coordenadas no encontradas o incompletas en el documento.")
                    Location() // Coordenadas por defecto si la lista es nula o corta
                }
            } else {
                Log.w(TAG, "Campo 'location' no encontrado en el documento.")
                Location() // Location por defecto si el campo 'location' no existe
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error crítico analizando location: ${e.message}", e)
            Location() // Location por defecto en caso de cualquier error
        }
    }

    private fun hotelToDocument(hotel: Hotel): Document {
        val finalAvailableRooms = if (hotel.availableRooms == 0 && hotel.roomCount > 0) {
            hotel.roomCount
        } else {
            hotel.availableRooms
        }

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
            put("availableRooms", finalAvailableRooms) // ✅ Usar el valor calculado
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

                // Usamos el estado original guardado en `_selectedHotel`.
                val originalHotel = _selectedHotel.value

                // Si el hotel original existe y su nombre no estaba en blanco, es una actualización.
                val isUpdate = originalHotel != null && originalHotel.name.isNotBlank()

                if (isUpdate) {
                    //ACTUALIZAR HOTEL EXISTENTE
                    Log.d(TAG, "🔄 Actualizando hotel con ID: ${hotel._id}")
                    success = MongoDBConnection.updateHotel(hotel._id, document)
                } else {
                    //INSERTAR HOTEL NUEVO
                    Log.d(TAG, "✨ Creando nuevo hotel: ${hotel.name}")
                    // Al insertar, MongoDB le asignará un nuevo _id.
                    success = MongoDBConnection.insertHotel(document)
                }

                if (success) {
                    val message = if (isUpdate) "✅ Hotel actualizado" else "✅ Hotel creado"
                    Log.d(TAG, message)
                    _toastMessage.value = message

                    // Cierra el diálogo y refresca la lista
                    cancelEdit() // Esto limpia _selectedHotel y _isEditing
                    loadHotels() // Vuelve a cargar toda la lista desde la BD
                } else {
                    val message =
                        if (isUpdate) "❌ Error al actualizar" else "❌ Error al crear"
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
                    loadHotels()
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