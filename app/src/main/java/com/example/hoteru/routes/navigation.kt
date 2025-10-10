package com.example.hoteru.routes
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hoteru.view.DetailsRoomScreen
import com.example.hoteru.view.HotelsByRating
import com.example.hoteru.view.RoomsByPrice
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.hoteru.view.AdminDashboardScreen
import com.example.hoteru.view.LoginScreen
import com.example.hoteru.view.HotelManagementScreen
import com.example.hoteru.view.RoomManagementScreen
import com.example.hoteru.view.CustomersScreen
import com.example.hoteru.viewModel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.view.GlobalReservationsScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.hoteru.view.BookingManagementScreen
import com.example.hoteru.view.DetailsHotelScreen

import com.example.hoteru.view.MapScreen
import com.example.hoteru.view.RegisterScreen
import com.example.hoteru.view.UserBookingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController, authViewModel = authViewModel
            )
        }
        // --- RUTA DE REGISTRO ---
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(
            route = "admin_dashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Obtenemos el usuario completo desde el ViewModel
            val authState by authViewModel.authState.collectAsState()
            val loggedInUser = authState.loginSuccess

            AdminDashboardScreen(
                navController = navController,
                user = loggedInUser // Pasamos el objeto User completo
            )
        }

        // Nuevas rutas para el admin
        composable("hotel_management") {
            // Obtenemos el estado de autenticación
            val authState by authViewModel.authState.collectAsState()
            val loggedInUser = authState.loginSuccess

            // Pasamos el usuario a la pantalla de gestión
            HotelManagementScreen(navController = navController, user = loggedInUser)
        }
        // -- RUTA CON ARGUMENTOS --
        composable(
            route = "booking_management/{hotelId}", // Define la ruta con un placeholder
            arguments = listOf(navArgument("hotelId") {
                type = NavType.StringType
            }) // Define el tipo del argumento
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
        composable("user_bookings/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserBookingsScreen(userId = userId)
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
            // Llama a MapScreen y le pasa los parámetros necesarios.
            // authViewModel ya está definido al inicio de AppNavigation, así que lo reutilizamos.
            MapScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("detailshotel/{_id}/{name}/{idname}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("_id")
            val name = backStackEntry.arguments?.getString("name")
            val user = backStackEntry.arguments?.getString("idname")


            DetailsHotelScreen(navController, id, name, user)
        }
        composable("detailsroom/{_idroom}/{_idhotel}/{idname}") { backStackEntry ->
                val idroom  = backStackEntry.arguments?.getString("_idroom")
                val idhotel = backStackEntry.arguments?.getString("_idhotel")
                val user = backStackEntry.arguments?.getString("idname")

                DetailsRoomScreen(navController, idroom, idhotel, user)
        }
        composable("roomsbyprices/{minimun}/{maximun}/{userid}") { backStackEntry ->
            val minimun = backStackEntry.arguments?.getString("minimun")
            val maximun = backStackEntry.arguments?.getString("maximun")
            val user = backStackEntry.arguments?.getString("userid")

            RoomsByPrice(navController, minimun, maximun, user)
        }
        composable("hotelsbyrating/{rating}") { backStackEntry ->
            val rating = backStackEntry.arguments?.getString("rating")
            HotelsByRating(navController, rating)
            }
        }

    }
