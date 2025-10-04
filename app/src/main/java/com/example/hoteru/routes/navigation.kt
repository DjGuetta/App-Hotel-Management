
package com.example.hoteru.routes

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hoteru.view.MapScreen
import com.example.hoteru.view.DetailsHotelScreen
import com.example.hoteru.view.DetailsRoomScreen
import com.example.hoteru.view.HotelsByRating
import com.example.hoteru.view.RoomsByPrice


@Composable
fun Routes(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home"
    ){
        composable("home") { MapScreen(navController) }
        composable("detailshotel/{_id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("_id")
            DetailsHotelScreen(navController, id)
        }
        composable("detailsroom/{_id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("_id")
            DetailsRoomScreen(navController, id)
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
