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
    val createdAt: Long = System.currentTimeMillis()
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
        val availableRooms = rooms.count { it.status == "Available" && it.isActive }
        val occupiedRooms = rooms.count { it.status == "Occupied" && it.isActive }
        val maintenanceRooms = rooms.count { it.status == "Maintenance" && it.isActive }
        val cleaningRooms = rooms.count { it.status == "Cleaning" && it.isActive }

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
