//@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.example.hoteru.view
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import MapViewModel
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.hoteru.model.Comment
import com.example.hoteru.viewModel.logicComments
import org.bson.types.ObjectId
import java.util.Date


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

//@Composable
//fun DetailsHotelScreen(navController: NavController, hotelId: String?){
//    val viewHotel: MapViewModel = viewModel()
//    val hotel by viewHotel.onehotel.collectAsState()
//    val rooms by viewHotel.rooms.collectAsState()
//
//    LaunchedEffect(hotelId){
//        viewHotel.loadDetailsDocument("Hoteles", hotelId)
//        viewHotel.loadRooms(hotelId)
//
//    }
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//    println("cuartos vista $rooms")
//
//
//
//    val images = hotel?.get("images") as? List<String> ?: emptyList()
//    print(images)
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        // Hotel data
//        Text(
//            text = hotel?.getString("name") ?: "not found..",
//            style = MaterialTheme.typography.titleLarge
//        )
//        Text(
//            text = hotel?.getString("description") ?: "",
//            style = MaterialTheme.typography.bodyMedium
//        )
//        Text(
//            text = hotel?.getString("contactEmail") ?: "",
//            style = MaterialTheme.typography.bodyMedium
//        )
//        Text(
//            text = hotel?.getString("contactPhone") ?: "",
//            style = MaterialTheme.typography.bodyMedium
//        )
//
//        // Hotel images
//        LazyColumn {
//            items(images) { img ->
//                Base64Image(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(180.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                        .padding(vertical = 4.dp),
//                    base64 = img
//                )
//            }
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "Habitaciones",
//                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.padding(bottom = 12.dp)
//            )
//
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
////                            Text(
////                                text = "Hotel: ${hotel?.getString("name ") ?: "Desconocido"}",
////                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
////                            )
//                            Text(
//                                text = "Tipo: ${r.getString("roomType") ?: "Desconocido"}",
//                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                text = "Cantidad disponible: ${r.getInteger("capacity") ?: 0}",
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                text = "Precio: ${r.getInteger("pricePerNight") ?: 0} $",
//                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C)) // green
//                            )
//                            Text(
//                                text = "Precio: ${r.getInteger("status") ?: 0} $",
//                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C)) // green
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
//        }
//
//    }
//}
//
//

@Composable
fun CommentsScreen(
    hotelId: String?,
    commentViewModel: logicComments = viewModel(),
    name: String?
) {
    var commentText by remember  { mutableStateOf("") }

    // Load comments when screen starts
    LaunchedEffect(hotelId) {
        commentViewModel.loadComments(hotelId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Comentarios",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Add a new comment section ---
        Text(
            text = "Deja tu comentario:",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Escribe Algo...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Styled button to match other buttons
        Button(
            onClick = {
                val newComment = Comment(
                    hotelId = hotelId,
                    commentText = commentText,
                    userName = name,
                    timestamp = Date()
                )
                commentViewModel.insertComment(newComment)
                commentText = ""
            },
            modifier = Modifier
                .align(Alignment.End)  // keep at the end/right side
                .height(48.dp),        // standard button height
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Text(
                text = "Postea un comentario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailsHotelScreen(navController: NavController, hotelId: String?, name: String?) {
    val viewHotel: MapViewModel = viewModel()
    val viewComment: logicComments = viewModel()

    val hotel by viewHotel.onehotel.collectAsState()
    val rooms by viewHotel.rooms.collectAsState()
    val comments by viewComment.comments.collectAsState()


    LaunchedEffect(hotelId) {
        viewHotel.loadDetailsDocument("Hoteles", hotelId)
        viewHotel.loadRooms(hotelId)
    }

    val images = hotel?.get("images") as? List<String> ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Hotel Name
            Text(
                text = hotel?.getString("name") ?: "Hotel not found",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = hotel?.getString("description") ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Information
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "📧 ${hotel?.getString("contactEmail") ?: "No email available"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "📞 ${hotel?.getString("contactPhone") ?: "No phone number"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "📍 ${hotel?.getString("address") ?: "No address"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rating
            val rating = hotel?.getInteger("rating") ?: 0
            Text(
                text = "⭐ Calificación: $rating / 5",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }


        // Hotel images
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

        // Rooms header
        item {
            Text(
                text = "Habitaciones",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black, // strong emphasis for section title
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Rooms list
        items(rooms ?: emptyList()) { r ->
            val id = when(val value = r["_id"]) {
                is ObjectId -> value.toHexString()
                is String -> value
                else -> "unknown"
            }

            val capacity = when(val c = r["capacity"]) {
                is Int -> c
                is Long -> c.toInt()
                else -> 0
            }

            val price = when(val p = r["pricePerNight"]) {
                is Int -> p
                is Long -> p.toInt()
                is Double -> p.toInt()
                else -> 0
            }

            val status = r["status"]?.toString() ?: "VACIO"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = { navController.navigate("detailsroom/$id/$hotelId") }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hotel: ${hotel?.getString("name") ?: "Desconocido"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Tipo: ${r["roomType"]?.toString() ?: "Desconocido"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cantidad disponible: $capacity",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Precio: $price $",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C))
                    )
                    Text(
                        text = "Estado: $status",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    r["image"]?.toString()?.takeIf { it.isNotBlank() }?.let { img ->
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
        item {
            CommentsScreen(hotelId,viewComment, name)
        }
        items(comments) { comment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormatter.format(comment.timestamp), // Convert Date to String
                        style = MaterialTheme.typography.bodySmall,    // smaller for timestamp
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    comment.userName?.let {
                        Text(
                            text = it, // Convert Date to String
                            style = MaterialTheme.typography.bodySmall,    // smaller for timestamp
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comment.commentText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

    }
}



