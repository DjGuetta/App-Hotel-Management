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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.example.hoteru.viewModel.AuthViewModel

import org.bson.Document
import org.bson.types.ObjectId

@Composable
fun HotelsByRating(navController: NavController, rating:String?){
    val mapModel: MapViewModel = viewModel()
    val hotels by mapModel.listOfHotelsByRating.collectAsState()
    val authViewModel: AuthViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()

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

        if (hotels.isNotEmpty()){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // spacing between hotels
            ) {
                items(hotels) { hotel ->
                    val id = when (val value = hotel["_id"]) {
                        is ObjectId -> value.toHexString()   // if it's ObjectId
                        is String -> value                    // if it's String
                        else -> null                          // fallback if _id is missing
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp)), // black border
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), // white background
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        onClick = {
                            navController.navigate("detailshotel/${hotel.get("_id")}/${authState.loginSuccess?.firstName ?: "Usuario"}/${authState.loginSuccess?.id ?: "idUsuario"}")
                        }
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, // center horizontally
                            verticalArrangement = Arrangement.Center // center vertically
                        ) {
                            // Hotel Name
                            Text(
                                text = hotel.getString("name") ?: "Not found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(4.dp))

                            // Hotel Description
                            Text(
                                text = hotel.getString("description") ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            // Hotel Images (horizontal scroll)
                            val images = (hotel.get("images") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            if (images.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(images) { imageBase64 ->
                                        Box(
                                            modifier = Modifier
                                                .width(180.dp)
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.LightGray)
                                        ) {
                                            Base64Image(
                                                base64 = imageBase64,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.LightGray)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    "No images available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

        }else{
            Text(
                "no hay hoteles con esa categoria",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
    }
}