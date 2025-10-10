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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hoteru.model.Room
import com.example.hoteru.viewModel.RoomManagementViewModel

// --- HELPERS (Funciones de Ayuda para este fichero) ---

@Composable
private fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val byteArrays = uris.mapNotNull { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    } catch (e: Exception) { null }
                }
                onImagesSelected(byteArrays)
            }
        }
    )
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

@Composable
private fun rememberBitmapFromBase64(base64String: String): Bitmap? {
    return remember(base64String) {
        try {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("Base64Decoder", "Error decodificando Base64: ${e.message}")
            null
        }
    }
}

// --- DIÁLOGO PRINCIPAL ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomEditDialog(
    room: Room,
    viewModel: RoomManagementViewModel,
    onDismiss: () -> Unit
) {
    val currentRoom by viewModel.selectedRoom.collectAsState()
    val roomForForm = currentRoom ?: room

    val roomTypes = listOf("Individual", "Doble", "Matrimonial", "Suite", "Familiar", "Apartamento")
    var isRoomTypeExpanded by remember { mutableStateOf(false) }

    val launchImagePicker = rememberImagePickerLauncher { byteArrays ->
        viewModel.addImages(byteArrays)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (roomForForm.roomNumber.isBlank()) "Nueva Habitación" else "Editar Habitación #${roomForForm.roomNumber}",
                    style = MaterialTheme.typography.titleLarge
                )

                // --- FORMULARIO ---
                OutlinedTextField(
                    value = roomForForm.roomNumber,
                    onValueChange = { viewModel.updateSelectedRoom(roomForForm.copy(roomNumber = it)) },
                    label = { Text("Número de Habitación") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = isRoomTypeExpanded,
                    onExpandedChange = { isRoomTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roomForForm.roomType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Habitación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoomTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = isRoomTypeExpanded, onDismissRequest = { isRoomTypeExpanded = false }) {
                        roomTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    viewModel.updateSelectedRoom(roomForForm.copy(roomType = type))
                                    isRoomTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = if (roomForForm.pricePerNight == 0.0) "" else roomForForm.pricePerNight.toString(),
                    onValueChange = { viewModel.updateSelectedRoom(roomForForm.copy(pricePerNight = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Precio por Noche") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = if (roomForForm.capacity == 0) "" else roomForForm.capacity.toString(),
                    onValueChange = { viewModel.updateSelectedRoom(roomForForm.copy(capacity = it.toIntOrNull() ?: 0)) },
                    label = { Text("Capacidad (personas)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = roomForForm.description,
                    onValueChange = { viewModel.updateSelectedRoom(roomForForm.copy(description = it)) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                // --- SECCIÓN DE IMÁGENES ---
                Spacer(modifier = Modifier.height(16.dp))
                Text("Imágenes", fontWeight = FontWeight.Bold)

                if (roomForForm.images.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(roomForForm.images) { base64Image ->
                            val bitmap = rememberBitmapFromBase64(base64String = base64Image)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Imagen de la habitación",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp)), // Para redondear la imagen
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "No hay imágenes cargadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                OutlinedButton(onClick = { launchImagePicker() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Imagen")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Imagen")
                }

                // --- BOTONES DE ACCIÓN ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = { viewModel.saveRoom(roomForForm) }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
