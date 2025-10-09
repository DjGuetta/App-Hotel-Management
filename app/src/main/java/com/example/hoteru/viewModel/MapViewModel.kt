package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.Hotel
import com.example.hoteru.model.MongoDBConnection
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class MapViewModel : ViewModel() {

    // --- ESTADOS PRINCIPALES CON MODELOS DE DATOS FUERTES ---
    // En lugar de List<Document>, usamos List<Hotel> para más seguridad y claridad.
    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels

    // Estado para un único hotel seleccionado
    private val _selectedHotel = MutableStateFlow<Hotel?>(null)
    val selectedHotel: StateFlow<Hotel?> = _selectedHotel

    // --- ESTADOS PARA LA BÚSQUEDA Y FILTROS ---
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // Lógica de filtrado que combina el texto de búsqueda con la lista de hoteles.
    val filteredHotels: StateFlow<List<Hotel>> = searchText
        .debounce(300) // Espera 300ms después de que el usuario deja de escribir
        .combine(_hotels) { text, hotels ->
            if (text.isBlank()) {
                hotels
            } else {
                _isSearching.value = true
                val filteredList = hotels.filter { it.name.contains(text, ignoreCase = true) }
                _isSearching.value = false
                filteredList
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ESTADOS PARA LA VISIBILIDAD DE LA UI ---
    private val _visibilityWindow = MutableStateFlow(false)
    val visibilityWindow: StateFlow<Boolean> = _visibilityWindow

    // Otros estados de visibilidad que tu amigo tenía
    private val _visibilityButton = MutableStateFlow(false)
    val visibilityButton: StateFlow<Boolean> = _visibilityButton

    private val _visibilitySearchEngine = MutableStateFlow(true)
    val visibilitySearchEngine: StateFlow<Boolean> = _visibilitySearchEngine

    private val _visibilityPriceFilter = MutableStateFlow(false)
    val visibilityPriceFilter: StateFlow<Boolean> = _visibilityPriceFilter

    private val _visibilityRatingFilter = MutableStateFlow(false)
    val visibilityRatingFilter: StateFlow<Boolean> = _visibilityRatingFilter

    // --- INICIALIZACIÓN ---
    init {
        loadHotels()
    }

    // --- FUNCIONES DE CARGA DE DATOS (REFACTORIZADAS) ---

    /**
     * Carga la lista de hoteles usando la función de Flow de MongoDBConnection.
     * Es más eficiente y reactivo.
     */
    private fun loadHotels() {
        viewModelScope.launch {
            // Usamos la función que YA existe en tu database.kt
            MongoDBConnection.getHotels()
                .catch { exception ->
                    // Manejo de errores si la carga falla
                    println("Error cargando hoteles: ${exception.message}")
                }
                .collect { hotelList ->
                    _hotels.value = hotelList
                }
        }
    }

    /**
     * Carga los detalles de un hotel específico.
     * Esta función reemplaza la genérica 'oneDocument'.
     */
    fun loadHotelDetails(hotelId: ObjectId) {
        // Esta lógica podría expandirse si necesitas cargar más detalles
        _selectedHotel.value = _hotels.value.find { it._id == hotelId }
    }

    // --- FUNCIONES PARA LA UI ---

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onFilterByPrice(min: Float, max: Float) {
        // Lógica de ejemplo para filtrar
        // Nota: Esto necesitaría una función específica en MongoDBConnection si quieres que sea eficiente.
        // Por ahora, filtramos la lista ya cargada.
    }

    fun onFilterByRating(rating: Double) {
        // Lógica de ejemplo para filtrar por rating
        // También necesitaría una función en el backend para ser óptimo.
    }

    fun moveCameraTo(cameraPositionState: CameraPositionState, location: LatLng) {
        viewModelScope.launch {
            val cameraPosition = CameraPosition.Builder()
                .target(location)
                .zoom(16f)
                .bearing(0f)
                .tilt(0f)
                .build()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(cameraPosition),
                durationMs = 1000
            )
        }
    }

    // --- FUNCIONES DE VISIBILIDAD  ---
    fun setVisibilityWindow(value: Boolean) {
        _visibilityWindow.value = value
    }

    fun setVisibilityButton(value: Boolean) {
        _visibilityButton.value = value
    }

    fun setVisibilitySearchEngine(value: Boolean) {
        _visibilitySearchEngine.value = value
    }

    fun setVisibilityPriceFilter(value: Boolean) {
        _visibilityPriceFilter.value = value
    }

    fun setVisibilityRatingFilter(value: Boolean) {
        _visibilityRatingFilter.value = value
    }
}
