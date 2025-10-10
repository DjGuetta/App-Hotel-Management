package com.example.hoteru.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.example.hoteru.model.Booking
import com.example.hoteru.viewModel.BookingManagementViewModel
import com.example.hoteru.viewModel.roomClass
import org.bson.Document


@Composable
fun DetailsRoomScreen(
    navController: NavController,
    roomId: String?,
    hotelId: String?,
    idUser: String?
) {
    val viewHotel: roomClass = viewModel()
    val r by viewHotel.onedocument.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var bookingToEdit by remember { mutableStateOf<Booking?>(null) }
    val bookingViewModel: BookingManagementViewModel = viewModel()

    LaunchedEffect(roomId) {
        viewHotel.loadDetailsDocument("Rooms", roomId)
    }

    fun Document?.getSafeInt(key: String): Int {
        return when (val v = this?.get(key)) {
            is Int -> v
            is Long -> v.toInt()
            is Double -> v.toInt()
            else -> 0
        }
    }
    val scrollState = rememberScrollState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
            .padding(16.dp)

    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
//                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Detalles de la habitación",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )
                )

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Text(
                    text = "Tipo de habitación",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = r?.getString("roomType") ?: "Desconocido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "Capacidad",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF757575)
                    )
                )
                Text(
                    text = "${r.getSafeInt("capacity")} personas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )

                Text(
                    text = "Precio por noche",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF757575)
                    )
                )
                Text(
                    text = "${r.getSafeInt("pricePerNight")} $",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Estado actual",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF757575)
                    )
                )
                Text(
                    text = r?.getString("status") ?: "VACÍO",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (r?.getString("status") == "OCUPADA")
                            Color(0xFFD32F2F)
                        else Color(0xFF388E3C),
                        fontWeight = FontWeight.Medium
                    )
                )

                val features = r?.getList("amenities", String::class.java) ?: emptyList()
                if (features.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Servicios incluidos",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF757575)
                        )
                    )
                    Text(
                        text = features.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF616161),
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                r?.getString("image")?.takeIf { it.isNotBlank() }?.let { img ->
                    Base64Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        base64 = img
                    )
                }
            }
        }

        BookingEditDialogUser(
            navController = navController,
            bookingToEdit = bookingToEdit,
            hotelId = hotelId.toString(),
            roomId = roomId.toString(),
            onSave = { updatedBooking ->
                bookingViewModel.createBooking(updatedBooking)
                if (!hotelId.isNullOrBlank()) {
                    bookingViewModel.loadActiveBookings(hotelId)
                }
            },
            userId = idUser,
        )
    }
}
