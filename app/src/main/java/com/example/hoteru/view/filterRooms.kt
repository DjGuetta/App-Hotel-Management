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
import com.example.app.viewmodel.MapViewModel
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
import org.bson.types.ObjectId


@Composable
fun RoomsByPrice(navController: NavController, minimum: String?, maximum: String?){
    val mapModel: MapViewModel = viewModel()

//    val idhotel by mapModel.idHotel.collectAsState()
    val hotel by mapModel.onehotel.collectAsState()
    val rooms by mapModel.listOfRoomsUnderTheirPrice.collectAsState()

    LaunchedEffect(minimum, maximum) {
        val minValue = minimum?.toDoubleOrNull() ?: 0.0
        val maxValue = maximum?.toDoubleOrNull() ?: Double.MAX_VALUE
        mapModel.listOfRoomsUnderTheirPrice(minValue, maxValue)
    }
//
//    LaunchedEffect(idhotel) {
//        mapModel.loadDetailsDocument("Hotels", idhotel)
//    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Habitaciones",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { r ->
                val idroom = when (val value = r["_id"]) {
                    is ObjectId -> value.toHexString()   // if it's ObjectId
                    is String -> value                    // if it's String
                    else -> null                          // fallback if _id is missing
                }
                val idhotel = when (val value = r["hotel_id"]) {
                    is ObjectId -> value.toHexString()   // if it's ObjectId
                    is String -> value                    // if it's String
                    else -> null                          // fallback if _id is missing
                }
//                    mapModel.idHotel(idhotel)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = {
                        navController.navigate("detailsroom/$idroom")

                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hotel: ${hotel?.getString("name ") ?: "Desconocido"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Tipo: ${r.getString("type") ?: "Desconocido"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cantidad disponible: ${r.getInteger("quantity") ?: 0}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Precio: ${r.getInteger("price") ?: 0} $",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C)) // green
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        r.getString("image")?.takeIf { it.isNotBlank() }?.let { img ->
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
        }
    }
}
