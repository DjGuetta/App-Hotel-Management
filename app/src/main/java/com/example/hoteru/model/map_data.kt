package com.example.hoteru.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object map_data {
    val tachiraBounds = LatLngBounds(
        LatLng(7.4, -72.9), // southwest → 0.2° more south and west
        LatLng(8.6, -71.5)  // northeast → 0.2° more north and east
    )

    val tachiraLatLng = LatLng(7.7669, -72.2250)
}