package com.example.hoteru.routes
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hoteru.view.DetailsRoomScreen
import com.example.hoteru.view.HotelsByRating
import com.example.hoteru.view.RoomsByPrice
import com.example.hoteru.view.AdminDashboardScreen
import com.example.hoteru.view.LoginScreen
import com.example.hoteru.view.HotelManagementScreen
import com.example.hoteru.view.RoomManagementScreen
import com.example.hoteru.view.ReservationsScreen
import com.example.hoteru.view.CustomersScreen
import com.example.hoteru.viewModel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.view.GlobalReservationsScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.hoteru.view.BookingManagementScreen
import com.example.hoteru.view.DetailsHotelScreen
import MapScreen

import com.example.hoteru.view.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                navController = navController
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen(
                navController = navController,
                user = null
            )
        }

        // Nuevas rutas para el admin
        composable("hotel_management") {
            HotelManagementScreen(navController = navController)
        }
        // -- RUTA CON ARGUMENTOS --
        composable(
            route = "booking_management/{hotelId}", // Define la ruta con un placeholder
            arguments = listOf(navArgument("hotelId") { type = NavType.StringType }) // Define el tipo del argumento
        ) { backStackEntry ->
            // Extrae el argumento de la ruta
            val hotelId = backStackEntry.arguments?.getString("hotelId")

            // Llama a la pantalla pasándole el ID
            if (hotelId != null) {
                BookingManagementScreen(navController = navController, hotelId = hotelId)
            } else {
                // Opcional: Maneja el caso de que el ID sea nulo (ej. volver atrás)
                navController.popBackStack()
            }
        }

        composable("registerUser") {
            RegisterScreen(navController = navController)
        }

        composable("room_management") {
            RoomManagementScreen(navController = navController)
        }
        composable("global_reservations") {
            GlobalReservationsScreen(navController = navController)
        }

        composable("customers") {
            CustomersScreen(navController = navController)
        }

        composable("home") {
            MapScreen(navController)
        }
        composable("detailshotel/{_id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("_id")
            DetailsHotelScreen(navController, id)
        }
        composable("detailsroom/{_idroom}/{_idhotel}") { backStackEntry ->
                val idroom  = backStackEntry.arguments?.getString("_idroom")
                val idhotel = backStackEntry.arguments?.getString("_idroom")

                DetailsRoomScreen(navController, idroom, idhotel)
        }
        composable("roomsbyprices/{minimun}/{maximun}") { backStackEntry ->
            val minimun = backStackEntry.arguments?.getString("minimun")
            val maximun = backStackEntry.arguments?.getString("maximun")
            RoomsByPrice(navController, minimun, maximun)
        }
        composable("hotelsbyrating/{rating}") { backStackEntry ->
            val rating = backStackEntry.arguments?.getString("rating")
            HotelsByRating(navController, rating)
            }
        }
    }
