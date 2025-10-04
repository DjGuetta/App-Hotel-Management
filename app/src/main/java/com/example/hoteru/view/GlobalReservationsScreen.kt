// En view/GlobalReservationsScreen.kt
package com.example.hoteru.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.BookingDetails
import com.example.hoteru.viewModel.GlobalBookingViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GlobalReservationsScreen(navController: NavController) {
    val viewModel: GlobalBookingViewModel = viewModel()
    val bookingsByHotel by viewModel.bookingDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas las Reservas Activas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (bookingsByHotel.isEmpty()) {
                Text("No hay ninguna reserva activa en ningún hotel.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    bookingsByHotel.forEach { (hotelName, bookings) ->
                        // Header para cada hotel
                        stickyHeader {
                            HotelHeader(hotelName)
                        }
                        // Lista de reservas para ese hotel
                        items(bookings, key = { it.booking._id }) { bookingDetail ->
                            BookingDetailCard(
                                bookingDetail = bookingDetail,
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateBookingStatus(bookingDetail.booking._id, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HotelHeader(hotelName: String) {
    Text(
        text = hotelName,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun BookingDetailCard(bookingDetail: BookingDetails, onUpdateStatus: (String) -> Unit) {
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val booking = bookingDetail.booking

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = booking.guestName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = bookingDetail.roomNumber,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Check-in: ${dateFormatter.format(booking.checkInDate)}")
            Text("Check-out: ${dateFormatter.format(booking.checkOutDate)}")
            Spacer(Modifier.height(8.dp))
            //... (similar al BookingCard anterior, con los botones de acción)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.status,
                    fontWeight = FontWeight.SemiBold,
                    color = when (booking.status) {
                        "CONFIRMED" -> Color(0xFF0288D1)
                        "CHECKED_IN" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                )
                Spacer(Modifier.width(16.dp))
                if (booking.status == "CONFIRMED") {
                    Button(onClick = { onUpdateStatus("CHECKED_IN") }) { Text("Check-In") }
                }
                if (booking.status == "CHECKED_IN") {
                    Button(onClick = { onUpdateStatus("CHECKED_OUT") }) { Text("Check-Out") }
                }
            }
        }
    }
}
