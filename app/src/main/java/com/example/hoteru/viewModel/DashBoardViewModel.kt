package com.example.hoteru.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.DashboardStats
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val TAG = "DashboardViewModel"

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _stats.value = MongoDBConnection.getDashboardStats()
                Log.d(TAG, "Estadísticas cargadas: ${_stats.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar estadísticas: ${e.message}", e)
                _stats.value = DashboardStats() // Resetear en caso de error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
