package com.example.hoteru.view
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import java.util.*



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.model.*
import com.example.hoteru.viewModel.BookingFormViewModel
import MapViewModel as Mv
import com.example.hoteru.viewModel.MapViewModel
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.example.hoteru.viewModel.GetUser

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BookingEditDialogUser(
//    navController: NavController,
//    bookingToEdit: Booking? = null,
//    onSave: (Booking) -> Unit,
//    hotelId: String,
//    roomId: String,
//    userId: String?,
//) {
//    val bookingViewModel: BookingFormViewModel = viewModel()
//    val hotelViewModel: Mv = viewModel()
//    val roomViewModel: MapViewModel = viewModel()
//    val getUser: GetUser = viewModel()
//
//    val dataH by hotelViewModel.onehotel.collectAsState()
//    val dataR by roomViewModel.onedocument.collectAsState()
//    val userGot by getUser.user.collectAsState()
//
//
//    var checkInDate by remember { mutableStateOf(bookingToEdit?.checkInDate) }
//    var checkOutDate by remember { mutableStateOf(bookingToEdit?.checkOutDate) }
//    var showCheckInDatePicker by remember { mutableStateOf(false) }
//    var showCheckOutDatePicker by remember { mutableStateOf(false) }
//    var bookingText by remember { mutableStateOf(false) }
//
//
//    println("Composable start")
//    println("Initial hotel data: $dataH")
//    println("Initial room data: $dataR")
//
//    // --- Load hotel and room data ---
//    LaunchedEffect(hotelId) {
//        println("Loading hotel...")
//        hotelViewModel.loadDetailsDocument("Hoteles", hotelId)
//    }
//
//    LaunchedEffect(roomId) {
//        println("Loading room...")
//        roomViewModel.loadDetailsDocument("Rooms", roomId)
//    }
//
//    LaunchedEffect(dataH, dataR) {
//        println("Data changed! hotel=$dataH, room=$dataR")
//    }
//    LaunchedEffect(userId) {
//        userId?.let { getUser.loadUserById(it) }
//    }
//
//    // --- Cálculo seguro del roomNumber ---
//    val roomNumber = remember(dataR) {
//        when (val rn = dataR?.get("roomNumber")) {
//            is Long -> rn
//            is Int -> rn.toLong()
//            is String -> rn.toLongOrNull() ?: 0L
//            else -> 0L
//        }
//    }
//
//    // --- Cálculo seguro del totalCost ---
//    val totalCost by remember(dataR, checkInDate, checkOutDate) {
//        derivedStateOf {
//            val room = dataR
//            val cin = checkInDate
//            val cout = checkOutDate
//
//            if (room == null || cin == null || cout == null) {
//                0.0
//            } else {
//                val nights = getNightCount(cin, cout)
//                val pricePerNight = when (val price = room.get("pricePerNight")) {
//                    is Double -> price
//                    is Int -> price.toDouble()
//                    is Long -> price.toDouble()
//                    is String -> price.toDoubleOrNull() ?: 0.0
//                    else -> 0.0
//                }
//                if (nights > 0) pricePerNight * nights else pricePerNight
//            }
//        }
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        shape = MaterialTheme.shapes.large
//    ) {
//        Column(modifier = Modifier.padding(24.dp)) {
//            Text(
//                text = if (bookingToEdit == null) "Nueva Reserva" else "Editar Reserva",
//                style = MaterialTheme.typography.headlineSmall
//            )
//
//            // --- Información de la reserva ---
//            if (dataH != null && dataR != null) {
//                Column {
//                    Text(
//                        text = "Hotel: ${dataH?.getString("name") ?: "N/A"}",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = "Habitación: $roomNumber (${dataR?.getString("roomType") ?: "N/A"})",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            } else {
//                Column {
//                    Text(
//                        text = "Cargando información...",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
//                }
//            }
//
//            // --- Sección de Fechas ---
//            Text(
//                "Fechas de la Estancia",
//                style = MaterialTheme.typography.titleSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                DatePickerFieldUser(
//                    label = "Check-in",
//                    selectedDate = checkInDate,
//                    onClick = { showCheckInDatePicker = true },
//                    modifier = Modifier.weight(1f)
//                )
//                DatePickerFieldUser(
//                    label = "Check-out",
//                    selectedDate = checkOutDate,
//                    onClick = { showCheckOutDatePicker = true },
//                    modifier = Modifier.weight(1f)
//                )
//            }
//
//            // --- Campo de Costo ---
//            OutlinedTextField(
//                value = if (totalCost > 0.0) "$${"%.2f".format(totalCost)}" else "",
//                onValueChange = { },
//                label = { Text("Costo Total") },
//                readOnly = true,
//                modifier = Modifier.fillMaxWidth(),
//                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Costo") }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // --- Botón Guardar ---
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                Button(
//                    onClick = {
//                        if (checkInDate != null && checkOutDate != null && totalCost > 0.0) {
//                            try {
//                                val newBooking = Booking(
//                                    _id = bookingToEdit?._id ?: ObjectId(),
//                                    hotelId = dataH?.getObjectId("_id") ?: ObjectId(hotelId),
//                                    roomId = dataR?.getObjectId("_id") ?: ObjectId(roomId),
//                                    userId = userId ?: "23",
//                                    guestName = userGot!!.firstName,
//                                    guestEmail = userGot!!.email,
//                                    totalCost = totalCost,
//                                    checkInDate = checkInDate!!,
//                                    checkOutDate = checkOutDate!!,
//                                    status = bookingToEdit?.status ?: "CONFIRMED"
//                                )
//                                onSave(newBooking)
//                                bookingText = true
//
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    },
//                    enabled = checkInDate != null && checkOutDate != null && totalCost > 0.0 && dataH != null && dataR != null
//                ) {
//                    Text("Guardar Reserva")
//                }
//
//            }
//            if (bookingText) {
//                Text("Gracias por su reserva!")
//            }else{
//                Text("Reserva ahora!")
//            }
//        }
//
//    }
//
//
//
//    // --- Diálogos de Fecha ---
//    if (showCheckInDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = checkInDate?.time ?: System.currentTimeMillis()
//        )
//        DatePickerDialog(
//            onDismissRequest = { showCheckInDatePicker = false },
//            confirmButton = {
//                TextButton(onClick = {
//                    datePickerState.selectedDateMillis?.let { millis ->
//                        val today = Calendar.getInstance().apply {
//                            set(Calendar.HOUR_OF_DAY, 0)
//                            set(Calendar.MINUTE, 0)
//                            set(Calendar.SECOND, 0)
//                            set(Calendar.MILLISECOND, 0)
//                        }
//                        if (millis >= today.timeInMillis) {
//                            checkInDate = Date(millis)
//                            if (checkOutDate?.before(Date(millis)) == true) checkOutDate = null
//                        }
//                    }
//                    showCheckInDatePicker = false
//                }) { Text("Aceptar") }
//            },
//            dismissButton = { TextButton(onClick = { showCheckInDatePicker = false }) { Text("Cancelar") } }
//        ) { DatePicker(state = datePickerState) }
//    }
//
//    if (showCheckOutDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = checkOutDate?.time ?: checkInDate?.time ?: System.currentTimeMillis()
//        )
//        DatePickerDialog(
//            onDismissRequest = { showCheckOutDatePicker = false },
//            confirmButton = {
//                TextButton(onClick = {
//                    datePickerState.selectedDateMillis?.let { millis ->
//                        val checkInTime = checkInDate?.time ?: 0
//                        if (millis >= checkInTime) checkOutDate = Date(millis)
//                    }
//                    showCheckOutDatePicker = false
//                }) { Text("Aceptar") }
//            },
//            dismissButton = { TextButton(onClick = { showCheckOutDatePicker = false }) { Text("Cancelar") } }
//        ) { DatePicker(state = datePickerState) }
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingEditDialogUser(
    navController: NavController,
    bookingToEdit: Booking? = null,
    onSave: (Booking) -> Unit,
    hotelId: String,
    roomId: String,
    userId: String?,
) {
    val bookingViewModel: BookingFormViewModel = viewModel()
    val hotelViewModel: Mv = viewModel()
    val roomViewModel: MapViewModel = viewModel()
    val getUser: GetUser = viewModel()

    val dataH by hotelViewModel.onehotel.collectAsState()
    val dataR by roomViewModel.onedocument.collectAsState()
    val userGot by getUser.user.collectAsState()

    var checkInDate by remember { mutableStateOf(bookingToEdit?.checkInDate) }
    var checkOutDate by remember { mutableStateOf(bookingToEdit?.checkOutDate) }
    var showCheckInDatePicker by remember { mutableStateOf(false) }
    var showCheckOutDatePicker by remember { mutableStateOf(false) }
    var bookingText by remember { mutableStateOf(false) }

    // --- Load data ---
    LaunchedEffect(hotelId) { hotelViewModel.loadDetailsDocument("Hoteles", hotelId) }
    LaunchedEffect(roomId) { roomViewModel.loadDetailsDocument("Rooms", roomId) }
    LaunchedEffect(userId) { userId?.let { getUser.loadUserById(it) } }

    // --- Safe calculations ---
    val roomNumber = remember(dataR) {
        when (val rn = dataR?.get("roomNumber")) {
            is Long -> rn
            is Int -> rn.toLong()
            is String -> rn.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    val totalCost by remember(dataR, checkInDate, checkOutDate) {
        derivedStateOf {
            val room = dataR
            val cin = checkInDate
            val cout = checkOutDate

            if (room == null || cin == null || cout == null) return@derivedStateOf 0.0

            val nights = getNightCount(cin, cout)
            val pricePerNight = when (val price = room.get("pricePerNight")) {
                is Double -> price
                is Int -> price.toDouble()
                is Long -> price.toDouble()
                is String -> price.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            if (nights > 0) pricePerNight * nights else pricePerNight
        }
    }

    // --- UI ---
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
//            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title
            Text(
                text = if (bookingToEdit == null) "Nueva Reserva" else "Editar Reserva",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )
            )

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Hotel & Room info
            if (dataH != null && dataR != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Hotel",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = dataH?.getString("name") ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF212121),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Habitación",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF757575)
                        )
                    )
                    Text(
                        text = "$roomNumber (${dataR?.getString("roomType") ?: "N/A"})",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF212121)
                        )
                    )
                }
            } else {
                Column {
                    Text(
                        text = "Cargando información...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF757575))
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // Dates
            Text(
                text = "Fechas de la estancia",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF1B1B1B),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerFieldUser(
                    label = "Check-in",
                    selectedDate = checkInDate,
                    onClick = { showCheckInDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerFieldUser(
                    label = "Check-out",
                    selectedDate = checkOutDate,
                    onClick = { showCheckOutDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // Cost
            OutlinedTextField(
                value = if (totalCost > 0.0) "$${"%.2f".format(totalCost)}" else "",
                onValueChange = {},
                label = { Text("Costo Total") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = "Costo",
                        tint = Color(0xFF388E3C)
                    )
                }
            )

            // Save button
            Button(
                onClick = {
                    if (checkInDate != null && checkOutDate != null && totalCost > 0.0) {
                        try {
                            val newBooking = Booking(
                                _id = bookingToEdit?._id ?: ObjectId(),
                                hotelId = dataH?.getObjectId("_id") ?: ObjectId(hotelId),
                                roomId = dataR?.getObjectId("_id") ?: ObjectId(roomId),
                                userId = userId ?: "23",
                                guestName = userGot?.firstName ?: "Usuario",
                                guestEmail = userGot?.email ?: "correo@ejemplo.com",
                                totalCost = totalCost,
                                checkInDate = checkInDate!!,
                                checkOutDate = checkOutDate!!,
                                status = bookingToEdit?.status ?: "CONFIRMED"
                            )
                            onSave(newBooking)
                            bookingText = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                enabled = checkInDate != null && checkOutDate != null && totalCost > 0.0 && dataH != null && dataR != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text(
                    text = "Guardar Reserva",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            AnimatedVisibility(visible = true) {
                Text(
                    text = if (bookingText) "¡Gracias por su reserva!" else "Complete los campos para reservar",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (bookingText) Color(0xFF388E3C) else Color.Gray
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }

    // Date pickers (unchanged)
    if (showCheckInDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = checkInDate?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCheckInDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (millis >= today.timeInMillis) {
                            checkInDate = Date(millis)
                            if (checkOutDate?.before(Date(millis)) == true) checkOutDate = null
                        }
                    }
                    showCheckInDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showCheckInDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showCheckOutDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = checkOutDate?.time ?: checkInDate?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCheckOutDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val checkInTime = checkInDate?.time ?: 0
                        if (millis >= checkInTime) checkOutDate = Date(millis)
                    }
                    showCheckOutDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showCheckOutDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun DatePickerFieldUser(
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

// Función auxiliar para contar noches
private fun getNightCount(checkIn: Date, checkOut: Date): Long {
    val start = Calendar.getInstance().apply {
        time = checkIn
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        time = checkOut
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (end.before(start) || end == start) return 0
    val diffInMillis = end.timeInMillis - start.timeInMillis
    return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
}