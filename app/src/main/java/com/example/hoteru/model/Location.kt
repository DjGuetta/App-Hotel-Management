package com.example.hoteru.model

data class Location(
    val type: String = "Point",
    val coordinates: List<Double> = listOf(0.0, 0.0)
) {
    val longitude: Double get() = coordinates.getOrElse(0) { 0.0 }
    val latitude: Double get() = coordinates.getOrElse(1) { 0.0 }
}