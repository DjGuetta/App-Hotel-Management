package com.example.hoteru.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class UserType {
    object Guest : UserType()
    object RegisteredUser : UserType()
    object Admin : UserType()
}

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val userType: UserType = UserType.RegisteredUser,
    val hotelId: String? = null, // Solo para admins
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isGuest: Boolean get() = userType == UserType.Guest
    val isRegistered: Boolean get() = userType == UserType.RegisteredUser
    val isAdmin: Boolean get() = userType == UserType.Admin
}