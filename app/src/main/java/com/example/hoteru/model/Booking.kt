package com.example.hoteru.model

import org.bson.Document
import org.bson.types.ObjectId
import java.util.Date

data class Booking(val _id: ObjectId = ObjectId(),
                   val hotelId: ObjectId,
                   val roomId: ObjectId,
                   val guestName: String,
                   val guestEmail: String,
                   val checkInDate: Date,
                   val checkOutDate: Date,
                   val status: String = "CONFIRMED", // Ej: CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
                   val createdAt: Date = Date()
) {
    companion object {
        fun fromDocument(doc: Document): Booking {
            return Booking(
                _id = doc.getObjectId("_id"),
                hotelId = doc.getObjectId("hotelId"),
                roomId = doc.getObjectId("roomId"),
                guestName = doc.getString("guestName"),
                guestEmail = doc.getString("guestEmail"),
                checkInDate = doc.getDate("checkInDate"),
                checkOutDate = doc.getDate("checkOutDate"),
                status = doc.getString("status"),
                createdAt = doc.getDate("createdAt")
            )
        }
    }

    fun toDocument(): Document {
        return Document()
            .append("_id", _id)
            .append("hotelId", hotelId)
            .append("roomId", roomId)
            .append("guestName", guestName)
            .append("guestEmail", guestEmail)
            .append("checkInDate", checkInDate)
            .append("checkOutDate", checkOutDate)
            .append("status", status)
            .append("createdAt", createdAt)
    }
}
