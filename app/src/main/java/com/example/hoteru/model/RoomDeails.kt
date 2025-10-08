// En model/RoomDetails.kt
package com.example.hoteru.model

// Clase de datos para la UI, no se guarda en la base de datos.
data class RoomDetails(
    val room: Room,
    val hotelName: String
)
