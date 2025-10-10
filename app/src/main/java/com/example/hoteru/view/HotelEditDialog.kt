package com.example.hoteru.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hoteru.model.Hotel
import com.example.hoteru.viewModel.HotelManagementViewModel
import java.util.regex.Pattern

// --- COMPOSABLE HELPER PARA EL PHOTO PICKER ---
@Composable
private fun rememberImagePickerLauncher(
    onImagesSelected: (List<ByteArray>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val imageByteArrays = uris.mapNotNull { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    } catch (e: Exception) {
                        Log.e("ImagePicker", "Error convirtiendo Uri a ByteArray: ${e.message}", e)
                        null
                    }
                }
                onImagesSelected(imageByteArrays)
            }
        }
    )
    return {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
@Composable
private fun rememberBitmapFromBase64(base64: String): Bitmap? {
    return remember(base64) {
        try {
            val imageBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("BitmapConverter", "Error decodificando Base64: ${e.message}")
            null
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelEditDialog(
    hotel: Hotel,
    isEditing: Boolean,
    onSave: (Hotel) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    viewModel: HotelManagementViewModel
) {
    val currentHotelFromVM by viewModel.selectedHotel.observeAsState()
    val currentHotel = currentHotelFromVM ?: hotel

    val timePattern = remember { Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$") }
    val isCheckInTimeValid = remember(currentHotel.checkInTime) {
        timePattern.matcher(currentHotel.checkInTime).matches()
    }
    val isCheckOutTimeValid = remember(currentHotel.checkOutTime) {
        timePattern.matcher(currentHotel.checkOutTime).matches()
    }

    val isFormValid = currentHotel.name.isNotBlank() &&
            currentHotel.address.isNotBlank() &&
            currentHotel.city.isNotBlank() &&
            currentHotel.roomCount > 0 &&
            isCheckInTimeValid && isCheckOutTimeValid

    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // El Column ahora es consciente de los cambios en currentHotel
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header (Sin cambios)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentHotel.name.isBlank()) "Nuevo Hotel" else currentHotel.name,
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
                        onHotelChange = { updatedHotel -> viewModel.updateSelectedHotel(updatedHotel) },
                        isCheckInTimeValid = isCheckInTimeValid,
                        isCheckOutTimeValid = isCheckOutTimeValid,
                        onAddImages = { byteArrays -> viewModel.addImages(byteArrays) }
                    )
                } else {
                    HotelDetailView(hotel = currentHotel)
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción (Sin cambios)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isEditing) {
                        TextButton(onClick = onCancel) { Text("Cancelar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(currentHotel) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF011C21)),
                            enabled = isFormValid
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
                        TextButton(onClick = onCancel) { Text("Cerrar") }
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
    isCheckOutTimeValid: Boolean,
    onAddImages: (List<ByteArray>) -> Unit
) {
    val launchImagePicker = rememberImagePickerLauncher { byteArrays ->
        onAddImages(byteArrays)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Campos de texto básicos ---
        OutlinedTextField(
            value = hotel.name,
            onValueChange = { onHotelChange(hotel.copy(name = it)) },
            label = { Text("Nombre del Hotel") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = hotel.name.isBlank()
        )
        //Campos como dirección, ciudad, estado
        OutlinedTextField(
            value = hotel.address,
            onValueChange = { onHotelChange(hotel.copy(address = it)) },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = hotel.address.isBlank()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        // --- Coordenadas ---
        Text(
            "Coordenada (para el mapa)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF718283),
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hotel.location.latitude.toString(),
                onValueChange = {
                    val lat = it.toDoubleOrNull() ?: hotel.location.latitude
                    onHotelChange(hotel.copy(location = hotel.location.copy(coordinates = listOf(hotel.location.longitude, lat))))
                },
                label = { Text("Latitud") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = hotel.location.longitude.toString(),
                onValueChange = {
                    val lon = it.toDoubleOrNull() ?: hotel.location.longitude
                    onHotelChange(hotel.copy(location = hotel.location.copy(coordinates = listOf(lon, hotel.location.latitude))))
                },
                label = { Text("Longitud") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        // --- Habitaciones ---
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
                onValueChange = { onHotelChange(hotel.copy(roomCount = it.toIntOrNull() ?: 0)) },
                label = { Text("Total de Habitaciones") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = hotel.roomCount <= 0
            )
        }

        //Campos como check-in, descripción, etc.
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

        // <<< VISUALIZACIÓN DE IMÁGENES >>>
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Imágenes del Hotel",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF718283)
        )
        if (hotel.images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(hotel.images) { base64Image ->
                    val bitmap = rememberBitmapFromBase64(base64 = base64Image)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Imagen del hotel",
                            modifier = Modifier
                                .size(100.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        } else {
            Text(
                "Aún no hay imágenes cargadas.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        OutlinedButton(
            onClick = { launchImagePicker() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir Imágenes")
        }
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
        DetailItem("Coordenadas", "Lat: ${hotel.location.latitude}, Lon: ${hotel.location.longitude}")
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
