package com.example.hoteru.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.example.hoteru.viewModel.AuthViewModel
import com.example.hoteru.viewModel.logicComments

import org.bson.Document
import org.bson.types.ObjectId

@Composable
fun HotelsByRating(navController: NavController, rating:String?){
    val mapModel: MapViewModel = viewModel()
    val hotels by mapModel.listOfHotelsByRating.collectAsState()
    val authViewModel: AuthViewModel = viewModel()
    val comments: logicComments = viewModel()

    val authState by authViewModel.authState.collectAsState()

    val commentsByHotel by comments.commentsByHotel.collectAsState()

    LaunchedEffect(hotels) {
        hotels.forEach { hotel ->
            val id = when (val value = hotel["_id"]) {
                is ObjectId -> value.toHexString()
                is String -> value
                else -> null
            }
            id?.let { comments.loadCommentsByHotel(it) }
        }
    }

    LaunchedEffect(rating) {
        val rating = rating?.toDoubleOrNull() ?: 0.0
        mapModel.listOfHotelsByRating(rating)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        Text(
            text = "Hoteles Filtrados Por Calificacion",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
        )

        if (hotels.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(hotels){ index, hotel ->
                    val id = when (val value = hotel["_id"]) {
                        is ObjectId -> value.toHexString()
                        is String -> value
                        else -> null
                    }

                    val name = hotel.getString("name") ?: "Hotel sin nombre"
                    val address = hotel.getString("address") ?: "Dirección no disponible"

                    // Safely parse rating (can be Int, Double, or String)
                    val rating: Double = try {
                        when (val r = hotel["rating"]) {
                            is Int -> r.toDouble()
                            is Double -> r
                            is String -> r.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                    } catch (e: Exception) {
                        0.0
                    }

                    val commentCount = id?.let { commentsByHotel[it]?.size ?: 0 } ?: 0

                    // Safely get list of images
                    val images: List<String> = try {
                        hotel.get("images") as? List<String> ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // Safely get amenities list
                    val features: List<String> = try {
                        hotel.getList("amenities", String::class.java) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                            .clickable {
                                navController.navigate(
                                    "detailshotel/${hotel.get("_id")}/${authState.loginSuccess?.firstName ?: "Usuario"}/${authState.loginSuccess?.id ?: "idUsuario"}"
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column {
                            // --- Image Section ---
                            if (images.isNotEmpty()) {
                                Base64Image(
                                    base64 = images.first(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No image", color = Color.DarkGray)
                                }
                            }

                            // --- Info Section ---
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Ubicación",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // --- Rating Row ---
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f", rating),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${commentCount} reseñas)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                // --- Amenities Row ---
                                if (features.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = features.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF616161)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay hoteles bajo esa categoria")
            }
        }
    }
}