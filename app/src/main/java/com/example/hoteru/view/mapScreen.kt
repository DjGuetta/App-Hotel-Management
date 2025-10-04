// ✅ works fine
package com.example.hoteru.view

import android.Manifest
import android.content.Context
import android.graphics.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.viewmodel.MapViewModel
import com.example.hoteru.R
import com.example.hoteru.model.map_data.tachiraBounds
import com.example.hoteru.model.map_data.tachiraLatLng
import com.example.hoteru.viewModel.UserLocation
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.bson.Document
import org.bson.types.ObjectId
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.navigation.Navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


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

@Composable
fun SearchEngine(cameraPositionState: CameraPositionState) {
    val mapModel: MapViewModel = viewModel()
    val searchText by mapModel.searchText.collectAsState()
    val hotels by mapModel.filteredHotels.collectAsState()
    val isSearching by mapModel.isSearching.collectAsState()
     OutlinedTextField(
        value = searchText,
        onValueChange =  mapModel::onSearchTextChange, // updates the state
//         onValueChange = { newText -> mapModel.onSearchTextChange(searchText) }
        label = { Text("Search a hotel") },
        modifier = Modifier.fillMaxWidth(),
         placeholder = { Text(text = "Search your hotel")},
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(modifier = Modifier
        .fillMaxWidth()) {
        if (searchText.isNotBlank()) {
            items(hotels){ hotel ->
                Text(
                    text = "${hotel.getString("name")}",
                    modifier = Modifier
                        .clickable{
                            val location = hotel.get("location", Document::class.java)
                            println(hotel.getString("name"))
                            println(location)

                            // Safe calls with Elvis operator
                            val lat = location?.getDouble("latitude") ?: return@clickable
                            val lon = location?.getDouble("longitude") ?: return@clickable
//
//                        mapModel.setCameraTarget(LatLng(lat, lon))
                            mapModel.moveCamerato( cameraPositionState,LatLng(lat, lon))
                        }


                )
            }
        }

    }


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DropdownListHotel(navController: NavController, DropDownItems: List<Document>, mapModel: MapViewModel){
    var isExpanded by remember { mutableStateOf(false) }
    var selectedText by rememberSaveable { mutableStateOf(DropDownItems.firstOrNull()?.getString("name") ?: "") }
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {isExpanded = !isExpanded}
    ){
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select hotel") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            modifier = Modifier
                .menuAnchor()   // ✅ REQUIRED
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropDownItems.forEach { hotel ->
                DropdownMenuItem(
                    text = {Text( hotel.getString("name"))},
                    onClick = {
                        if (locationPermissionState.status.isGranted){
                            val location = hotel.get("location", Object::class.java) as? Document
                            val lat = location?.getDouble("latitude")
                            val lon = location?.getDouble("longitude")
                            if (lat != null && lon != null) {
                                // ✅ Instead of calling DrawWay directly, update state
//                            selectedLocation = LatLng(lat, lon)
                                selectedText = hotel.getString("name")
                                mapModel.updateSelectedLocation(LatLng(lat, lon)) // ✅ updates ViewModel state
                                mapModel.visibilityWindow(false)
//
                        }} else {
                            // 🚨 Ask for permission
                            locationPermissionState.launchPermissionRequest()
                        }

                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )

            }
        }

    }
}
@Composable
fun DropDownListMenu(
    mapModel: MapViewModel
){
    ModalDrawerSheet {
        Text("Menu", modifier = Modifier.padding(16.dp))
        Divider()
        NavigationDrawerItem(
            label = { Text("Motor de busqueda") },
            selected = false,
            onClick = {
                mapModel.visibilitySearchEngine (true)
                mapModel.visibilityPriceFilter (false)
                mapModel.visibilityRatingFilter (false)
            }
        )
        NavigationDrawerItem(
            label = { Text("Filtro por precios") },
            selected = false,
            onClick = {
                mapModel.visibilitySearchEngine (false)
                mapModel.visibilityPriceFilter (true)
                mapModel.visibilityRatingFilter (false)
            }
        )

        NavigationDrawerItem(
            label = { Text("Filtro por calificacion") },
            selected = false,
            onClick = {
                mapModel.visibilitySearchEngine (false)
                mapModel.visibilityPriceFilter (false)
                mapModel.visibilityRatingFilter (true)
            }
        )

    }

    }


@Composable
fun PriceFilter(navController: NavController) {
    var minimum by remember { mutableStateOf("") }
    var maximum by remember { mutableStateOf("") }

    Column {
        // First the row with the text fields
        Row {
            OutlinedTextField(
                value = minimum,
                onValueChange = { minimum = it },
                label = { Text("Min Price") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = maximum,
                onValueChange = { maximum = it },
                label = { Text("Max Price") },
                modifier = Modifier.weight(1f)
            )
        }
        if (minimum.isNotEmpty() && maximum.isNotEmpty()) {
            Text("desde $minimum hasta $maximum")
            Button(
                onClick = {
                    navController.navigate("roomsbyprices/$minimum/$maximum")
                }
            ) {
                Text("Buscar")
            }
        }


    }


    }
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RatingFilter(navController: NavController){
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("1", "2", "3", "4", "5")
        var selectedOption by remember { mutableStateOf(options[0]) } // default: 1 star

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            // Field showing current selection
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select rating") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            // Dropdown options
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                            navController.navigate("hotelsbyrating/$selectedOption")

                        }
                    )
                }
            }
        }
    }



@Composable
fun MapScreen(navController: NavController) {
    // Create a new instance of the MapViewModel, which holds and manages hotel data as state.
// Note: Instantiating manually means no lifecycle management (not recommended for production).
    val mapModel: MapViewModel = viewModel()
    val userModel: UserLocation = viewModel()
    val selectedLocation by mapModel.selectedLocation.collectAsState()
    val userLocation by userModel.userLocation.collectAsState()
    val visibilityWindow by mapModel.visibilityWindow.collectAsState()
    val visibilitySearchEngine by mapModel.visibilitySearchEngine.collectAsState()
    val visibilityPriceFilter by mapModel.visibilityPriceFilter.collectAsState()
    val visibilityRatingFilter by mapModel.visibilityRatingFilter.collectAsState()


    LaunchedEffect(Unit) {
        userModel.updateLocation()
    }


// Collect the StateFlow 'hotels' from the ViewModel as a Compose State,
// then delegate its current value to the variable 'hotels'.
// This makes 'hotels' reactive in the UI, triggering recomposition when the data changes.
    val hotels by mapModel.hotels.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(tachiraLatLng, 10f)
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false // 🚫 removes the toolbar (blue arrow + Maps icon)
            )
        )
    }
    ModalNavigationDrawer(
        drawerState = rememberDrawerState(DrawerValue.Closed),
        drawerContent = {
            DropDownListMenu(mapModel = mapModel)
        }
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            GoogleMap(
                Modifier.fillMaxSize(), cameraPositionState, properties = MapProperties(
                    latLngBoundsForCameraTarget = tachiraBounds,
                    minZoomPreference = 8f,
                    maxZoomPreference = 16f
                ),
                uiSettings = uiSettings


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
                        val markerState = rememberMarkerState(position = LatLng(lat, lon))

                        MarkerInfoWindow(
                            state = markerState,
                            icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.h),
                            onInfoWindowClick = {
                                // Whole info window clicked
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
                userLocation?.let { current ->
                    selectedLocation?.let { dest ->
                        println("User Location: $current")
                        println("Destination: $dest")
                        mapModel.visibilityButton(true)
                        Marker(
                            state = rememberMarkerState(position = current),
                            icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.user),
                            title = "You are here",
                        )

                        // Polyline from user to selected hotel
                        Polyline(
                            points = listOf(current, dest),
                            color = Color.Blue,
                        )


                    }
                }
                println("selected location:$selectedLocation")
                print("selected user location:$userLocation")

            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.White)
            ) {
                if (visibilitySearchEngine == true) {
                    SearchEngine(cameraPositionState)
                }
                if (visibilityPriceFilter == true) {
                    PriceFilter(navController)
                }
                if (visibilityRatingFilter == true) {
                    RatingFilter(navController)
                }
            }
            Button(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.White),
                onClick = {
                    mapModel.visibilityWindow(true)
                }
            ) {
                Text(text = "->")
            }

            if (visibilityWindow == true) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                        .background(Color.White),
                ) {
                    DropdownListHotel(navController, hotels, mapModel)
                    Button(onClick = {
                        mapModel.visibilityWindow(false)
                    }) {
                        Text("X")
                    }

                }

            }
            if (userLocation != null && selectedLocation != null) {
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    onClick = { mapModel.updateSelectedLocation(null) } // remove the way
                ) {
                    Text("Remove the way")
                }
            }
        }

    }

    }












