package com.example.hoteru.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.hoteru.viewModel.BookingManagementViewModel
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingManagementScreen(navController: NavController, hotelId: String?) {
    val viewModel: BookingManagementViewModel = viewModel()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Estado para controlar la visibilidad del diálogo de creación
    var showCreateDialog by remember { mutableStateOf(false) }

    // Cargar las reservas cuando la pantalla se inicia, si el hotelId no es nulo
    LaunchedEffect(hotelId) {
        if (!hotelId.isNullOrBlank()) {
            viewModel.loadActiveBookings(hotelId)
        }
    }

    // Envolvemos todo en un Scaffold.
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
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Reserva", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Usar el padding del Scaffold
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
                    items(bookings, key = { it._id.toHexString() }) { booking ->
                        BookingCard(
                            booking = booking,
                            onUpdateStatus = { bookingId, newStatus ->
                                viewModel.updateBookingStatus(bookingId, newStatus, hotelId)
                            }
                        )
                    }
                }
            }
        }

        // Lógica para mostrar el diálogo de creación
        if (showCreateDialog) {
            BookingEditDialog(
                onDismiss = { showCreateDialog = false },
                onSave = { newBooking ->
                    viewModel.createBooking(newBooking)
                    showCreateDialog = false
                }
            )
        }
    }
}


@Composable
fun BookingCard(booking: Booking, onUpdateStatus: (ObjectId, String) -> Unit) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
                    color = when (booking.status) {
                        "CONFIRMED" -> Color(0xFF0288D1) // Azul
                        "CHECKED_IN" -> Color(0xFF4CAF50) // Verde
                        else -> Color.Gray
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (booking.status.equals("CONFIRMED", ignoreCase = true)) {
                    Button(onClick = { onUpdateStatus(booking._id, "CHECKED_IN") }) {
                        Text("Hacer Check-In")
                    }
                }
                if (booking.status.equals("CHECKED_IN", ignoreCase = true)) {
                    Button(onClick = { onUpdateStatus(booking._id, "CHECKED_OUT") }) {
                        Text("Hacer Check-Out")
                    }
                }
            }
        }
    }
}

