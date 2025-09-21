@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.example.hoteru.view

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.app.viewmodel.MapViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import org.w3c.dom.Document

@Composable
fun Base64Image(base64: String){
    if (base64.isEmpty()) {
        // show placeholder if no image
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No image")
        }
    }else{
        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//    imageBytes
//    → This is your raw binary data of the image, stored in a ByteArray.
//    It’s what you got from:
//
//    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
//
//
//    offset (here 0)
//    → This tells Android from where in the byte array to start reading.
//
//    0 means: start from the very first byte.
//
//    If you had some extra header data at the beginning, you could skip some bytes by setting this to another number.
//
//    length (here imageBytes.size)
//    → This is how many bytes to read starting from offset.
//
//    imageBytes.size means: read the entire array.
//
//    You could also pass a smaller number if you only wanted to decode part of the data.
//
//    So effectively:
//
//    👉 BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//    = “Take the whole imageBytes array, starting at the first byte, and decode it into a Bitmap.”
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Hotel image",
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp)
        )

    }



}
@Composable
fun DetailsHotelScreen(navController: NavController, hotelId: String?){
    val viewHotel: MapViewModel = viewModel()

    val hotel by viewHotel.onehotel.collectAsState()
    LaunchedEffect(hotelId){
        viewHotel.loadDetailsHotel("Hotel", hotelId)
    }
    val images = hotel?.get("images") as? List<String> ?: emptyList()
    print(images)
//    val Base64Im = images?.firstOrNull()
//    val base64 = Base64Im?.getString("number_1")
    Column {
        Text(text = hotel?.getString("name") ?: "not found..")
        Text(text = hotel?.getString("description") ?: "")
        LazyColumn {
                items(images) { Base64Image(it) }
        }



    }


}

