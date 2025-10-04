package com.example.hoteru.model

import org.bson.Document
import org.bson.types.ObjectId

data class Room(
    val _id: ObjectId = ObjectId(),
    val hotelId: ObjectId,
    val roomNumber: String,
    val roomType: String,
    val pricePerNight: Double,
    val description: String,
    val amenities: List<String>,
    val capacity: Int,
    val status: String = "Available", // Available, Occupied, Maintenance, Cleaning
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDocument(doc: Document): Room {
            return Room(
                _id = doc.getObjectId("_id"),
                hotelId = doc.getObjectId("hotelId"),
                roomNumber = doc.getString("roomNumber"),
                roomType = doc.getString("roomType"),
                pricePerNight = doc.getDouble("pricePerNight"),
                description = doc.getString("description"),
                amenities = doc.getList("amenities", String::class.java),
                capacity = doc.getInteger("capacity"),
                status = doc.getString("status"),
                isActive = doc.getBoolean("isActive", true),
                createdAt = doc.getLong("createdAt")
            )
        }
    }

    fun toDocument(): Document {
        return Document().apply {
            put("_id", _id)
            put("hotelId", hotelId)
            put("roomNumber", roomNumber)
            put("roomType", roomType)
            put("pricePerNight", pricePerNight)
            put("description", description)
            put("amenities", amenities)
            put("capacity", capacity)
            put("status", status)
            put("isActive", isActive)
            put("createdAt", createdAt)
        }
    }
}