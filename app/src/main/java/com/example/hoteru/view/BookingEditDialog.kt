package com.example.hoteru.view

import androidx.compose.animation.core.copy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingEditDialog(
    bookingToEdit: Booking? = null,
    onDismiss: () -> Unit,
    onSave: (Booking) -> Unit,
    currentAdminId: String
) {
    val viewModel: BookingFormViewModel = viewModel()

    // --- Estados del formulario ---
    var guestName by remember { mutableStateOf(bookingToEdit?.guestName ?: "") }
    var guestEmail by remember { mutableStateOf(bookingToEdit?.guestEmail ?: "") }
    var guestPhone by remember { mutableStateOf(bookingToEdit?.guestPhone ?: "") }

    // --- Estados para menús y fechas ---
    val hotels by viewModel.hotels.collectAsState()
    val availableRooms by viewModel.availableRooms.collectAsState()
    var selectedHotel by remember { mutableStateOf<Hotel?>(null) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var checkInDate by remember { mutableStateOf(bookingToEdit?.checkInDate) }
    var checkOutDate by remember { mutableStateOf(bookingToEdit?.checkOutDate) }
    var showCheckInDatePicker by remember { mutableStateOf(false) }
    var showCheckOutDatePicker by remember { mutableStateOf(false) }

    // --- Lógica de carga ---
    LaunchedEffect(bookingToEdit) {
        if (bookingToEdit != null) {
            viewModel.loadDataForEditing(bookingToEdit)
        }
    }
    val initialHotel by viewModel.initialHotel.collectAsState()
    val initialRoom by viewModel.initialRoom.collectAsState()
    LaunchedEffect(initialHotel, initialRoom) {
        selectedHotel = initialHotel
        selectedRoom = initialRoom
    }

    // <<< 1. CÁLCULO DE COSTO ROBUSTO CON derivedStateOf >>>
    val totalCost by remember(selectedRoom, checkInDate, checkOutDate) {
        derivedStateOf {
            val room = selectedRoom
            val cin = checkInDate
            val cout = checkOutDate
            if (room != null && cin != null && cout != null) {
                val nights = getNightCount(cin, cout)
                if (nights > 0) {
                    (room.pricePerNight * nights)
                } else {
                    // Si las fechas son inválidas o es una sola noche, cobrar al menos una noche
                    room.pricePerNight
                }
            } else {
                0.0 // Si falta algún dato, el costo es 0
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

                // --- Sección de Selección ---
                ExposedDropdownMenu(
                    label = "Hotel",
                    items = hotels,
                    selectedItem = selectedHotel,
                    onItemSelected = { hotel ->
                        selectedHotel = hotel
                        selectedRoom = null
                        viewModel.loadAvailableRoomsForHotel(hotel._id)
                    },
                    itemToString = { it.name }
                )
                if (selectedHotel != null) {
                    ExposedDropdownMenu(
                        label = "Habitación Disponible",
                        items = availableRooms,
                        selectedItem = selectedRoom,
                        onItemSelected = { room -> selectedRoom = room },
                        itemToString = { "Hab. ${it.roomNumber} (${it.roomType})" }
                    )
                }

                // --- Sección de Datos del Huésped ---
                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Nombre del Huésped") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = guestEmail,
                    onValueChange = { guestEmail = it },
                    label = { Text("Email del Huésped") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = guestPhone,
                    onValueChange = { guestPhone = it },
                    label = { Text("Teléfono del Huésped") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Sección de Fechas ---
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Fechas de la Estancia",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        label = "Check-in",
                        selectedDate = checkInDate,
                        onClick = { showCheckInDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "Check-out",
                        selectedDate = checkOutDate,
                        onClick = { showCheckOutDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- Campo de Costo ---
                OutlinedTextField(
                    value = if (totalCost > 0.0) totalCost.toString() else "",
                    onValueChange = { /* No se puede cambiar */ },
                    label = { Text("Costo Total") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Costo") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Botones de acción ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedHotel != null && selectedRoom != null && checkInDate != null && checkOutDate != null) {
                                val newBooking = Booking(
                                    _id = bookingToEdit?._id ?: ObjectId(),
                                    hotelId = selectedHotel!!._id,
                                    roomId = selectedRoom!!._id,
                                    userId = bookingToEdit?.userId ?: currentAdminId,
                                    guestName = guestName,
                                    guestEmail = guestEmail,
                                    guestPhone = guestPhone,
                                    totalCost = totalCost,
                                    checkInDate = checkInDate!!,
                                    checkOutDate = checkOutDate!!,
                                    status = bookingToEdit?.status ?: "CONFIRMED"
                                )
                                onSave(newBooking)
                            }
                        },
                        enabled = selectedHotel != null && selectedRoom != null && guestName.isNotBlank() && checkInDate != null && checkOutDate != null && totalCost > 0.0
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }

    // --- Diálogos de Selección de Fecha ---
    if (showCheckInDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCheckInDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        // Convierte los milisegundos UTC a un objeto Date que respeta el día seleccionado
                        val selectedDate =
                            Date(utcMillis + java.util.TimeZone.getDefault().getOffset(utcMillis))
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0); set(
                            Calendar.MINUTE,
                            0
                        ); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.time

                        if (!selectedDate.before(today)) {
                            checkInDate = selectedDate
                            if (checkOutDate?.before(checkInDate) == true) {
                                checkOutDate = null
                            }
                        }
                    }
                    showCheckInDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCheckOutDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (checkInDate?.time ?: System.currentTimeMillis())
        )
        DatePickerDialog(
            onDismissRequest = { showCheckOutDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val selectedDate =
                            Date(utcMillis + java.util.TimeZone.getDefault().getOffset(utcMillis))
                        val checkInDateStartOfDay = checkInDate?.let {
                            Calendar.getInstance().apply {
                                time = it
                                set(Calendar.HOUR_OF_DAY, 0); set(
                                Calendar.MINUTE,
                                0
                            ); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                            }.time
                        } ?: Date(0)

                        if (!selectedDate.before(checkInDateStartOfDay)) {
                            checkOutDate = selectedDate
                        }
                    }
                    showCheckOutDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckOutDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


// <<< 2. CAMPO DE FECHA CON BOX, LA SOLUCIÓN CORRECTA >>>
@Composable
fun DatePickerField(
    label: String,
    selectedDate: Date?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateText = selectedDate?.let { dateFormatter.format(it) } ?: ""

    Box(modifier = modifier) {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
            }
        )
        // Capa transparente "clicable" que cubre todo el campo de texto
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}


private fun getNightCount(checkIn: Date, checkOut: Date): Long {
    // Clona las fechas para no modificar las originales y resetea la hora
    val start = Calendar.getInstance().apply {
        time = checkIn
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        time = checkOut
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    if (end.before(start) || end == start) return 0
    val diffInMillis = end.timeInMillis - start.timeInMillis
    return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
}


// ... El resto del código (ExposedDropdownMenu) se mantiene igual ...
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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
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
