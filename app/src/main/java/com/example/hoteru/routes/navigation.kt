package com.example.hoteru.routes
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import MapScreen
import com.example.hoteru.view.DetailsHotelScreen
import org.bson.types.ObjectId


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

    }
}
