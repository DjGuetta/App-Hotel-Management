package com.example.hoteru.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.model.*
import com.example.hoteru.viewModel.BookingFormViewModel
import org.bson.types.ObjectId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingEditDialog(
    bookingToEdit: Booking? = null,
    onDismiss: () -> Unit,
    onSave: (Booking) -> Unit
) {
    val viewModel: BookingFormViewModel = viewModel()

    // --- Estados del formulario ---
    var guestName by remember { mutableStateOf(bookingToEdit?.guestName ?: "") }
    var guestEmail by remember { mutableStateOf(bookingToEdit?.guestEmail ?: "") }
    var totalCost by remember { mutableStateOf(bookingToEdit?.totalCost?.toString() ?: "") }
    var userId by remember { mutableStateOf(bookingToEdit?.userId ?: "") }

    // --- Estados para los menús desplegables ---
    val hotels by viewModel.hotels.collectAsState()
    val availableRooms by viewModel.availableRooms.collectAsState()

    var selectedHotel by remember { mutableStateOf<Hotel?>(null) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }

    // Cargar el hotel y la habitación si estamos editando
    LaunchedEffect(bookingToEdit) {
        if (bookingToEdit != null) {
            viewModel.loadDataForEditing(bookingToEdit)
        }
    }

    // Observar los datos cargados para edición
    val initialHotel by viewModel.initialHotel.collectAsState()
    val initialRoom by viewModel.initialRoom.collectAsState()
    LaunchedEffect(initialHotel, initialRoom) {
        selectedHotel = initialHotel
        selectedRoom = initialRoom
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (bookingToEdit == null) "Nueva Reserva" else "Editar Reserva",
                    style = MaterialTheme.typography.headlineSmall
                )

                // --- Selector de Hotel ---
                ExposedDropdownMenu(
                    label = "Hotel",
                    items = hotels,
                    selectedItem = selectedHotel,
                    onItemSelected = { hotel ->
                        selectedHotel = hotel
                        selectedRoom = null // Reiniciar la habitación al cambiar de hotel
                        viewModel.loadAvailableRoomsForHotel(hotel._id)
                    },
                    itemToString = { it.name }
                )

                // --- Selector de Habitación (solo si se ha seleccionado un hotel) ---
                if (selectedHotel != null) {
                    ExposedDropdownMenu(
                        label = "Habitación Disponible",
                        items = availableRooms,
                        selectedItem = selectedRoom,
                        onItemSelected = { room -> selectedRoom = room },
                        itemToString = { "Hab. ${it.roomNumber} (${it.roomType})" }
                    )
                }

                // --- Campos de texto ---
                OutlinedTextField(value = guestName, onValueChange = { guestName = it }, label = { Text("Nombre del Huésped") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = guestEmail, onValueChange = { guestEmail = it }, label = { Text("Email del Huésped") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("ID de Usuario") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = totalCost, onValueChange = { totalCost = it }, label = { Text("Costo Total") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                // --- TODO: Añadir selectores de fecha (Check-in / Check-out) ---
                // Por simplicidad, usaremos la fecha actual. En una app real, usarías un DatePickerDialog.

                Spacer(modifier = Modifier.height(16.dp))

                // --- Botones de acción ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalRoom = selectedRoom
                            val finalHotel = selectedHotel
                            if (finalRoom != null && finalHotel != null) {
                                val newBooking = Booking(
                                    _id = bookingToEdit?._id ?: ObjectId(),
                                    hotelId = finalHotel._id,
                                    roomId = finalRoom._id,
                                    userId = userId,
                                    guestName = guestName,
                                    guestEmail = guestEmail,
                                    totalCost = totalCost.toDoubleOrNull() ?: 0.0,
                                    checkInDate = Date(), // Placeholder
                                    checkOutDate = Date(), // Placeholder
                                    status = bookingToEdit?.status ?: "CONFIRMED"
                                )
                                onSave(newBooking)
                            }
                        },
                        // Habilitar el botón solo si todos los campos están llenos
                        enabled = selectedHotel != null && selectedRoom != null && guestName.isNotBlank() && totalCost.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

// Composable de ayuda genérico para menús desplegables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdownMenu(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem?.let(itemToString) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
