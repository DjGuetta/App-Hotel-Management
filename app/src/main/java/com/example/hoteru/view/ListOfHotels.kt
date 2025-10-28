package com.example.hoteru.view
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import MapViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.hoteru.viewModel.MapViewModel as mp
import com.example.hoteru.viewModel.logicComments
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hoteru.R
import com.example.hoteru.model.map_data.tachiraBounds
import com.example.hoteru.viewModel.AuthViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberMarkerState
import org.bson.types.ObjectId


@Composable
fun SearchEngineListOfHotels(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val mapModel: mp = viewModel()
    val searchText by mapModel.searchText.collectAsState()
    val hotels by mapModel.filteredHotels.collectAsState()
    val isSearching by mapModel.isSearching.collectAsState()
    val authState by authViewModel.authState.collectAsState()

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
//                            val lat = hotel.location.coordinates[1]
//                            val lon = hotel.location.coordinates[0]
//                            mapModel.moveCameraTo(cameraPositionState, LatLng(lat, lon))
                            navController.navigate("detailshotel/${hotel._id.toHexString()}/${authState.loginSuccess?.firstName ?: "Usuario"}/${authState.loginSuccess?.id ?: "idUsuario"}")

                            mapModel.onSearchTextChange("")
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ListOfHotels(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val mapModel: MapViewModel = viewModel()
    val comments: logicComments = viewModel()
    val hotels by mapModel.hotels.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val commentsByHotel by comments.commentsByHotel.collectAsState()

    LaunchedEffect(hotels) {
        hotels.forEach { hotel ->
            val id = when (val value = hotel["_id"]) {
                is ObjectId -> value.toHexString()
                is String -> value
                else -> null
            }
            id?.let { comments.loadCommentsByHotel(it) }
        }
    }

    if (hotels.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(hotels){ index, hotel ->
                val id = when (val value = hotel["_id"]) {
                    is ObjectId -> value.toHexString()
                    is String -> value
                    else -> null
                }

                val name = hotel.getString("name") ?: "Hotel sin nombre"
                val address = hotel.getString("address") ?: "Dirección no disponible"

                // Safely parse rating (can be Int, Double, or String)
                val rating: Double = try {
                    when (val r = hotel["rating"]) {
                        is Int -> r.toDouble()
                        is Double -> r
                        is String -> r.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                } catch (e: Exception) {
                    0.0
                }

                val commentCount = id?.let { commentsByHotel[it]?.size ?: 0 } ?: 0

                // Safely get list of images
                val images: List<String> = try {
                    hotel.get("images") as? List<String> ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                // Safely get amenities list
                val features: List<String> = try {
                    hotel.getList("amenities", String::class.java) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        .clickable {
                            navController.navigate(
                                "detailshotel/${hotel.get("_id")}/${authState.loginSuccess?.firstName ?: "Usuario"}/${authState.loginSuccess?.id ?: "idUsuario"}"
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column {
                        // --- Image Section ---
                        if (images.isNotEmpty()) {
                            Base64Image(
                                base64 = images.first(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No image", color = Color.DarkGray)
                            }
                        }

                        // --- Info Section ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Ubicación",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // --- Rating Row ---
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", rating),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${commentCount} reseñas)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            // --- Amenities Row ---
                            if (features.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = features.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF616161)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay hoteles registrados actualmente")
        }
    }
}



@Composable
fun ListOfHotelsScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val mapModel: mp = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val visibilityWindow by mapModel.visibilityWindow.collectAsState()
    val visibilitySearchEngine by mapModel.visibilitySearchEngine.collectAsState()
    val visibilityPriceFilter by mapModel.visibilityPriceFilter.collectAsState()
    val visibilityRatingFilter by mapModel.visibilityRatingFilter.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navControllerHost = navController,
                authViewModel = authViewModel
            )
        }
    ) { innerPadding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                DropDownListMenu(
                    mapModel = mapModel,
                    scope = scope,
                    drawerState = drawerState
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {


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
                            if (visibilitySearchEngine) SearchEngineListOfHotels(navController, authViewModel)
                            if (visibilityPriceFilter) PriceFilter(navController, authState.loginSuccess?.id)
                            if (visibilityRatingFilter) RatingFilter(navController)
                        }
                        IconButton(onClick = { CloseOrOpenDropDownListMenu(scope, drawerState) }) {
                            Icon(Icons.Default.Menu, "Menu", modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ListOfHotels(navController, authViewModel)
                }


            }
        }
    }
}