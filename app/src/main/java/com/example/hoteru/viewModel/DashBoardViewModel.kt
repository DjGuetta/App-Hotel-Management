package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.DashboardStats
import com.example.hoteru.model.Hotel
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    // --- StateFlow para la lista de hoteles ---
    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()
    // ----------------------------------------------

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "DashboardViewModel"

    init {
        loadDashboardData(null)
    }

    // Función que refleja la carga más que solo estadísticas
    fun loadDashboardData(adminId: String?) {
        // Si no hay adminId, no podemos cargar los datos específicos.
        if (adminId.isNullOrBlank()) {
            Log.w(TAG, "Admin ID es nulo o vacío, no se pueden cargar los datos.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Obtenemos estadísticas y hoteles del admin actual
                val statsResult = MongoDBConnection.getDashboardStats() // Esto podría necesitar también el adminId
                val hotelsResult = MongoDBConnection.getHotelsByAdminId(adminId) // <-- Usamos la nueva función

                _stats.value = statsResult
                _hotels.value = hotelsResult // Guardamos la lista de hoteles

                Log.d(TAG, "Estadísticas cargadas: ${_stats.value}")
                Log.d(TAG, "Hoteles cargados para el admin $adminId: ${_hotels.value.size} hoteles")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar datos del dashboard: ${e.message}", e)
                _stats.value = DashboardStats()
                _hotels.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
