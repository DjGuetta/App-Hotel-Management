// ✅ works fine

import android.content.Context
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.viewmodel.MapViewModel
import com.example.hoteru.R
import com.example.hoteru.model.map_data.tachiraBounds
import com.example.hoteru.model.map_data.tachiraLatLng
import com.example.hoteru.viewModel.UserLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import org.bson.Document
import org.bson.types.ObjectId



@Composable
fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
): BitmapDescriptor {
    // Get the vector image from resources (or throw error if not found)
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return BitmapDescriptorFactory.defaultMarker()
    // Create a blank bitmap the same size as the drawable
    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicWidth)
    // Create a blank bitmap the same size as the drawable
    Canvas(bitmap).apply {
        drawable.setBounds(0,0,width,height)
        drawable.draw(this)
    }
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
//@Composable
//fun moveCamerato(location: LatLng) {
//    val mapModel: MapViewModel = viewModel()
//    LaunchedEffect(location) {
//        location.let {
//            mapModel.cameraPositionState.animate(
//                update = CameraUpdateFactory.newLatLngZoom(location, 15f),
//                durationMs = 1000
//            )
//        }
//    }
//}

@Composable
fun SearchEngine() {
    val mapModel: MapViewModel = viewModel()
    val searchText by mapModel.searchText.collectAsState()
    val hotels by mapModel.filteredHotels.collectAsState()
    val isSearching by mapModel.isSearching.collectAsState()
     OutlinedTextField(
        value = searchText,
        onValueChange =  mapModel::onSearchTextChange, // updates the state
        label = { Text("Search a hotel") },
        modifier = Modifier.fillMaxWidth(),
         placeholder = { Text(text = "Search your hotel")},
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(modifier = Modifier
        .fillMaxWidth()) {
        items(hotels){ hotel ->
            Text(
                text = "${hotel.getString("name")}",
                modifier = Modifier
                    .clickable{
                        val location = hotel.get("location", Document::class.java)
                        println(hotel.getString("name"))

                        // Safe calls with Elvis operator
                        val lat = location?.getDouble("latitude") ?: return@clickable
                        val lon = location?.getDouble("longitude") ?: return@clickable
//                        mapModel.setCameraTarget(LatLng(lat, lon))
                        mapModel.moveCamerato( LatLng(lat, lon))
                    }


                )
        }
    }


}
@Composable
fun MapScreen(navController: NavController) {
    // Create a new instance of the MapViewModel, which holds and manages hotel data as state.
// Note: Instantiating manually means no lifecycle management (not recommended for production).
    val mapModel: MapViewModel = viewModel()
    val userModel: UserLocation = viewModel()

// Collect the StateFlow 'hotels' from the ViewModel as a Compose State,
// then delegate its current value to the variable 'hotels'.
// This makes 'hotels' reactive in the UI, triggering recomposition when the data changes.
    val hotels by mapModel.hotels.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(tachiraLatLng, 10f)
    }

    // Use LaunchedEffect to react to changes in the target location

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        GoogleMap(
            Modifier.fillMaxSize(), mapModel.cameraPositionState, properties = MapProperties(
                latLngBoundsForCameraTarget = tachiraBounds,
                minZoomPreference = 8f,
                maxZoomPreference = 16f
            )
        ) {

            hotels.forEach { hotel ->
                val location = hotel.get("location", Object::class.java) as? Document
                val id = when (val value = hotel["_id"]) {
                    is ObjectId -> value.toHexString()   // if it's ObjectId
                    is String -> value                    // if it's String
                    else -> null                          // fallback if _id is missing
                }
                println(id)
                println("MapScreen")
                if (location != null) {
                    val lat = location.getDouble("latitude")
                    val lon = location.getDouble("longitude")
//                    val hotelLocation = LatLng(lat, lon)
                    val markerState = rememberMarkerState(position = LatLng(lat, lon))
//                    val userLatLng = cameraPositionState.position.target

//                    Marker(
//                        state = markerState,
//                        icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.h),
//                        title = hotel.getString("name"),
//                        snippet = hotel.getString("description"),
////                        onClick = {
////                            navController.navigate("detailshotel/${id}")
////                            true
////                        }
//                    )
                    MarkerInfoWindow(
                        state = markerState,
                        icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.h),
                        onInfoWindowClick = { marker ->
                            // user tapped anywhere on the info window -> navigate
                            navController.navigate("detailshotel/$id")
                        }
                    ) { marker ->
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(8.dp)
                        ) {
                            Text(text = hotel.getString("name") ?: "")
                            Text(text = hotel.getString("description") ?: "")

                        }
                    }


                }

            }


        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.White)
        ) {
            SearchEngine()

        }

    }
}




