package com.example.hoteru.view

import MapViewModel
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.model.Booking
import com.example.hoteru.viewModel.LogicBooking
import org.bson.types.ObjectId
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Calendar






@Composable
fun BookingDateFields(
    checkInDate: String,
    onCheckInChange: (String) -> Unit,
    checkOutDate: String,
    onCheckOutChange: (String) -> Unit
) {
    val context = LocalContext.current

    // State for dialogs visibility
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    // Convert strings to calendar objects for date picker defaults
    val calendar = Calendar.getInstance()

    if (showCheckInPicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                onCheckInChange(selected)
                showCheckInPicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showCheckOutPicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                onCheckOutChange(selected)
                showCheckOutPicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // "Fake" text fields to open the picker dialogs
    OutlinedTextField(
        value = checkInDate,
        onValueChange = {},
        label = { Text("Fecha de llegada") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ showCheckInPicker = true },
        readOnly = true
    )

    OutlinedTextField(
        value = checkOutDate,
        onValueChange = {},
        label = { Text("Fecha de salida") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showCheckOutPicker = true },
        readOnly = true
    )
}


@Composable
fun BookingFormNormalUser(
    hotelId: String?,
    roomId: String?,
    userId: String = "pepe"
) {
    val bookingLogic: LogicBooking = viewModel()

    val viewModel: MapViewModel = viewModel()
    val room by viewModel.onehotel.collectAsState()

    val bookingResult by bookingLogic.bookingResult.collectAsState()

    var guestName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }

    LaunchedEffect(bookingResult) {
        if (bookingResult == true) {
            bookingLogic.resetResult()
        }
    }
    LaunchedEffect(roomId) {
        viewModel.loadDetailsDocument("Rooms", roomId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reserva tu habitación",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )

//        // Nombre
//        OutlinedTextField(
//            value = guestName,
//            onValueChange = { guestName = it },
//            label = { Text("Nombre del huésped") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        // Email
//        OutlinedTextField(
//            value = guestEmail,
//            onValueChange = { guestEmail = it },
//            label = { Text("Correo electrónico") },
//            modifier = Modifier.fillMaxWidth()
//        )

        // Campos de fechas
        BookingDateFields(
            checkInDate = checkInDate,
            onCheckInChange = { checkInDate = it },
            checkOutDate = checkOutDate,
            onCheckOutChange = { checkOutDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de confirmación
        Button(
            onClick = {
                try {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val booking = Booking(
                        hotelId = ObjectId(hotelId),
                        roomId = ObjectId(roomId),
                        userId = userId,
                        checkInDate = formatter.parse(checkInDate) ?: Date(),
                        checkOutDate = formatter.parse(checkOutDate) ?: Date(),
                        guestName = guestName,
                        guestEmail = guestEmail,
                        guestPhone = "",
                        totalCost = room?.getInteger("pricePerNight")?.toDouble() ?: 0.0
                    )
                    bookingLogic.makeBooking(booking)
                } catch (e: Exception) {
                    println("❌ Error creating booking: ${e.message}")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Confirmar reserva", color = Color.White)
        }

        // Resultado
        when (bookingResult) {
            true -> Text("✅ Reserva confirmada correctamente.", color = Color.Green)
            false -> Text("❌ Error al realizar la reserva.", color = Color.Red)
            else -> {}
        }
    }
}
