// En model/BookingDetails.kt
package com.example.hoteru.model

// Esta clase no se guarda en la BD, es solo para la UI.
data class BookingDetails(
    val booking: Booking,
    val hotelName: String,
    val roomNumber: String
)
