package com.example.hoteru.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.Room
import com.example.hoteru.viewModel.RoomManagementViewModel
import org.bson.types.ObjectId
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.hoteru.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementScreen(
    navController: NavController,
    viewModel: RoomManagementViewModel = viewModel()
) {

    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddRoomDialog by remember { mutableStateOf(false) }

    // Por ahora usamos un hotelId fijo - luego podemos pasarlo como parámetro
    val sampleHotelId = ObjectId("6557a5b8c8f45b7a9c3e2a1b")

    LaunchedEffect(Unit) {
        viewModel.loadRoomsByHotel(sampleHotelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Habitaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddRoomDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar habitación")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRoomDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar habitación")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (rooms.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Bed,
                        contentDescription = "Sin habitaciones",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay habitaciones registradas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Presiona el botón + para agregar una",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(rooms) { room ->
                        RoomCard(
                            room = room,
                            onEditClick = { viewModel.selectRoom(room) },
                            onDeleteClick = { viewModel.deleteRoom(room._id) },
                            onStatusChange = { newStatus ->
                                viewModel.updateRoomStatus(room._id, newStatus)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Mostrar mensajes de error/éxito
            errorMessage?.let { message ->
                LaunchedEffect(message) {
                    // Podrías implementar un snackbar aquí
                }
            }
        }

        // Diálogo para agregar/editar habitación
        if (showAddRoomDialog || viewModel.selectedRoom.collectAsState().value != null) {
            RoomEditDialog(
                room = viewModel.selectedRoom.collectAsState().value,
                hotelId = sampleHotelId,
                onDismiss = {
                    showAddRoomDialog = false
                    viewModel.selectRoom(null)
                },
                onSave = { room ->
                    if (viewModel.selectedRoom.value != null) {
                        viewModel.updateRoom(room)
                    } else {
                        viewModel.createRoom(room)
                    }
                    showAddRoomDialog = false
                    viewModel.selectRoom(null)
                }
            )
        }
    }
}

@Composable
fun RoomCard(
    room: Room,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    // Mapa que relaciona el estado en Inglés (clave) con su traducción en Español (valor).
    val statusTranslations = remember {
        mapOf(
            "Available" to "Disponible",
            "Occupied" to "Ocupada",
            "Cleaning" to "Limpieza",
            "Maintenance" to "En Mantenimiento"
        )
    }
    //val roomStatuses = listOf("Available", "Occupied", "Cleaning", "Maintenance")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (room.status) {
                "Available" -> MaterialTheme.colorScheme.surfaceVariant
                "Occupied" -> MaterialTheme.colorScheme.errorContainer
                "Maintenance" -> MaterialTheme.colorScheme.tertiaryContainer
                "Cleaning" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Alineamos los elementos arriba
            ) {
                    Column(modifier = Modifier.weight(1f)) { // `weight` para que ocupe el espacio disponible ) {
                        Text(
                            text = "Habitación ${room.roomNumber}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = room.roomType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                //Precio
                Text(
                    text = "$${room.pricePerNight}/noche",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = room.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Capacidad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bed,
                        contentDescription = "Capacidad",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.capacity} personas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Estado
                Box {
                    TextButton(onClick = { showStatusMenu = true }) {
                        Text(
                            text = statusTranslations[room.status] ?: room.status,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (room.status) {
                                "Occupied" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                    // Menú desplegable para cambiar el estado
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        statusTranslations.forEach { (statusInEnglish, statusInSpanish) ->
                            DropdownMenuItem(
                                text = { Text(statusInSpanish) },
                                onClick = {
                                    onStatusChange(statusInEnglish)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amenidades
            if (room.amenities.isNotEmpty()) {
                Text(
                    text = "Servicios: ${room.amenities.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ){
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}
