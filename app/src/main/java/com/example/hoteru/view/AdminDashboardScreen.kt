package com.example.hoteru.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.model.DashboardStats
import com.example.hoteru.model.User
import com.example.hoteru.viewModel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = viewModel(),
    user: User?
) {
    val stats by dashboardViewModel.stats.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val hotels by dashboardViewModel.hotels.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            dashboardViewModel.loadDashboardData(it.id)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Espacio para separar de la TopAppBar

            Text(
                text = "Bienvenido, ${user?.fullName ?: "Administrador"}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(), // Ocupa todo el espacio restante
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            } else {
                // Una vez que la carga finaliza, mostramos TODO el contenido.
                Column {
                    QuickStatsRow(stats = stats)

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 16.dp)
                    ) {
                        items(adminModules) { module ->
                            AdminModuleCard(
                                module = module,
                                onCardClick = {
                                    when (module.title) {
                                        "Gestión de Hoteles" -> navController.navigate("hotel_management")
                                        "Gestión de Habitaciones" -> navController.navigate("room_management")
                                        "Reservas" -> {
                                            when {
                                                hotels.size == 1 -> {
                                                    val singleHotelId =
                                                        hotels.first()._id.toHexString()
                                                    navController.navigate("booking_management/$singleHotelId")
                                                }

                                                else -> {
                                                    navController.navigate("hotel_management")
                                                }
                                            }
                                        }

                                        "Clientes" -> navController.navigate("customers")
                                    }
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
fun QuickStatsRow(stats: DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickStatCard(
            title = "Hoteles",
            value = stats.totalHotels.toString(), // Usa el dato dinámico
            color = Color(0xFF4CAF50)
        )

        QuickStatCard(
            title = "Habitaciones",
            value = stats.totalRooms.toString(), // Usa el dato dinámico
            color = Color(0xFF2196F3)
        )

        QuickStatCard(
            title = "Reservas Hoy",
            value = stats.reservationsToday.toString(), // Usa el dato dinámico
            color = Color(0xFFFF9800)
        )
    }
}

// El resto de tus Composables (QuickStatCard, AdminModuleCard) y data classes (AdminModule)
// no necesitan ningún cambio y se quedan exactamente igual.

@Composable
fun QuickStatCard(title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AdminModuleCard(module: AdminModule, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = null,
                tint = Color(0xFF011C21),
                modifier = Modifier.padding(end = 16.dp)
            )

            Column {
                Text(
                    text = module.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = module.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Data Classes
data class AdminModule(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val adminModules = listOf(
    AdminModule(
        title = "Gestión de Hoteles",
        description = "Administra tus hoteles y sucursales",
        icon = Icons.Default.Hotel
    ),
    AdminModule(
        title = "Gestión de Habitaciones",
        description = "Gestiona habitaciones, precios y disponibilidad",
        icon = Icons.Default.Bed
    ),
    AdminModule(
        title = "Reservas",
        description = "Ver y gestionar reservas activas",
        icon = Icons.Default.Receipt
    ),
    AdminModule(
        title = "Clientes",
        description = "Administrar información de clientes",
        icon = Icons.Default.Person
    )
)

