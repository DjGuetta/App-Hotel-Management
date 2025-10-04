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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip

import org.bson.Document
import org.bson.types.ObjectId

@Composable
fun HotelsByRating(navController: NavController, rating:String?){
    val mapModel: MapViewModel = viewModel()
    val hotels by mapModel.listOfHotelsByRating.collectAsState()

    LaunchedEffect(rating) {
        val rating = rating?.toDoubleOrNull() ?: 0.0
        mapModel.listOfHotelsByRating(rating)
    }

    if (hotels.isNotEmpty()){
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // spacing between hotels
        ) {

            items(hotels) { hotel ->
                val id = when (val value = hotel["_id"]) {
                    is ObjectId -> value.toHexString()   // if it's ObjectId
                    is String -> value                    // if it's String
                    else -> null                          // fallback if _id is missing
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    onClick = {
                        navController.navigate("detailshotel/$id")
                    }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Hotel Name
                        Text(
                            text = hotel.getString("name") ?: "Not found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        // Hotel Description
                        Text(
                            text = hotel.getString("description") ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(8.dp))

                        // Hotel Images (horizontal scroll)
                        val images = hotel.get("images") as? List<String> ?: emptyList()
                        if (images.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(images) { imageBase64 ->
                                    Base64Image(
                                        imageBase64,
                                    )
                                }
                            }
                        } else {
                            Text(
                                "No images available",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

    }else{
        Text("no hay hoteles con esa categoria")
    }



}