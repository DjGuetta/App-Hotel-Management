package com.example.hoteru.model

data class RoomStats(
    val total: Int,
    val available: Int,
    val occupied: Int,
    val maintenance: Int,
    val cleaning: Int
)