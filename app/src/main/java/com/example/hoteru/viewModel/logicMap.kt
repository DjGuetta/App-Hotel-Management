package com.example.app.viewmodel

// ------------------- IMPORTS -------------------
// Import for using a ViewModel, which stores and manages UI-related data
import androidx.lifecycle.ViewModel
// Import for launching coroutines scoped to the ViewModel's lifecycle
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
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
import kotlin.let

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

    private val _rooms = MutableStateFlow<List<Document>>(emptyList())

    // Publicly exposed read-only version of the hotels list.
    // English: Exposed as `StateFlow` so UI can observe but not modify it directly.
    // Español: Expuesto como `StateFlow` para que la UI pueda observar pero no modificarlo directamente.
    val rooms: StateFlow<List<Document>> = _rooms
    private val _onehotel = MutableStateFlow<Document?>(null)

    val onehotel: StateFlow<Document?> = _onehotel

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)

    // Public immutable state that the Composable observes
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation
    private val _visibilityWindow = MutableStateFlow(false)

    // Public immutable state that the Composable observes
    val visibilityWindow: StateFlow<Boolean?> = _visibilityWindow

    private val _visibilityButton = MutableStateFlow(false)

    val visibilityButton: StateFlow<Boolean?> = _visibilityButton

    private val _visibilitySearchEngine = MutableStateFlow(true)
    val visibilitySearchEngine: StateFlow<Boolean?> = _visibilitySearchEngine


    private val _visibilityPriceFilter = MutableStateFlow(false)
    val visibilityPriceFilter: StateFlow<Boolean?> = _visibilityPriceFilter

    private val _visibilityRatingFilter = MutableStateFlow(false)
    val visibilityRatingFilter: StateFlow<Boolean?> = _visibilityRatingFilter


    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()
    private val _listOfHotels = MutableStateFlow<List<Document>>(emptyList())

    val filteredHotels: StateFlow<List<Document>> = searchText
        .combine(_listOfHotels) { text, hotels ->
            if (text.isBlank()) {
                hotels
            } else {
                hotels.filter { doc -> doesMatchSearchQuery(doc, text) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val _listOfRoomsUnderTheirPrice = MutableStateFlow<List<Document>>(emptyList())
    val listOfRoomsUnderTheirPrice: StateFlow<List<Document>> = _listOfRoomsUnderTheirPrice

    val _listOfHotelsByRating = MutableStateFlow<List<Document>>(emptyList())
    val listOfHotelsByRating: StateFlow<List<Document>> = _listOfHotelsByRating


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
            val collection = MongoDBConnection.getCollection("Hoteles")

            // English: Fetch all documents and convert them to a list.
            // Español: Obtiene todos los documentos y los convierte en una lista.
            val results = collection.find().toList()

            // English: Update the StateFlow value so observers receive the new data.
            // Español: Actualiza el valor de StateFlow para que los observadores reciban los nuevos datos.
            _hotels.value = results
        }
    }

     fun loadDetailsDocument(collection: String, id: String?){
        viewModelScope.launch(Dispatchers.IO) {
            val oneDocument = MongoDBConnection.oneDocument(collection, id)
            println(oneDocument)
            _onehotel.value = oneDocument

        }

    }
    fun loadRooms(idHotel:String?){
        viewModelScope.launch(Dispatchers.IO) {
            val rooms = MongoDBConnection.getRooms(idHotel)
            println(rooms)
            _rooms.value = rooms
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        if (text.isNotBlank()) {
            // Only fetch hotels when user types something
            listOfHotelsSearchEngine()
        }
    }
    fun updateSelectedLocation(location: LatLng?) {
        _selectedLocation.value = location
    }
    fun visibilityWindow(value: Boolean) {
        _visibilityWindow.value = value
    }

    fun visibilityButton(value: Boolean) {
        _visibilityButton.value = value
    }

    fun visibilitySearchEngine(value: Boolean) {
        _visibilitySearchEngine.value = value
    }

    fun visibilityPriceFilter(value: Boolean) {
        _visibilityPriceFilter.value = value
    }

    fun visibilityRatingFilter(value: Boolean) {
        _visibilityRatingFilter.value = value
    }

    private val _hotelsById = MutableStateFlow<Map<String, Document>>(emptyMap())
    val hotelsById: StateFlow<Map<String, Document>> = _hotelsById

    fun loadHotelsForRooms(rooms: List<Document>) {
        viewModelScope.launch(Dispatchers.IO) {
            val map = mutableMapOf<String, Document>()
            rooms.forEach { r ->
                val hotelId = r.get("hotel_id")?.toString() ?: return@forEach
                if (!map.containsKey(hotelId)) {
                    // Call loadDetailsDocument to update _onehotel
                    loadDetailsDocument("Hotels", hotelId)
                    // Get the document from _onehotel
                    val hotelDoc = _onehotel.value
                    hotelDoc?.let { map[hotelId] = it }
                }
            }
            _hotelsById.value = map
        }
    }

    private fun doesMatchSearchQuery(document: Document, searchText: String): Boolean {
        val hotelName = document.getString("name") ?: ""
        return hotelName.contains(searchText, ignoreCase = true)
    }

    fun moveCamerato(cameraPositionState: CameraPositionState, location: LatLng) {


        viewModelScope.launch {
            val cameraPosition = CameraPosition.Builder()
                .target(location) // exact LatLng
                .zoom(35f)        // desired zoom
                .bearing(0f)      // keep map north-up
                .tilt(0f)         // flat tilt
                .build()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(cameraPosition),
                durationMs = 1000
            )
            val final = cameraPositionState.position.target
            println("Camera ended at = ${final.latitude}, ${final.longitude}")

        }
    }
    fun listOfHotelsSearchEngine(){
        viewModelScope.launch(Dispatchers.IO) {
            val collection = MongoDBConnection.getCollection("Hoteles")

            // English: Fetch all documents and convert them to a list.
            // Español: Obtiene todos los documentos y los convierte en una lista.
            val results = collection.find().toList()
            _listOfHotels.value = results

        }
    }
    fun listOfRoomsUnderTheirPrice(minimun: Double, maximun: Double){
        viewModelScope.launch(Dispatchers.IO) {
            val collection = MongoDBConnection.getRoomsUnderTheirPrices(minimun, maximun).toList()
            _listOfRoomsUnderTheirPrice.value = collection

        }

    }
    fun listOfHotelsByRating(rating: Double){
        viewModelScope.launch(Dispatchers.IO) {
            val collection = MongoDBConnection.getHotelsUnderRating(rating).toList()
            _listOfHotelsByRating.value = collection

        }

    }




}

