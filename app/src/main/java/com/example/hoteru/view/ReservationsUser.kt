package com.example.hoteru.view

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.model.Booking
import com.example.hoteru.model.User
import com.example.hoteru.viewModel.BookinsUserExpose
import com.example.hoteru.viewModel.GetUser
import com.example.hoteru.viewModel.MapViewModel
import org.bson.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import MapViewModel as Mv

@Composable
fun UserBookingsScreen(
    userId: String?,
    bookingViewModel: BookinsUserExpose = viewModel()
) {
    val bookings by bookingViewModel.userBookings.collectAsState()
    val loading by bookingViewModel.loading.collectAsState()
    val error by bookingViewModel.error.collectAsState()

    // Load bookings when screen starts
    LaunchedEffect(userId) {
        bookingViewModel.loadBookingsByUser(userId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Mis Reservas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(
                    text = error ?: "Error desconocido",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            bookings.isEmpty() -> {
                Text(
                    text = "No se encontraron reservas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bookings) { booking ->
                        BookingCard(booking)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking) {
    val context = LocalContext.current
    val hotelViewModel: Mv = viewModel()
    val roomViewModel: MapViewModel = viewModel()
    val getUser: GetUser = viewModel()

    val dataH by hotelViewModel.onehotel.collectAsState()
    val dataR by roomViewModel.onedocument.collectAsState()
    val userGot by getUser.user.collectAsState()

    LaunchedEffect(booking.hotelId) {
        hotelViewModel.loadDetailsDocument("Hoteles", booking.hotelId.toString())
    }

    LaunchedEffect(booking.roomId) {
        roomViewModel.loadDetailsDocument("Rooms", booking.roomId.toString())
    }

    LaunchedEffect(booking.userId) {
        booking.userId?.let { getUser.loadUserById(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nombre del usuario: ${userGot?.firstName ?: "Desconocido"}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Hotel: ${dataH?.getString("name") ?: "No disponible"}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Número de Habitación: ${dataR?.getString("roomNumber") ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Tipo de Habitación: ${dataR?.getString("roomType") ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            Text(
                text = "Check-in: ${dateFormat.format(booking.checkInDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Check-out: ${dateFormat.format(booking.checkOutDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Costo total: $${"%.2f".format(booking.totalCost)}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF388E3C))
            )
            Text(
                text = "Estado: ${booking.status}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    createBookingPdf(context, booking, dataH, dataR, userGot)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Descargar PDF")
            }
        }
    }
}

/**
 * Generates a PDF for a booking and saves it in the device's Downloads folder.
 */
fun createBookingPdf(
    context: Context,
    booking: Booking,
    hotelData: Document?,
    roomData: Document?,
    userData: User?
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    var y = 80f
    paint.textSize = 22f
    paint.isFakeBoldText = true
    canvas.drawText("Resumen de Reserva", 180f, y, paint)

    paint.textSize = 16f
    paint.isFakeBoldText = false
    y += 40f
    canvas.drawText("Usuario: ${userData?.firstName ?: "Desconocido"}", 40f, y, paint)
    y += 25f
    canvas.drawText("Hotel: ${hotelData?.getString("name") ?: "No disponible"}", 40f, y, paint)
    y += 25f
    canvas.drawText("Habitación #: ${roomData?.getString("roomNumber") ?: "N/A"}", 40f, y, paint)
    y += 25f
    canvas.drawText("Tipo de Habitación: ${roomData?.getString("roomType") ?: "N/A"}", 40f, y, paint)
    y += 25f
    canvas.drawText("Check-in: ${booking.checkInDate}", 40f, y, paint)
    y += 25f
    canvas.drawText("Check-out: ${booking.checkOutDate}", 40f, y, paint)
    y += 25f
    canvas.drawText("Costo Total: $${"%.2f".format(booking.totalCost)}", 40f, y, paint)
    y += 25f
    canvas.drawText("Estado: ${booking.status}", 40f, y, paint)
    y += 40f
    paint.textSize = 12f
    canvas.drawText("Generado por AndeStay App", 220f, y, paint)

    pdfDocument.finishPage(page)

    try {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "Reserva_${booking._id}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    } finally {
        pdfDocument.close()
    }
}
