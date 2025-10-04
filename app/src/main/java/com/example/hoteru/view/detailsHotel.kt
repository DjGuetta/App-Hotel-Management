@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.example.hoteru.view

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.app.viewmodel.MapViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

import androidx.compose.ui.draw.clip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.bson.types.ObjectId


@Composable
fun Base64Image(
    base64: String,
    modifier: Modifier = Modifier
) {
    if (base64.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No image")
        }
    } else {
        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Hotel image",
            modifier = modifier
        )
    }
}

@Composable
fun DetailsHotelScreen(navController: NavController, hotelId: String?){
    val viewHotel: MapViewModel = viewModel()
    val hotel by viewHotel.onehotel.collectAsState()
    val rooms by viewHotel.rooms.collectAsState()

    LaunchedEffect(hotelId){
        viewHotel.loadDetailsDocument("Hoteles", hotelId)
//        viewHotel.loadRooms(hotelId)

    }
    val images = hotel?.get("images") as? List<String> ?: emptyList()
    print(images)

    Column(modifier = Modifier.padding(16.dp)) {
        // Hotel data
        Text(
            text = hotel?.getString("name") ?: "not found..",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = hotel?.getString("description") ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = hotel?.getString("contactEmail") ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = hotel?.getString("contactPhone") ?: "",
            style = MaterialTheme.typography.bodyMedium
        )

        // Hotel images
        LazyColumn {
            items(images) { img ->
                Base64Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp),
                    base64 = img
                )
            }
        }
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

//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(rooms) { r ->
//                    val id = when (val value = r["_id"]) {
//                        is ObjectId -> value.toHexString()   // if it's ObjectId
//                        is String -> value                    // if it's String
//                        else -> null                          // fallback if _id is missing
//                    }
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(12.dp),
//                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                        onClick = {
//                            navController.navigate("detailsroom/$id")
//                        }
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "Tipo: ${r.getString("type") ?: "Desconocido"}",
//                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                text = "Cantidad disponible: ${r.getInteger("quantity") ?: 0}",
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                text = "Precio: ${r.getInteger("price") ?: 0} $",
//                                style = MaterialTheme.typography.bodyMedium.copy(
//                                    color = Color(
//                                        0xFF388E3C
//                                    )
//                                ) // green
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            r.getString("image")?.takeIf { it.isNotBlank() }?.let { img ->
//                                Base64Image(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(160.dp)
//                                        .clip(RoundedCornerShape(10.dp)),
//                                    base64 = img
//                                )
//                            }
//                        }
//                    }
//                }
//            }
        }

    }}




