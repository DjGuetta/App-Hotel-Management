package com.example.hoteru.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hoteru.model.User
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    user: User?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
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

        // Estadísticas Rápidas
        QuickStatsRow()

        Spacer(modifier = Modifier.height(24.dp))

        // Módulos de Gestión
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
fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickStatCard(
            title = "Hoteles",
            value = "3",
            color = Color(0xFF4CAF50)
        )

        QuickStatCard(
            title = "Habitaciones",
            value = "45",
            color = Color(0xFF2196F3)
        )

        QuickStatCard(
            title = "Reservas Hoy",
            value = "12",
            color = Color(0xFFFF9800)
        )
    }
}

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