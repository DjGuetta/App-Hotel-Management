package com.example.hoteru.model

import org.bson.Document
import java.util.Date


data class Comment(
    val hotelId: String?,
    val userName: String?,
    val commentText: String,
    val timestamp: Date
)

// ✅ Function to convert Comment → Document
fun Comment.toDocument(): Document {
    return Document()
        .append("hotelId", hotelId)
        .append("userName", userName)
        .append("commentText", commentText)
        .append("timestamp", timestamp)
}

// ✅ Function to convert Document → Comment
fun Document.toComment(): Comment {
    return Comment(
        hotelId = this.getString("hotelId"),
        userName = this.getString("userName"),
        commentText = this.getString("commentText"),
        timestamp = this.getDate("timestamp")
    )
}
