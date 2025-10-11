package com.example.hoteru.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.Hotel
import com.example.hoteru.model.Room
import com.example.hoteru.viewModel.RoomManagementViewModel
import org.bson.types.ObjectId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoomManagementScreen(navController: NavController) {
    val viewModel: RoomManagementViewModel = viewModel()
    val roomsByHotel by viewModel.roomDetailsByHotel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hotels by viewModel.hotels.collectAsState()

    var showSelectHotelDialog by remember { mutableStateOf(false) }

    // --- 1. 'val' (solo lectura) ---
    val roomToEdit by viewModel.selectedRoom.collectAsState()

    // --- 2. 'roomToDelete' para claridad ---
    var roomToDelete by remember { mutableStateOf<Room?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Habitaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSelectHotelDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Habitación")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading && roomsByHotel.isEmpty()) {
                CircularProgressIndicator()
            } else if (roomsByHotel.isEmpty()) {
                Text("No hay habitaciones registradas en ningún hotel.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    roomsByHotel.forEach { (hotelName, roomDetails) ->
                        stickyHeader {
                            Text(
                                text = hotelName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(roomDetails, key = { it.room._id }) { detail ->
                            RoomCard(
                                room = detail.room,
                                // --- 3. Llamamos a la función del ViewModel ---
                                onEditClick = { viewModel.selectRoom(detail.room) },
                                // --- 4. Usamos la variable renombrada 'roomToDelete' ---
                                onDeleteClick = { roomToDelete = detail.room },
                                onStatusChange = { room, newStatus ->
                                    viewModel.updateRoomStatus(room._id, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- Diálogo para seleccionar hotel (al crear) ---
        if (showSelectHotelDialog) {
            SelectHotelDialog(
                hotels = hotels,
                onDismiss = { showSelectHotelDialog = false },
                onHotelSelected = { hotel ->
                    showSelectHotelDialog = false
                    // --- 5. Creamos un objeto Room completo con valores por defecto ---
                    val newRoom = Room(
                        hotelId = hotel._id,
                        roomNumber = "",
                        roomType = "Individual",
                        pricePerNight = 0.0,
                        description = "",
                        amenities = emptyList(),
                        capacity = 1,
                        status = "DISPONIBLE",
                        images = emptyList()
                    )
                    viewModel.selectRoom(newRoom)
                }
            )
        }

        // --- Diálogo de Edición/Creación ---
        // Se muestra si hay una habitación seleccionada en el ViewModel
        roomToEdit?.let { room ->
            RoomEditDialog(
                room = room,
                viewModel = viewModel,
                onDismiss = { viewModel.selectRoom(null) } // Limpia la selección para cerrar
            )
        }

        // --- Diálogo de confirmación de borrado ---
        roomToDelete?.let { room ->
            AlertDialog(
                onDismissRequest = { roomToDelete = null },
                title = { Text("Confirmar Eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar la habitación #${room.roomNumber}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRoom(room._id)
                            roomToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { roomToDelete = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun SelectHotelDialog(
    hotels: List<Hotel>,
    onDismiss: () -> Unit,
    onHotelSelected: (Hotel) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Seleccionar Hotel", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn {
                    items(hotels, key = { it._id }) { hotel ->
                        Text(
                            text = hotel.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHotelSelected(hotel) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCard(
    room: Room,
    onEditClick: (Room) -> Unit,
    onDeleteClick: (Room) -> Unit,
    onStatusChange: (Room, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Habitación ${room.roomNumber}",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = room.roomType,
                    style = TextStyle(fontSize = 16.sp, color = Color.Gray)
                )
                Text(
                    text = room.description,
                    style = TextStyle(fontSize = 14.sp, color = Color.DarkGray)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Capacidad",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.capacity} personas",
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                    )
                }
                if (room.amenities.isNotEmpty()) {
                    Text(
                        text = "Servicios: ${room.amenities.joinToString(", ")}",
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = "$${room.pricePerNight}/noche",
                    style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                )

                Column(horizontalAlignment = Alignment.End) {
                    StatusDropdown(
                        currentStatus = room.status,
                        onStatusSelected = { newStatus -> onStatusChange(room, newStatus) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        TextButton(onClick = { onEditClick(room) }) {
                            Text("Editar")
                        }
                        TextButton(onClick = { onDeleteClick(room) }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    currentStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("DISPONIBLE", "OCUPADA", "MANTENIMIENTO", "LIMPIEZA")
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (currentStatus.uppercase()) {
        "DISPONIBLE" -> Color(0xFF4CAF50)
        "OCUPADA" -> Color(0xFFF44336)
        "MANTENIMIENTO" -> Color(0xFFFF9800)
        "LIMPIEZA" -> Color(0xFF03A9F4)
        else -> Color.Gray
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentStatus,
                style = TextStyle(fontWeight = FontWeight.Bold, color = statusColor)
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
