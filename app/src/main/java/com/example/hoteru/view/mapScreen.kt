// ✅ works fine
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBar
import androidx.navigation.compose.composable
import android.Manifest
import android.content.Context
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
fun HeaderOfTheMap() {
    // The main container for the header, using a Surface for background/elevation if needed.
    // In this case, just a simple Row will suffice for the content.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Add padding around the header
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Distribute space between logo/text and notification icon
    ) {
        // 1. Icon and Text/Subtitle Group
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for the Hotel Icon from the image.
            // Replace R.drawable.icono with your actual drawable resource if it's the black hotel icon.
            // If you don't have that specific icon, you might use an Icon with a suitable ImageVector.
            // Since your base uses painterResource, I'll keep that structure, but you need to ensure the drawable exists.

            // NOTE: If you don't have the black icon, you could use:
            // Icon(
            //     imageVector = Icons.Filled.LocationCity, // Use a similar built-in icon
            //     contentDescription = "Hotel Icon",
            //     modifier = Modifier.size(48.dp), // Adjust size as needed
            //     tint = Color.Black
            // )

            // Using Image and a placeholder drawable name from your base code structure:
            Image(
                painter = painterResource(R.drawable.icono), // REPLACE R.drawable.icono with your actual hotel icon drawable
                contentDescription = "HotelHub Icon",
                // The size in the image is quite large for the icon (appears around 48dp or more)
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp)) // Space between icon and text column

            // Text Column (HotelHub and Bienvenido, Usuario)
            Column {
                Text(
                    text = "AndeStay",
                    fontSize = 20.sp, // Adjusted size to better match the image
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // Using black for the main text
                )
                Text(
                    text = "Bienvenido, Usuario",
                    fontSize = 14.sp, // Smaller font for the subtitle
                    color = Color.Gray // Using a gray color for the subtitle
                )
            }
        }

        // 2. Notification Bell Icon with Red Dot
//        NotificationBellIcon()
    }
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
        label = { Text("Busca Un Hotel") },
        modifier = Modifier.fillMaxWidth(),
         placeholder = { Text(text = "Busca Tu Hotel")},
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

fun CloseOrOpenDropDownListMenu(scope: CoroutineScope, drawerState: DrawerState){

    scope.launch {
        if (drawerState.isClosed) {
            drawerState.open()  // 👉 open drawer
        } else {
            drawerState.close() // 👉 close drawer
        }
    }
}
@Composable
fun DropDownListMenu(
    mapModel: MapViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    ModalDrawerSheet {
        Box(modifier = Modifier.fillMaxSize()) {
            // 👉 Close button at the top-right
            IconButton(
                onClick = { CloseOrOpenDropDownListMenu(scope, drawerState) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Menu")
            }

            // 👉 Drawer content below
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp) // give space for the top button
            ) {
                Text("Menu", modifier = Modifier.padding(16.dp))
                Divider()
                NavigationDrawerItem(
                    label = { Text("Motor de busqueda") },
                    selected = false,
                    onClick = {
                        mapModel.visibilitySearchEngine(true)
                        mapModel.visibilityPriceFilter(false)
                        mapModel.visibilityRatingFilter(false)
                        CloseOrOpenDropDownListMenu(scope, drawerState)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Filtro por precios") },
                    selected = false,
                    onClick = {
                        mapModel.visibilitySearchEngine(false)
                        mapModel.visibilityPriceFilter(true)
                        mapModel.visibilityRatingFilter(false)
                        CloseOrOpenDropDownListMenu(scope, drawerState)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Filtro por calificación") },
                    selected = false,
                    onClick = {
                        mapModel.visibilitySearchEngine(false)
                        mapModel.visibilityPriceFilter(false)
                        mapModel.visibilityRatingFilter(true)
                        CloseOrOpenDropDownListMenu(scope, drawerState)
                    }
                )
            }
        }
    }
}



@Composable
fun PriceFilter(navController: NavController) {
    var minimum by remember { mutableStateOf("") }
    var maximum by remember { mutableStateOf("") }

    Column (modifier = Modifier.fillMaxWidth()) {
        // First the row with the text fields
        Row {
            OutlinedTextField(
                value = minimum,
                onValueChange = { minimum = it },
                label = { Text("Min Precio") },
                modifier = Modifier.weight(1f).padding(end = 8.dp, bottom = 16.dp)
            )
            OutlinedTextField(
                value = maximum,
                onValueChange = { maximum = it },
                label = { Text("Max Precio") },
                modifier = Modifier.weight(1f).padding(end = 8.dp, bottom = 16.dp)
            )

        }
        if (minimum.isNotEmpty() && maximum.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally // center everything
            ) {
                // Centered text
                Text(
                    text = "Desde $minimum Hasta $maximum",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black, // black color
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Centered button
                Button(
                    onClick = { navController.navigate("roomsbyprices/$minimum/$maximum") },
                    modifier = Modifier
                        .width(120.dp)  // smaller width
                        .height(36.dp), // smaller height
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black, // button color black
                        contentColor = Color.White // text inside button
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Text(
                        text = "Buscar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }
    }
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RatingFilter(navController: NavController){
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("1⭐", "2⭐", "3⭐", "4⭐", "5⭐")
        var selectedOption by remember { mutableStateOf(options[0]) } // default: 1 star

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Field showing current selection
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Seleccionar Calificacion") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // ✅ adds bottom spacing

            )

            // Dropdown options
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option ) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                            val result = selectedOption.replace("⭐", "")
                            navController.navigate("hotelsbyrating/$result")

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
            selected = currentRoute == "reservations",
            onClick = { navControllerHost.navigate("reservations") },
            icon = { Icon(Icons.Default.Book, contentDescription = "Reservations") },
            label = { Text("Reservaciones") }
        )

        NavigationBarItem(
            selected = currentRoute == "your_hotel",
            onClick = { navControllerHost.navigate("your_hotel") },
            icon = { Icon(Icons.Default.Hotel, contentDescription = "Your Hotel") },
            label = { Text("Tu Hotel") }
        )

        NavigationBarItem(
            selected = currentRoute == "admin_dashboard",
            onClick = { navControllerHost.navigate("admin_dashboard") },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
            label = { Text("Admin") }
        )
    }
}

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
                                color = Color.Black
                            )


                        }
                    }
                    println("selected location:$selectedLocation")
                    print("selected user location:$userLocation")
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
//                        .padding(top = 16.dp)
                        .background(Color.White)
                ) {
                    HeaderOfTheMap()
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
//                            .padding(top = 16.dp)

                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceBetween // Pushes items to edges
                        ) {
                            // LEFT SIDE: Filters section
                            Column(
                                modifier = Modifier
                                    .weight(1f)  // takes remaining space
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

                            Box(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.CenterVertically)
                            ){
                                IconButton(
                                    onClick = {
                                        CloseOrOpenDropDownListMenu(scope, drawerState)
                                    },
                                ) {
                                    Icon(Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        modifier = Modifier
                                            .size(64.dp) // ⬆️ Increases the clickable/touch area
                                            .padding(4.dp))
                                }

                            }

                            // RIGHT SIDE: Menu button

                        }
                    }

                    // 👇 Row that places filters on the left and the button on the right

                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    // Bottom-left arrow IconButton
                    IconButton(
                        onClick = { mapModel.visibilityWindow(true) },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Black, shape = RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Open Hotels",
                            tint = Color.White
                        )
                    }

                    // Central popup window
                    if (visibilityWindow == true) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .padding(15.dp)
//                                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                        ) {
                            IconButton(
                                onClick = { mapModel.visibilityWindow(false) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Black
                                )
                            }

                            DropdownListHotel(navController, hotels, mapModel)


                        }
                    }

                    // Bottom-center "Remove the way" IconButton
                    if (userLocation != null && selectedLocation != null) {
                        IconButton(
                            onClick = { mapModel.updateSelectedLocation(null) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .size(48.dp)
                                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove the way",
                                tint = Color.White
                            )
                        }
                    }
                }



            }

        }
    }
}

















