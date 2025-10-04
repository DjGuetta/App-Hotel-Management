package com.example.hoteru.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    user: User?
) {
    // 3. INYECTAR EL VIEWMODEL Y OBTENER LOS ESTADOS
    val viewModel: DashboardViewModel = viewModel()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header (sin cambios)
        Text(
            text = "Panel Administrador",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF011C21)
        )

        Text(
            text = "Bienvenido, ${user?.name ?: "Administrador"}",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 4. PASAR LOS DATOS DINÁMICOS AL COMPOSABLE DE ESTADÍSTICAS
        if (isLoading) {
            // Muestra un indicador de carga mientras se obtienen los datos
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Una vez cargados, muestra las tarjetas con los datos reales
            QuickStatsRow(stats = stats)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Módulos de Gestión (sin cambios)
        Text(
            text = "Módulos de Gestión",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(adminModules) { module ->
                AdminModuleCard(
                    module = module,
                    onCardClick = {
                        when (module.title) {
                            "Gestión de Hoteles" -> navController.navigate("hotel_management")
                            "Gestión de Habitaciones" -> navController.navigate("room_management")
                            "Reservas" -> navController.navigate("global_reservations")
                            "Clientes" -> navController.navigate("customers")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuickStatsRow(stats: DashboardStats) { // 5. EL COMPOSABLE AHORA RECIBE LOS DATOS
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

