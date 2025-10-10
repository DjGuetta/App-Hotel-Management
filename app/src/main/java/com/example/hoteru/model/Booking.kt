package com.example.hoteru.model

import org.bson.Document
import org.bson.types.ObjectId
import java.util.Date

// Representa una reserva individual en la base de datos
data class Booking(
    val _id: ObjectId = ObjectId(),
    val hotelId: ObjectId,
    val roomId: ObjectId,
    val userId: String, // ID del usuario que reserva
    val checkInDate: Date,
    val checkOutDate: Date,
    val guestName: String,
    val guestEmail: String,
    val totalCost: Double,
    val status: String = "CONFIRMED", // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    val createdAt: Long = System.currentTimeMillis(),
    val actualCheckIn: Date? = null,   // <<< Guarda la fecha y hora REAL del check-in
    val actualCheckOut: Date? = null,  // <<< Guarda la fecha y hora REAL del check-out
) {
    fun toDocument(): Document {
        return Document().apply {
            put("_id", _id)
            put("hotelId", hotelId)
            put("roomId", roomId)
            put("userId", userId)
            put("checkInDate", checkInDate)
            put("checkOutDate", checkOutDate)
            put("guestName", guestName)
            put("guestEmail", guestEmail)
            put("totalCost", totalCost)
            put("status", status)
            put("createdAt", createdAt)
            actualCheckIn?.let { put("actualCheckIn", it) }     // <<< Solo se guarda si no es nulo
            actualCheckOut?.let { put("actualCheckOut", it) }   // <<< Solo se guarda si no es nulo
        }
    }

    companion object {
        fun fromDocument(doc: Document): Booking {
            return Booking(
                _id = doc.getObjectId("_id"),
                hotelId = doc.getObjectId("hotelId"),
                roomId = doc.getObjectId("roomId"),
                userId = doc.getString("userId"),
                checkInDate = doc.getDate("checkInDate"),
                checkOutDate = doc.getDate("checkOutDate"),
                guestName = doc.getString("guestName"),
                guestEmail = doc.getString("guestEmail") ?: "No proporcionado",
                totalCost = doc.getDouble("totalCost"),
                status = doc.getString("status"),
                createdAt = doc.getLong("createdAt") ?: 0L,
                actualCheckIn = doc.getDate("actualCheckIn"),     // <<< Se lee si existe, si no, es nulo
                actualCheckOut = doc.getDate("actualCheckOut")    // <<< Se lee si existe, si no, es nulo
            )
        }
    }
}
