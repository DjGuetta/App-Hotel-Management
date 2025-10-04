package com.example.hoteru.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import MapViewModel
import org.bson.Document


@Composable
fun DetailsRoomScreen(navController: NavController, roomId: String?) {
    val viewHotel: MapViewModel = viewModel()
    val r by viewHotel.onehotel.collectAsState()

    LaunchedEffect(roomId) {
        viewHotel.loadDetailsDocument("Rooms", roomId)
    }

    println("room view $r")

    // Safe function to get integer from MongoDB document
    fun Document?.getSafeInt(key: String): Int {
        return when (val v = this?.get(key)) {
            is Int -> v
            is Long -> v.toInt()
            is Double -> v.toInt()
            else -> 0
        }
    }

    val images = r?.get("image")
    print(images)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tipo: ${r?.getString("roomType") ?: "Desconocido"}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cantidad disponible: ${r.getSafeInt("capacity")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Precio: ${r.getSafeInt("pricePerNight")} $",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C)) // green
            )
            Text(
                text = "Estado: ${r?.getString("status") ?: "VACIO"}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C)) // green
            )

            val features = r?.getList("amenities", String::class.java) ?: emptyList()
            if (features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Servicios: ${features.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            r?.getString("image")?.takeIf { it.isNotBlank() }?.let { img ->
                Base64Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    base64 = img
                )
            }
        }
    }
}
