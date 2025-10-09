package com.example.hoteru.model

import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.Date

data class Comment(
    val _id: ObjectId = ObjectId(),          // MongoDB unique ID
    val hotelId: String?,                   // Reference to the hotel document
//    val userName: String,                    // User who wrote the comment
    val commentText: String,                 // The comment text
    val timestamp: Date = Date()  // Time when comment was created
)