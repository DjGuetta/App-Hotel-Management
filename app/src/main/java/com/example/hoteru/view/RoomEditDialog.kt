package com.example.hoteru.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.hoteru.model.Room
import org.bson.types.ObjectId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomEditDialog(
    room: Room?,
    hotelId: ObjectId,
    onDismiss: () -> Unit,
    onSave: (Room) -> Unit
) {
    var roomNumber by remember { mutableStateOf(room?.roomNumber ?: "") }
    var roomType by remember { mutableStateOf(room?.roomType ?: "Individual") }
    var pricePerNight by remember { mutableStateOf(room?.pricePerNight?.toString() ?: "") }
    var description by remember { mutableStateOf(room?.description ?: "") }
    var capacity by remember { mutableStateOf(room?.capacity?.toString() ?: "1") }
    var status by remember { mutableStateOf(room?.status ?: "Available") }
    var amenities by remember { mutableStateOf(room?.amenities?.joinToString(", ") ?: "") }

    val roomTypes = listOf("Individual", "Doble", "Suite", "Familiar", "Presidencial")
    val statusTranslations = remember {
        mapOf(
            "Available" to "Disponible",
            "Occupied" to "Ocupada",
            "Maintenance" to "En Mantenimiento",
            "Cleaning" to "Limpieza"
        )
    }

    // --- Variables de estado para los menús desplegables ---
    var isRoomTypeExpanded by remember { mutableStateOf(false) }
    var isStatusExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (room == null) "Agregar Habitación" else "Editar Habitación",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Formulario
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Número de habitación") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- TIPO DE HABITACIÓN  ---
                ExposedDropdownMenuBox(
                    expanded = isRoomTypeExpanded,
                    onExpandedChange = { isRoomTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = {},
                        label = { Text("Tipo de habitación") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(), //Ancla el menú al TextField
                        readOnly = true,
                        trailingIcon = { //Añade el icono de la flecha
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoomTypeExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = isRoomTypeExpanded,
                        onDismissRequest = { isRoomTypeExpanded = false }
                    ) {
                        roomTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    roomType = type
                                    isRoomTypeExpanded = false //Cierra el menú al seleccionar
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pricePerNight,
                    onValueChange = { pricePerNight = it },
                    label = { Text("Precio por noche") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacidad") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- ESTADO ---
                ExposedDropdownMenuBox(
                    expanded = isStatusExpanded,
                    onExpandedChange = { isStatusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = statusTranslations[status]?: status,
                        onValueChange = {},
                        label = { Text("Estado") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = isStatusExpanded,
                        onDismissRequest = { isStatusExpanded = false }
                    ) {
                        statusTranslations.forEach { (statusInEnglish, statusInSpanish) ->
                            DropdownMenuItem(
                                text = { Text(statusInSpanish) },
                                onClick = {
                                    status = statusInEnglish
                                    isStatusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amenities,
                    onValueChange = { amenities = it },
                    label = { Text("Servicios (separados por coma)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("WiFi, TV, A/C, Minibar") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val newRoom = Room(
                            _id = room?._id ?: ObjectId(),
                            hotelId = hotelId,
                            roomNumber = roomNumber,
                            roomType = roomType,
                            pricePerNight = pricePerNight.toDoubleOrNull() ?: 0.0,
                            description = description,
                            amenities = amenities.split(",").map { it.trim() }.filter { it.isNotEmpty() }, // <--- MEJORA: Evita servicios vacíos
                            capacity = capacity.toIntOrNull() ?: 1,
                            status = status
                        )
                        onSave(newRoom)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = roomNumber.isNotEmpty() && pricePerNight.isNotEmpty()
                ) {
                    Text(if (room == null) "Agregar Habitación" else "Actualizar Habitación")
                }
            }
        }
    }
}
