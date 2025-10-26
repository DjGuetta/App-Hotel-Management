package com.example.hoteru.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import MapViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import org.bson.types.ObjectId


@Composable
fun RoomsByPrice(navController: NavController, minimum: String?, maximum: String?, user: String?) {
    val mapModel: MapViewModel = viewModel()
    val hotel by mapModel.onehotel.collectAsState()
    val rooms by mapModel.listOfRoomsUnderTheirPrice.collectAsState()
    val hotelbyroom by mapModel.hotelsById.collectAsState()

    // Parse safely
    val minValue = minimum?.toIntOrNull() ?: 0
    val maxValue = maximum?.toIntOrNull() ?: Int.MAX_VALUE

    // Fetch rooms whenever min/max change
    LaunchedEffect(minValue, maxValue) {
        mapModel.listOfRoomsUnderTheirPrice(minValue, maxValue)
    }

    LaunchedEffect(rooms) {
        if (rooms.isNotEmpty()) {
            mapModel.loadHotelsForRooms(rooms)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Habitaciones Filtrada por precios",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
        )

        if (rooms.isEmpty()) {
            Text("No se encontraron habitaciones para ese rango de precios.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms ) { r ->
                    val idroom = when (val value = r["_id"]) {
                        is ObjectId -> value.toHexString()
                        is String -> value
                        else -> null
                    }
                    val idhotel = when (val value = r["hotelId"]) {
                        is ObjectId -> value.toHexString()
                        is String -> value
                        else -> null
                    }
                    val hotelDoc = idhotel?.let { hotelbyroom[it] }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp)), // borde negro
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        onClick = { navController.navigate("detailsroom/$idroom/$idhotel/$user") }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hotel: ${hotelDoc?.getString("name") ?: "Desconocido"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Tipo: ${r.getString("roomType") ?: "Desconocido"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cantidad disponible: ${r.getInteger("capacity") ?: 0}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Precio: ${r.getDouble("pricePerNight") ?: r.getInteger("pricePerNight") ?: 0} $",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C))
                            )

                            val status = r.getString("status") ?: "VACIO"
                            if (status == "OCUPADA"){
                                Text(
                                    text = "Estado: $status",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFF44336))
                                )

                            }else{
                                Text(
                                    text = "Estado: $status",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C))
                                )

                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            val imageList = r?.getList("images", String::class.java) ?: emptyList()

                            if (imageList.isNotEmpty()) {
                                imageList.forEach { img ->
                                    if (!img.isNullOrBlank()) {
                                        Base64Image(
                                            base64 = img,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


