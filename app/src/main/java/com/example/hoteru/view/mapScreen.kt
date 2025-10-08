// ✅ works fine
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBar
import androidx.navigation.compose.composable

import android.Manifest
import android.content.Context
import android.graphics.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Hotel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController


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
                            val coordinatesAny = location?.get("coordinates") as? List<*>

                            val lat = coordinatesAny?.getOrNull(1)?.toString()?.toDoubleOrNull() ?: 0.0
                            val lon = coordinatesAny?.getOrNull(0)?.toString()?.toDoubleOrNull() ?: 0.0


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
                            val location = hotel.get("location", Document::class.java)
                            val coordinatesAny = location?.get("coordinates") as? List<*>
                            val lat = coordinatesAny?.getOrNull(1)?.toString()?.toDoubleOrNull() ?: 0.0
                            val lon = coordinatesAny?.getOrNull(0)?.toString()?.toDoubleOrNull() ?: 0.0
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
    mapModel: MapViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState
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
        Button(
            onClick = {
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()  // 👉 open drawer
                    } else {
                        drawerState.close() // 👉 close drawer
                    }
                }
            }
        ) {
            Text("X")
        }

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
fun BottomNavigationBar(navControllerHost: NavController) {
    val currentRoute = navControllerHost.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navControllerHost.navigate("home") },
            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
            label = { Text("Mapa") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            selected = currentRoute == "your_hotel",
            onClick = { navControllerHost.navigate("your_hotel") },
            icon = { Icon(Icons.Default.Hotel, contentDescription = "Your Hotel") },
            label = { Text("Tu Hotel") }
        )

        NavigationBarItem(
            selected = currentRoute == "reservations",
            onClick = { navControllerHost.navigate("reservations") },
            icon = { Icon(Icons.Default.Book, contentDescription = "Reservations") },
            label = { Text("Reservaciones") }
        )
    }
}

//@Composable
//fun SlotOfNavigationBar(navControllerHost: Unit) {
//    Scaffold(
//        bottomBar = { BottomNavigationBar(navControllerHost) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navControllerHost,
//            startDestination = "home",
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable("home") { MapScreen(navControllerHost) }
//
//        }
//    }
//}
@Composable
fun MapScreen(navController: NavController) {
    val mapModel: MapViewModel = viewModel()
    val userModel: UserLocation = viewModel()
    val selectedLocation by mapModel.selectedLocation.collectAsState()
    val userLocation by userModel.userLocation.collectAsState()
    val visibilityWindow by mapModel.visibilityWindow.collectAsState()
    val visibilitySearchEngine by mapModel.visibilitySearchEngine.collectAsState()
    val visibilityPriceFilter by mapModel.visibilityPriceFilter.collectAsState()
    val visibilityRatingFilter by mapModel.visibilityRatingFilter.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        userModel.updateLocation()
    }

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
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) } // ✅ bottom navigation visible in MapScreen
    ) { innerPadding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false, // 🚫 disables swipe gesture
            drawerContent = {
                DropDownListMenu(mapModel = mapModel, scope, drawerState)
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                        val location = hotel.get("location", Document::class.java)
                        val coordinatesAny = location?.get("coordinates") as? List<*>
                        val id = when (val value = hotel["_id"]) {
                            is ObjectId -> value.toHexString()   // if it's ObjectId
                            is String -> value                    // if it's String
                            else -> null                          // fallback if _id is missing
                        }
                        val lat = coordinatesAny?.getOrNull(1)?.toString()?.toDoubleOrNull() ?: 0.0
                        val lon = coordinatesAny?.getOrNull(0)?.toString()?.toDoubleOrNull() ?: 0.0
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
                    userLocation?.let { current ->
                        selectedLocation?.let { dest ->
                            println("User Location: $current")
                            println("Destination: $dest")
                            mapModel.visibilityButton(true)
                            Marker(
                                state = rememberMarkerState(position = current),
                                icon = bitmapDescriptorFromVector(
                                    LocalContext.current,
                                    R.drawable.user
                                ),
                                title = "Estas aqui",
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
                    Button(
                        onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()  // 👉 open drawer
                                } else {
                                    drawerState.close() // 👉 close drawer
                                }
                            }
                        }
                    ) {
                        Text("=")
                    }
                }
                Box(
                    modifier = Modifier
                    .matchParentSize()
                ){
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
    }
}

















