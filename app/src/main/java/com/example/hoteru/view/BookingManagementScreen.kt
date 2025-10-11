package com.example.hoteru.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.Booking
import com.example.hoteru.model.Hotel
import com.example.hoteru.viewModel.BookingManagementViewModel
import com.example.hoteru.viewModel.HotelManagementViewModel
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingManagementScreen(navController: NavController, hotelId: String?) {
    // --- ViewModels ---
    val bookingViewModel: BookingManagementViewModel = viewModel()
    val hotelViewModel: HotelManagementViewModel = viewModel()

    // --- Estados ---
    val bookings by bookingViewModel.bookings.collectAsStateWithLifecycle()
    val isLoading by bookingViewModel.isLoading.collectAsStateWithLifecycle()
    val currentHotel by hotelViewModel.selectedHotel.observeAsState()

    // --- Estados para el diálogo ---
    var showEditDialog by remember { mutableStateOf(false) }
    var bookingToEdit by remember { mutableStateOf<Booking?>(null) }


    // Cargar las reservas cuando la pantalla se inicia, si el hotelId no es nulo
    LaunchedEffect(hotelId) {
        if (!hotelId.isNullOrBlank()) {
            bookingViewModel.loadActiveBookings(hotelId)
            try {
                hotelViewModel.loadHotelById(ObjectId(hotelId))
            } catch (e: Exception) {
                Log.e("BookingManagement", "ID de hotel inválido: $hotelId", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Reservas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    bookingToEdit = null
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Reserva", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (hotelId.isNullOrBlank()) {
                Text("Error: ID de hotel no válido.")
            } else if (bookings.isEmpty()) {
                Text(
                    "No hay reservas activas en este momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookings, key = { it._id.toString() }) { booking ->
                        BookingCard(
                            booking = booking,
                            hotel = currentHotel,
                            onCheckIn = { bookingId ->
                                bookingViewModel.performCheckIn(bookingId, hotelId)
                            },
                            onCheckOut = { bookingId ->
                                bookingViewModel.performCheckOut(bookingId, hotelId)
                            }
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO PARA CREAR O EDITAR ---
        if (showEditDialog) {
            val adminId = currentHotel?.adminId?.toString() // Usar toString() para consistencia

            BookingEditDialog(
                bookingToEdit = bookingToEdit,
                onDismiss = { showEditDialog = false },
                onSave = { updatedBooking ->
                    bookingViewModel.saveBooking(updatedBooking)
                    showEditDialog = false
                    if (!hotelId.isNullOrBlank()) {
                        bookingViewModel.loadActiveBookings(hotelId)
                    }
                },
                currentAdminId = adminId ?: ""
            )
        }
    }
}


@Composable
fun BookingCard(
    booking: Booking,
    hotel: Hotel?,
    onCheckIn: (ObjectId) -> Unit,
    onCheckOut: (ObjectId) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val now = Calendar.getInstance()

    val canCheckIn = remember(booking, hotel, now) {
        isCheckInAllowed(booking, hotel, now)
    }
    val canCheckOut = remember(booking, hotel, now) {
        isCheckOutAllowed(booking, hotel, now)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Huésped: ${booking.guestName}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text("Email: ${booking.guestEmail}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Check-in: ${dateFormatter.format(booking.checkInDate)}",
                fontSize = 14.sp
            )
            Text(
                "Check-out: ${dateFormatter.format(booking.checkOutDate)}",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Estado: ", fontSize = 14.sp)
                Text(
                    text = booking.status,
                    fontWeight = FontWeight.SemiBold,
                    color = when (booking.status.uppercase()) {
                        "CONFIRMED" -> Color(0xFF0288D1)
                        "CHECKED_IN" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (booking.status.equals("CONFIRMED", ignoreCase = true)) {
                    Button(
                        onClick = { onCheckIn(booking._id) },
                        enabled = canCheckIn
                    ) {
                        Text("Hacer Check-In")
                    }
                }
                if (booking.status.equals("CHECKED_IN", ignoreCase = true)) {
                    Button(
                        onClick = { onCheckOut(booking._id) },
                        enabled = canCheckOut
                    ) {
                        Text("Hacer Check-Out")
                    }
                }
            }
        }
    }
}
private fun isSameDay(cal1: Calendar, date2: Date): Boolean {
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isCheckInAllowed(booking: Booking, hotel: Hotel?, now: Calendar): Boolean {
    try {
        if (hotel == null || !booking.status.equals("CONFIRMED", ignoreCase = true)) {
            return false
        }
        if (!isSameDay(now, booking.checkInDate)) {
            return false
        }
        val (hotelCheckInHour, hotelCheckInMinute) = hotel.checkInTime.split(":").map { it.toInt() }
        val checkInLimitTime = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hotelCheckInHour)
            set(Calendar.MINUTE, hotelCheckInMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return now.timeInMillis >= checkInLimitTime.timeInMillis
    } catch (e: Exception) {
        Log.e("CheckInLogic", "Error validando check-in: ${e.message}", e)
        return false
    }
}

private fun isCheckOutAllowed(booking: Booking, hotel: Hotel?, now: Calendar): Boolean {
    try {
        if (hotel == null || !booking.status.equals("CHECKED_IN", ignoreCase = true)) {
            return false
        }
        if (!isSameDay(now, booking.checkOutDate)) {
            return false
        }
        val (hotelCheckOutHour, hotelCheckOutMinute) = hotel.checkOutTime.split(":").map { it.toInt() }
        val checkOutLimitTime = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hotelCheckOutHour)
            set(Calendar.MINUTE, hotelCheckOutMinute)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return now.timeInMillis <= checkOutLimitTime.timeInMillis
    } catch (e: Exception) {
        Log.e("CheckOutLogic", "Error validando check-out: ${e.message}", e)
        return false
    }
}
