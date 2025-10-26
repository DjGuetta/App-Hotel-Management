package com.example.hoteru.view

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.hoteru.viewModel.MapViewModel as mp

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
        modifier = Modifier.fillMaxWidth(),
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
fun ListOfHotels(navController: NavController, authViewModel: AuthViewModel = viewModel()){
    val mapModel: MapViewModel = viewModel()
    val hotels by mapModel.hotels.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    if (hotels.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // spacing between hotels
        ) {
            items(hotels) { hotel ->
                val id = when (val value = hotel["_id"]) {
                    is ObjectId -> value.toHexString()   // if it's ObjectId
                    is String -> value                    // if it's String
                    else -> null                          // fallback if _id is missing
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Black, RoundedCornerShape(12.dp)), // black border
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White), // white background
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    onClick = {
                        navController.navigate("detailshotel/${hotel.get("_id")}/${authState.loginSuccess?.firstName ?: "Usuario"}/${authState.loginSuccess?.id ?: "idUsuario"}")
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, // center horizontally
                        verticalArrangement = Arrangement.Center // center vertically
                    ) {
                        // Hotel Name
                        Text(
                            text = hotel.getString("name") ?: "Not found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        // Hotel Description
                        Text(
                            text = hotel.getString("description") ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(8.dp))

                        // Hotel Images (horizontal scroll)
                        val images = hotel.get("images") as? List<String> ?: emptyList()
                        if (images.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(images) { imageBase64 ->
                                    Box(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.LightGray)
                                    ) {
                                        Base64Image(
                                            base64 = imageBase64,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.LightGray)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No images available",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }

                }
            }
        }
    } else {
        Text("no hay hoteles registrados actualmente")
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