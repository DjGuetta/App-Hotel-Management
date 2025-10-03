package com.example.hoteru.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object map_data {
    val tachiraBounds = LatLngBounds(
        LatLng(7.6, -72.7), // southwest (lower-left) → expanded south/west
        LatLng(8.4, -71.7)  // northeast (upper-right) → expanded north/east
    )

    val tachiraLatLng = LatLng(7.7669, -72.2250)
}