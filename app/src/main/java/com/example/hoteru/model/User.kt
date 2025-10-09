package com.example.hoteru.model

import androidx.compose.runtime.Immutable
import org.bson.Document
import java.util.UUID

@Immutable
sealed class UserType(val value: String) {
    object Guest : UserType("Guest")    object RegisteredUser : UserType("RegisteredUser")
    object Admin : UserType("Admin")

    override fun toString(): String {
        return value
    }
}

data class User(
    val id: String = UUID.randomUUID().toString(), // Usamos UUID para IDs únicos y robustos
    val email: String,
    val firstName: String,
    val lastName: String,
    val userType: UserType,
    val phone: String = "",
    val hotelId: String? = null, // Solo para admins
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName"

    val isGuest: Boolean get() = userType is UserType.Guest
    val isRegistered: Boolean get() = userType is UserType.RegisteredUser
    val isAdmin: Boolean get() = userType is UserType.Admin
}

/**
 * Función de extensión para convertir un objeto User a un Document de MongoDB
 * que se pueda guardar en la base de datos.
 */
fun User.toDocument(passwordHash: String): Document {
    return Document().apply {
        append("userId", this@toDocument.id)
        append("email", this@toDocument.email.lowercase())
        append("firstName", this@toDocument.firstName)
        append("lastName", this@toDocument.lastName)
        append("passwordHash", passwordHash)
        append("userType", this@toDocument.userType.value)
        append("phone", this@toDocument.phone)
        append("createdAt", this@toDocument.createdAt)
        this@toDocument.hotelId?.let { append("hotelId", it) } // Añade hotelId solo si no es nulo
    }
}
