package com.example.hoteru.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hoteru.model.Hotel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelEditDialog(
    hotel: Hotel,
    isEditing: Boolean,
    onSave: (Hotel) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit

) {
    var currentHotel by remember { mutableStateOf(hotel) }
    //Valida que un formato de 24h
    val timePattern = remember { Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$") }
    val isCheckInTimeValid = remember(currentHotel.checkInTime) {
        timePattern.matcher(currentHotel.checkInTime).matches()
    }
    val isCheckOutTimeValid = remember(currentHotel.checkOutTime) {
        timePattern.matcher(currentHotel.checkOutTime).matches()
    }

    // Calcular si el formulario es válido
    val isFormValid = currentHotel.name.isNotBlank() &&
            currentHotel.address.isNotBlank() &&
            currentHotel.city.isNotBlank() &&
            currentHotel.roomCount > 0 &&
            isCheckInTimeValid && isCheckOutTimeValid
            /*
            currentHotel.location.latitude != 0.0 &&
            currentHotel.location.longitude != 0.0
            */
    // Resetear el estado cuando cambia el hotel
    LaunchedEffect(hotel) {
        currentHotel = hotel
    }

    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentHotel.name.isBlank()) "Nuevo Hotel" else hotel.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF011C21)
                    )

                    if (!isEditing && hotel.name.isNotBlank()) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    HotelEditForm(
                        hotel = currentHotel,
                        onHotelChange = { updatedHotel -> currentHotel = updatedHotel },
                        isCheckInTimeValid = isCheckInTimeValid,
                        isCheckOutTimeValid = isCheckOutTimeValid

                    )
                } else {
                    HotelDetailView(hotel = currentHotel)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isEditing) {
                        TextButton(onClick = onCancel) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(currentHotel) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF011C21)),
                            enabled = isFormValid // Solo se habilita si el formulario es válido
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar")
                        }
                    } else {
                        if (currentHotel.name.isNotBlank()) {
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        TextButton(onClick = onCancel) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HotelEditForm(
    hotel: Hotel,
    onHotelChange: (Hotel) -> Unit,
    isCheckInTimeValid: Boolean,
    isCheckOutTimeValid: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //Campos básicos
        OutlinedTextField(
            value = hotel.name,
            onValueChange = { onHotelChange(hotel.copy(name = it)) },
            label = { Text("Nombre del Hotel") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = hotel.name.isBlank()
        )

        if (hotel.name.isBlank()) {
            Text(
                "El nombre es requerido",
                color = Color(0xFFF44336),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        OutlinedTextField(
            value = hotel.address,
            onValueChange = { onHotelChange(hotel.copy(address = it)) },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = hotel.address.isBlank()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hotel.city,
                onValueChange = { onHotelChange(hotel.copy(city = it)) },
                label = { Text("Ciudad") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = hotel.city.isBlank()
            )

            OutlinedTextField(
                value = hotel.state,
                onValueChange = { onHotelChange(hotel.copy(state = it)) },
                label = { Text("Estado") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = hotel.state.isBlank()
            )
        }

        //Coordenadas en fila
        Text(
            "Coordenada (para el mapa)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF718283),
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hotel.location.latitude.toString(),
                onValueChange = {
                    val lat = it.toDoubleOrNull() ?: 0.0
                    val newLocation = hotel.location.copy(
                        coordinates = listOf(hotel.location.longitude, lat)
                    )
                    onHotelChange(hotel.copy(location = newLocation))
                },
                label = { Text("Latitud") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = hotel.location.latitude == 0.0
            )

            OutlinedTextField(
                value = hotel.location.longitude.toString(),
                onValueChange = {
                    val lon = it.toDoubleOrNull() ?: 0.0
                    val newLocation = hotel.location.copy(
                        coordinates = listOf(lon, hotel.location.latitude)
                    )
                    onHotelChange(hotel.copy(location = newLocation))
                },
                label = { Text("Longitud") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = hotel.location.longitude == 0.0
            )
        }

        if (hotel.location.latitude == 0.0 || hotel.location.longitude == 0.0) {
            Text(
                "Ingrese coordenadas válidas (ej: Lat: 19.4326, Lon: -99.1332)",
                color = Color(0xFFF44336),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        //Habitaciones en fila
        Text(
            "Información de habitaciones",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF718283),
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hotel.roomCount.toString(),
                onValueChange = { newValue ->
                    val count = newValue.toIntOrNull() ?: 0
                    val avaible = if (count > hotel.availableRooms) count else hotel.availableRooms
                    onHotelChange(hotel.copy(roomCount = count, availableRooms = avaible))
        },
        label = { Text("Total de Habitaciones") },
        modifier = Modifier.weight(1f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = hotel.roomCount <= 0
        )

            OutlinedTextField(
                value = hotel.availableRooms.toString(),
                onValueChange = { newValue ->
                    val available = newValue.toIntOrNull() ?: 0
                    // No permite más disponible que el total
                    val finalAvailable = minOf(available, hotel.roomCount)
                    onHotelChange(hotel.copy(availableRooms = finalAvailable))
                },
                label = { Text("Disponibles") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = hotel.availableRooms < 0 || hotel.availableRooms > hotel.roomCount
            )
        }
        // --- Políticas de Horario en Fila ---
        Text(
            "Políticas de Horario",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF718283),
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hotel.checkInTime,
                onValueChange = { onHotelChange(hotel.copy(checkInTime = it)) },
                label = { Text("Hora Check-In") },
                placeholder = { Text("HH:mm") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = !isCheckInTimeValid
            )
            OutlinedTextField(
                value = hotel.checkOutTime,
                onValueChange = { onHotelChange(hotel.copy(checkOutTime = it)) },
                label = { Text("Hora Check-Out") },
                placeholder = { Text("HH:mm") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = !isCheckOutTimeValid
            )
        }
        if (!isCheckInTimeValid || !isCheckOutTimeValid) {
            Text(
                "El formato debe ser HH:mm (ej: 15:00)",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        OutlinedTextField(
            value = hotel.description,
            onValueChange = { onHotelChange(hotel.copy(description = it)) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        OutlinedTextField(
            value = hotel.contactEmail,
            onValueChange = { onHotelChange(hotel.copy(contactEmail = it)) },
            label = { Text("Email de Contacto") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = hotel.contactPhone,
            onValueChange = { onHotelChange(hotel.copy(contactPhone = it)) },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

    }
}

@Composable
fun HotelDetailView(hotel: Hotel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailItem("Nombre", hotel.name)
        DetailItem("Dirección", hotel.address)
        DetailItem("Ciudad", "${hotel.city}, ${hotel.state}")
        DetailItem("Coordenadas", "Lat: ${hotel.location.latitude}, Lon: ${hotel.location.longitude}") // ✅ Mejorado
        DetailItem("Descripción", hotel.description)
        DetailItem("Email", hotel.contactEmail)
        DetailItem("Teléfono", hotel.contactPhone)
        DetailItem("Habitaciones", "${hotel.availableRooms}/${hotel.roomCount} disponibles")
        DetailItem("Hora de Check-In", hotel.checkInTime)
        DetailItem("Hora de Check-Out", hotel.checkOutTime)
        DetailItem("Estado", if (hotel.isActive) "Activo" else "Inactivo")

        if (hotel.amenities.isNotEmpty()) {
            DetailItem("Servicios", hotel.amenities.joinToString(", "))
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.ifBlank { "No especificado" },
            fontSize = 14.sp,
            color = Color(0xFF011C21)
        )
    }
}
