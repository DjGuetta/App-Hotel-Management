package com.example.hoteru.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.Hotel
import com.example.hoteru.model.HotelUiState
import com.example.hoteru.model.User
import com.example.hoteru.model.RoomStats
import com.example.hoteru.viewModel.HotelManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelManagementScreen(navController: NavController, user:User?) {
    val viewModel: HotelManagementViewModel = viewModel()
    val hotelUiState by viewModel.hotelUiState.observeAsState(HotelUiState.Loading)
    val selectedHotel by viewModel.selectedHotel.observeAsState()
    val isEditing by viewModel.isEditing.observeAsState(false)
    val toastMessage by viewModel.toastMessage.observeAsState()

    LaunchedEffect(Unit) {
        try {
            viewModel.loadHotelsWithStats()
        } catch (e: Exception) {
            Log.e("HotelScreen", "CRASH en loadHotels: ${e.message}", e)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Hoteles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Log.d("Pantalla de gestión de hoteles", "Recargando manualmente...")
                        viewModel.loadHotelsWithStats()
                    }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_rotate),
                            contentDescription = "Recargar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedHotel == null && !isEditing) {
                FloatingActionButton(
                    onClick = {
                        Log.d("Pantalla de gestión de hoteles", "Creando nuevo hotel...")
                        val adminId = user?.id ?: ""
                        viewModel.createNewHotel(adminId)
                    },
                    containerColor = Color(0xFF011C21)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar Hotel",
                        tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = hotelUiState) {
                is HotelUiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando hoteles...")
                    }
                }
                is HotelUiState.Success -> {
                    if (state.hotels.isEmpty()) {
                        EmptyHotelsView(
                            onAddHotel = {
                                val adminId = user?.id ?: ""
                                viewModel.createNewHotel(adminId) },
                        )
                    } else {
                        HotelsListView(
                            navController = navController,
                            hotels = state.hotels,
                            viewModel = viewModel,
                            onHotelClick = {
                                Log.d("Pantalla de gestión de hoteles", "Hotel seleccionado: ${it.name}")
                                viewModel.selectHotel(it)
                            },
                        )
                    }
                }
                is HotelUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = {
                            Log.d("Pantalla de gestión de hoteles", "Reintentando cargar hoteles...")
                            viewModel.loadHotelsWithStats()
                        },
                    )
                }
                is HotelUiState.Empty -> {
                    EmptyHotelsView(
                        onAddHotel = {
                            val adminId = user?.id ?: ""
                            viewModel.createNewHotel(adminId) },
                    )
                }
            }

            // Diálogo de edición
            selectedHotel?.let { hotel ->
                Log.d("Pantalla de gestión de hoteles", "Mostrando diálogo para hotel: ${hotel.name}")
                HotelEditDialog(
                    hotel = hotel,
                    isEditing = isEditing,
                    viewModel = viewModel,
                    onSave = { updatedHotel ->
                        Log.d("Pantalla de gestión de hoteles", "Guardando hotel: ${updatedHotel.name}")
                        viewModel.saveHotel(updatedHotel)
                    },
                    onDelete = {
                        Log.d("Pantalla de gestión de hoteles", "Eliminando hotel: ${hotel.name}")
                        viewModel.deleteHotel(hotel)
                    },
                    onCancel = {
                        Log.d("Pantalla de gestión de hoteles", "Cancelando edición")
                        viewModel.cancelEdit()
                    },
                    onEdit = {
                        Log.d("Pantalla de gestión de hoteles", "Editando hotel")
                        viewModel.editHotel()
                    }
                )
            }
        }
    }
}

@Composable
fun HotelsListView(
    navController: NavController,
    hotels: List<Hotel>,
    viewModel: HotelManagementViewModel,
    onHotelClick: (Hotel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Lista de Hoteles - ${hotels.size} encontrados",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(hotels, key = { hotel -> hotel._id }) { hotel ->
            HotelCard(
                navController = navController,
                hotel = hotel,
                viewModel = viewModel,
                onClick = { onHotelClick(hotel) }
            )
        }
    }
}

@Composable
fun HotelCard(
    navController: NavController,
    hotel: Hotel,
    viewModel: HotelManagementViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roomStats = viewModel.getCurrentRoomStats(hotel._id)
    val availableRooms = roomStats?.available ?: hotel.availableRooms
    val totalRooms = roomStats?.total ?: hotel.roomCount

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = hotel.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF011C21)
            )
            Text(
                text = hotel.getLocationString(),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingDisplay(rating = hotel.rating)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$availableRooms/$totalRooms habitaciones disponibles",
                fontSize = 12.sp,
                color = if (availableRooms > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            // Mostrar estadísticas adicionales si están disponibles
            roomStats?.let { stats ->

                if (stats.maintenance > 0 || stats.cleaning > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (stats.maintenance > 0) append("${stats.maintenance} en mantenimiento")
                            if (stats.cleaning > 0) {
                                if (stats.maintenance > 0) append(", ")
                                append("${stats.cleaning} en limpieza")
                            }
                        },
                        fontSize = 10.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    // Asegúrate de tener la ruta "booking_management/{hotelId}" definida en tu grafo de navegación
                    navController.navigate("booking_management/${hotel._id}")
                },
                modifier = Modifier.align(Alignment.End) // Alinea el botón a la derecha
            ) {
                Text("Ver Reservas")
        }
        }
    }
}

@Composable
fun EmptyHotelsView(onAddHotel: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No tienes hoteles registrados",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddHotel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF011C21))
            ) {
                Text("Agregar Primer Hotel")
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error: $message",
                fontSize = 16.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}
@Composable
fun RatingDisplay(
    modifier: Modifier = Modifier,
    rating: Int,
    maxRating: Int = 5,starColor: Color = Color(0xFFFFC107) // Un color dorado para las estrellas
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dibuja las estrellas llenas
        for (i in 1..rating) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Estrella llena",
                tint = starColor,
                modifier = Modifier.size(20.dp) // Tamaño de la estrella
            )
        }
        // Dibuja las estrellas vacías
        for (i in (rating + 1)..maxRating) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Estrella vacía",
                tint = starColor.copy(alpha = 0.5f), // Más tenues
                modifier = Modifier.size(20.dp)
            )
        }
    }
}