package com.example.hoteru.view

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hoteru.R
import com.example.hoteru.model.Hotel
import androidx.compose.material.icons.filled.ExitToApp
import com.example.hoteru.model.map_data.tachiraBounds
import com.example.hoteru.model.map_data.tachiraLatLng
import com.example.hoteru.viewModel.AuthState
import com.example.hoteru.viewModel.AuthViewModel
import com.example.hoteru.viewModel.MapViewModel
import com.example.hoteru.viewModel.UserLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight

import com.google.android.gms.maps.model.MapStyleOptions

// --- FUNCIONES DE CONVERSIÓN ---

@Composable
fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return BitmapDescriptorFactory.defaultMarker()

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun HeaderOfTheMap(userName: String, onLogoutClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.icono),
                    contentDescription = "AndeStay Icon",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AndeStay",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Bienvenido, $userName",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            // --- BOTÓN DE LOGOUT ---
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar Sesión"
                )
            }

        }

        Box( modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x33000000),
                        Color(0x11000000),
                        Color.Transparent)
                )
            )
        )
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
        onValueChange = mapModel::onSearchTextChange,
        label = { Text("Busca Un Hotel") },
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        placeholder = { Text(text = "Busca Tu Hotel") },
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (isSearching) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (searchText.isNotBlank()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            items(hotels) { hotel ->
                Text(
                    text = hotel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val lat = hotel.location.coordinates[1]
                            val lon = hotel.location.coordinates[0]
                            mapModel.moveCameraTo(cameraPositionState, LatLng(lat, lon))
                            mapModel.onSearchTextChange("")
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DropdownListHotel(navController: NavController, DropDownItems: List<Hotel>, mapModel: MapViewModel){

    var isExpanded by remember { mutableStateOf(false) }
    var selectedText by rememberSaveable { mutableStateOf(DropDownItems.firstOrNull()?.name ?: "") }
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
                    text = {Text( hotel.name)},
                    onClick = {
                        if (locationPermissionState.status.isGranted){
                            val location = hotel.location
                            val coordinatesAny = location.coordinates as? List<*>
                            val lat = coordinatesAny?.getOrNull(1)?.toString()?.toDoubleOrNull() ?: 0.0
                            val lon = coordinatesAny?.getOrNull(0)?.toString()?.toDoubleOrNull() ?: 0.0
                            if (lat != null && lon != null) {
                                // ✅ Instead of calling DrawWay directly, update state
//                            selectedLocation = LatLng(lat, lon)
                                selectedText = hotel.name
                                mapModel.updateSelectedLocation(LatLng(lat, lon)) // ✅ updates ViewModel state
                                mapModel.setVisibilityWindow(false)
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



    fun CloseOrOpenDropDownListMenu(scope: CoroutineScope, drawerState: DrawerState) {
        scope.launch {
            if (drawerState.isClosed) {
                drawerState.open()
            } else {
                drawerState.close()
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)) {
                IconButton(
                    onClick = { CloseOrOpenDropDownListMenu(scope, drawerState) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Menu")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp)
                ) {
                    Text("Menu", modifier = Modifier.padding(16.dp))
                    Divider()
                    NavigationDrawerItem(
                        label = { Text("Motor de búsqueda") },
                        selected = false,
                        onClick = {
                            mapModel.setVisibilitySearchEngine(true)
                            mapModel.setVisibilityPriceFilter(false)
                            mapModel.setVisibilityRatingFilter(false)
                            CloseOrOpenDropDownListMenu(scope, drawerState)
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Filtro por precios") },
                        selected = false,
                        onClick = {
                            mapModel.setVisibilitySearchEngine(false)
                            mapModel.setVisibilityPriceFilter(true)
                            mapModel.setVisibilityRatingFilter(false)
                            CloseOrOpenDropDownListMenu(scope, drawerState)
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Filtro por calificación") },
                        selected = false,
                        onClick = {
                            mapModel.setVisibilitySearchEngine(false)
                            mapModel.setVisibilityPriceFilter(false)
                            mapModel.setVisibilityRatingFilter(true)
                            CloseOrOpenDropDownListMenu(scope, drawerState)
                        }
                    )
                }
            }
        }
    }

@Composable
fun PriceFilter(navController: NavController, user: String?) {
    var minimum by remember { mutableStateOf("") }
    var maximum by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp)) {
        Row {
            OutlinedTextField(
                value = minimum,
                onValueChange = { minimum = it },
                label = { Text("Min Precio") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp, bottom = 16.dp)
            )
            OutlinedTextField(
                value = maximum,
                onValueChange = { maximum = it },
                label = { Text("Max Precio") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp, bottom = 16.dp)
            )
        }
        if (minimum.isNotEmpty() && maximum.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Desde $minimum Hasta $maximum",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { navController.navigate("roomsbyprices/$minimum/$maximum/$user") },
                    modifier = Modifier
                        .width(120.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
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
fun RatingFilter(navController: NavController) {
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("1⭐", "2⭐", "3⭐", "4⭐", "5⭐")
        var selectedOption by remember { mutableStateOf(options[0]) } // default: 1 star

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().padding(top = 15.dp)
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
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
fun BottomNavigationBar(navControllerHost: NavController, authViewModel: AuthViewModel = viewModel()) {
    val currentRoute = navControllerHost.currentBackStackEntryAsState().value?.destination?.route
    val authState by authViewModel.authState.collectAsState()

    // Usamos la propiedad `loginSuccess` que SÍ existe en tu AuthState.
    val loggedInUser = authState.loginSuccess

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp
    ) {

        // --- Item 1: Mapa ---
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navControllerHost.navigate("home") },
            icon = { Icon(Icons.Default.Place, contentDescription = "Mapa") },
            label = { Text("Mapa") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Gray,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Gray,
                unselectedTextColor = Color.Black
            )
        )

        // --- Item 2: Hoteles ---
        NavigationBarItem(
            selected = currentRoute == "listOfHotels",
            onClick = { navControllerHost.navigate("listOfHotels") },
            icon = { Icon(Icons.Default.Hotel, contentDescription = "Hoteles") },
            label = { Text("Hoteles") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Gray,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Gray,
                unselectedTextColor = Color.Black
            )
        )

        // --- Item 2: Reservaciones ---
        NavigationBarItem(
            selected = currentRoute == "user_bookings",
            onClick = { navControllerHost.navigate("user_bookings")},
            icon = { Icon(Icons.Default.Book, contentDescription = "Reservations") },
            label = { Text("Reservaciones") }
        )

        // Esto asegura que el item se dibuje como parte de la barra.
        if (loggedInUser?.isAdmin == true) {
            NavigationBarItem(
                selected = currentRoute?.startsWith("admin_dashboard") ?: false,
                onClick = {
                    navControllerHost.navigate("admin_dashboard/${loggedInUser.id}")
                },
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                label = { Text("Admin") }
            )
        }

    }
}
@Composable
fun MapScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val mapModel: MapViewModel = viewModel()
    val userModel: UserLocation = viewModel()

    val hotels by mapModel.hotels.collectAsState()
    val selectedLocation by mapModel.selectedLocation.collectAsState()
    val userLocation by userModel.userLocation.collectAsState()
    val visibilityWindow by mapModel.visibilityWindow.collectAsState()
    val visibilitySearchEngine by mapModel.visibilitySearchEngine.collectAsState()
    val visibilityPriceFilter by mapModel.visibilityPriceFilter.collectAsState()
    val visibilityRatingFilter by mapModel.visibilityRatingFilter.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            userModel.updateLocation()
        } catch (e: SecurityException) {
            println("SecurityException: Permiso de ubicación no concedido. ${e.message}")
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(tachiraLatLng, 10f)
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(mapToolbarEnabled = false)
        )
    }
    val context = LocalContext.current
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navControllerHost = navController, authViewModel) }
    ) { innerPadding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
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
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        latLngBoundsForCameraTarget = tachiraBounds,
                        minZoomPreference = 8f,
                        maxZoomPreference = 30f,
                        mapStyleOptions = mapStyleOptions
                    ),
                    uiSettings = uiSettings
                ) {
                    hotels.forEach { hotel ->
                        val lat = hotel.location.coordinates[1]
                        val lon = hotel.location.coordinates[0]
                        val position = LatLng(lat, lon)
                        val markerState = rememberMarkerState(position = position)

                        MarkerInfoWindow(
                            state = markerState,
                            icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.h),
                            onInfoWindowClick = {
                                navController.navigate("detailshotel/${hotel._id.toHexString()}/${authState.loginSuccess?.firstName ?: "Usuario"}/${authState.loginSuccess?.id ?: "idUsuario"}")
                            }
                        ) { marker ->
                            Column(
                                modifier = Modifier
                                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .shadow(4.dp)
                            ) {
                                Text(text = hotel.name, fontWeight = FontWeight.Bold)
                                Text(text = hotel.description, maxLines = 2)
                            }
                        }
                    }
                    userLocation?.let { current ->
                        selectedLocation?.let { dest ->
                            println("User Location: $current")
                            println("Destination: $dest")
                            mapModel.setVisibilityButton(true)
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

                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    LaunchedEffect(authState.loginSuccess) {
                        if (authState.loginSuccess == null) {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }

                    // Header with working logout button
                    HeaderOfTheMap(
                        userName = authState.loginSuccess?.firstName ?: "Usuario",
                        onLogoutClick = { authViewModel.logout() } // ✅ triggers state reset
                    )


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (visibilitySearchEngine) SearchEngine(cameraPositionState)
                            if (visibilityPriceFilter) PriceFilter(navController, authState.loginSuccess?.id)
                            if (visibilityRatingFilter) RatingFilter(navController)
                        }
                        IconButton(onClick = { CloseOrOpenDropDownListMenu(scope, drawerState) }) {
                            Icon(Icons.Default.Menu, "Menu", modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = { mapModel.setVisibilityWindow(true) },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Black, shape = RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.ArrowUpward, "Open Hotels", tint = Color.White)
                    }

                    if (visibilityWindow) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .padding(16.dp)

                        ) {
                            IconButton(
                                onClick = { mapModel.setVisibilityWindow(false) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Close, "Close", tint = Color.Black)
                            }
                            DropdownListHotel(navController, hotels, mapModel)
                        }
                    }

                    if (userLocation != null && selectedLocation != null) {
                        IconButton(
                            onClick = { mapModel.updateSelectedLocation(null) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .size(48.dp)
                                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Close, "Remove the way", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}