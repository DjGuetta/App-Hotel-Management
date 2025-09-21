package com.example.app.viewmodel

// ------------------- IMPORTS -------------------
// Import for using a ViewModel, which stores and manages UI-related data
import android.location.Location
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
// Import for launching coroutines scoped to the ViewModel's lifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoteru.model.MongoDBConnection
import com.example.hoteru.model.map_data.tachiraLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
//import com.google.maps.android.compose.rememberCameraPositionState
// Import for coroutines with a specific dispatcher for background work
// Import for launching coroutines
import kotlinx.coroutines.launch
// Import for StateFlow (a cold asynchronous data stream for observing state changes)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
// Import for working with MongoDB documents
import org.bson.Document
import org.bson.types.ObjectId

/**
 * ViewModel that manages hotel data retrieved from MongoDB.
 *
 * English:
 * - This ViewModel loads hotel data asynchronously using coroutines.
 * - It uses StateFlow to expose data updates to the UI in a reactive way.
 *
 * Español:
 * - Este ViewModel carga datos de hoteles de forma asíncrona usando corrutinas.
 * - Utiliza StateFlow para exponer actualizaciones de datos a la interfaz de usuario de forma reactiva.
 */
class MapViewModel : ViewModel() {

    // ------------------- STATE FLOW EXPLANATION -------------------
    // English:
    // MutableStateFlow is a special data holder that can emit updates over time.
    // - It is *mutable* internally (can be changed from inside this ViewModel).
    // - It allows asynchronous and reactive UI updates when its value changes.
    //
    // Español:
    // MutableStateFlow es un contenedor especial de datos que puede emitir actualizaciones con el tiempo.
    // - Es *mutable* internamente (se puede cambiar desde dentro de este ViewModel).
    // - Permite actualizaciones asíncronas y reactivas de la interfaz cuando cambia su valor.
    //
    // Difference with a simple State:
    // English:
    // - `State` in Compose is tied to the UI and recomposes on change.
    // - `StateFlow` is part of Kotlin coroutines and can be collected anywhere, not just in Compose.
    // - StateFlow supports asynchronous and background updates more naturally.
    //
    // Español:
    // - `State` en Compose está vinculado directamente a la UI y recompone cuando cambia.
    // - `StateFlow` es parte de Kotlin coroutines y puede ser recogido en cualquier parte, no solo en Compose.
    // - StateFlow admite actualizaciones asíncronas y en segundo plano de forma más natural.
    private val _hotels = MutableStateFlow<List<Document>>(emptyList())

    // Publicly exposed read-only version of the hotels list.
    // English: Exposed as `StateFlow` so UI can observe but not modify it directly.
    // Español: Expuesto como `StateFlow` para que la UI pueda observar pero no modificarlo directamente.
    val hotels: StateFlow<List<Document>> = _hotels
    private val _onehotel = MutableStateFlow<Document?>(null)

    val onehotel: StateFlow<Document?> = _onehotel

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            tachiraLatLng,
            12f
        )
    )



    val filteredHotels: StateFlow<List<Document>> = searchText
        .combine(
            _hotels) { text, hotels ->
            if(text.isBlank()){
                hotels
            }else{
                hotels.filter { doc ->
                    doesMatchSearchQuery(doc, text)
                }
            }
        }.stateIn(
            viewModelScope, // tied to ViewModel lifecycle
            SharingStarted.WhileSubscribed(5000), // active only when collected
            emptyList() // default value before emission
        )



    init {
        loadHotels()
    }

    /**
     * Loads the list of hotels from the MongoDB "Hotel" collection.
     *
     * English:
     * - Uses `viewModelScope.launch(Dispatchers.IO)` to run this work on a background thread.
     * - `Dispatchers.IO` is optimized for database and network operations.
     * - After fetching the results, it updates `_hotels.value`, which notifies any UI collectors.
     *
     * Español:
     * - Usa `viewModelScope.launch(Dispatchers.IO)` para ejecutar este trabajo en un hilo de segundo plano.
     * - `Dispatchers.IO` está optimizado para operaciones de base de datos y red.
     * - Después de obtener los resultados, actualiza `_hotels.value`, lo que notifica a cualquier observador en la UI.
     */
    private fun loadHotels() {
        // English: Launch a coroutine in the ViewModel's scope on the IO dispatcher.
        // Español: Lanza una corrutina en el ámbito del ViewModel en el despachador IO.
        viewModelScope.launch(Dispatchers.IO) {

            // English: Get the MongoDB collection named "Hotel".
            // Español: Obtiene la colección de MongoDB llamada "Hotel".
            val collection = MongoDBConnection.getCollection("Hotel")

            // English: Fetch all documents and convert them to a list.
            // Español: Obtiene todos los documentos y los convierte en una lista.
            val results = collection.find().toList()

            // English: Update the StateFlow value so observers receive the new data.
            // Español: Actualiza el valor de StateFlow para que los observadores reciban los nuevos datos.
            _hotels.value = results
        }
    }

     fun loadDetailsHotel(collection: String, id: String?){
        viewModelScope.launch(Dispatchers.IO) {
            val oneDocument = MongoDBConnection.oneDocument(collection, id)

            println(oneDocument)
            _onehotel.value = oneDocument

        }

    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    private fun doesMatchSearchQuery(document: Document, searchText: String): Boolean {
        val hotelName = document.getString("name") ?: ""
        return hotelName.contains(searchText, ignoreCase = true)
    }

    fun moveCamerato(location: LatLng) {
        println(location)
        println(location)
        println(location)
        println(location)
        println(location)
        println(location)
        println(location)
        println(location)
        println("kdsdsd")

        viewModelScope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 15f),
                durationMs = 1000
            )
        }
    }



//    fun listOfHotels(){
//        viewModelScope.launch(Dispatchers.IO) {
//            val listofDocument = MongoDBConnection.getDocuments("Hotel")
//            println(listofDocument)
//            _listOfHotels.value = listofDocument
//
//        }
//    }


}

