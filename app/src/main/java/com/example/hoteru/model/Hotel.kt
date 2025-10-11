package com.example.hoteru.model

import org.bson.types.ObjectId

data class Hotel(
    val _id: ObjectId = ObjectId(),
    val name: String,
    val address: String = "",
    val city: String = "",
    val state: String = "Táchira",
    val location: Location = Location(), // Usa la clase Location externa
    val description: String = "",
    val amenities: List<String> = emptyList(),
    val contactEmail: String = "",
    val contactPhone: String = "",
    val images: List<String> = emptyList(),
    val isActive: Boolean = true,
    val roomCount: Int = 0,
    val availableRooms: Int = 0,
    val adminId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val rating: Int = 0, //Valor por defecto de 0.0 para hoteles nuevos o sin calificar
    val checkInTime: String = "15:00",  // <<< HORA ESTÁNDAR DE CHECK-IN (formato HH:mm)
    val checkOutTime: String = "12:00" // <<< HORA ESTÁNDAR DE CHECK-OUT (formato HH:mm)
) {
    fun getLocationString(): String {
        return if (address.isNotEmpty() && city.isNotEmpty()) {
            "$address, $city, $state"
        } else if (city.isNotEmpty()) {
            "$city, $state"
        } else {
            state
        }
    }

    fun getRoomStats(rooms: List<Room>): RoomStats {
        val totalRooms = rooms.size
        val availableRooms = rooms.count { it.status.equals("Available", ignoreCase = true) && it.isActive }
        val occupiedRooms = rooms.count { it.status.equals("Occupied", ignoreCase = true) && it.isActive }
        val maintenanceRooms = rooms.count { it.status.equals("Maintenance", ignoreCase = true) && it.isActive }
        val cleaningRooms = rooms.count { it.status.equals("Cleaning", ignoreCase = true) && it.isActive }

        return RoomStats(
            total = totalRooms,
            available = availableRooms,
            occupied = occupiedRooms,
            maintenance = maintenanceRooms,
            cleaning = cleaningRooms
        )
    }

    // Estados para la UI
    sealed class HotelUiState {
        object Loading : HotelUiState()
        data class Success(val hotels: List<Hotel>) : HotelUiState()
        data class Error(val message: String) : HotelUiState()
        object Empty : HotelUiState()
    }
}
