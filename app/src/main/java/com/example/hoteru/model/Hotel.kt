package com.example.hoteru.model

import org.bson.types.ObjectId

data class Hotel(
    val _id: ObjectId = ObjectId(),
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "Táchira",
    val location: Location = Location(),
    val description: String = "",
    val amenities: List<String> = emptyList(),
    val contactEmail: String = "",
    val contactPhone: String = "",
    val images: List<String> = emptyList(),
    val isActive: Boolean = true,
    val adminId: String = "", // ID del administrador dueño
    val createdAt: Long = System.currentTimeMillis(),
    val roomCount: Int = 0,
    val availableRooms: Int = 0
) {
    fun getLocationString(): String {
        return "$address, $city, $state"
    }

    }

data class Location(
    val type: String = "Point",
    val coordinates: List<Double> = listOf(0.0,0.0) //Longitud y latitud
){
    val longitude: Double get() = coordinates.getOrNull(0) ?: 0.0
    val latitude: Double get() = coordinates.getOrNull(1) ?: 0.0
}

// Estados para la UI
sealed class HotelUiState {
    object Loading : HotelUiState()
    data class Success(val hotels: List<Hotel>) : HotelUiState()
    data class Error(val message: String) : HotelUiState()
    object Empty : HotelUiState()
}