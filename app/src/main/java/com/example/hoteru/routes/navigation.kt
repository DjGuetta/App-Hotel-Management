package com.example.hoteru.routes
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.hoteru.view.AdminDashboardScreen
import com.example.hoteru.view.LoginScreen
import com.example.hoteru.view.HotelManagementScreen
import com.example.hoteru.view.RoomManagementScreen
import com.example.hoteru.view.ReservationsScreen
import com.example.hoteru.view.CustomersScreen
import com.example.hoteru.viewModel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.view.GlobalReservationsScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.hoteru.view.BookingManagementScreen
import com.example.hoteru.view.MapScreen
import com.example.hoteru.view.RegisterScreen

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

        composable("room_management") {
            RoomManagementScreen(navController = navController)
        }
        composable("global_reservations") {
            GlobalReservationsScreen(navController = navController)
        }

        composable("customers") {
            CustomersScreen(navController = navController)
        }

        // Las rutas existentes del mapa (para cuando integres)
        composable("home") {
            // Llama a MapScreen y le pasa los parámetros necesarios.
            // authViewModel ya está definido al inicio de AppNavigation, así que lo reutilizamos.
            MapScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(
            route = "detailshotel/{hotelId}",
            arguments = listOf(navArgument("hotelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId")
            if (hotelId != null) {
                // Asegúrate de que DetailsHotelScreen exista y acepte estos parámetros
                // DetailsHotelScreen(navController = navController, hotelId = hotelId)

                // Si aún no la tienes, puedes poner un placeholder temporal
                Text("Pantalla de detalles para el hotel con ID: $hotelId")
            }
        }
    }
}
