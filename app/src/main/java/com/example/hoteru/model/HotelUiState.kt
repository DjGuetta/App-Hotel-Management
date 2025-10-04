package com.example.hoteru.model

import com.example.hoteru.model.Hotel

/**
 * Representa los diferentes estados de la UI para la gestión de hoteles
 */
sealed class HotelUiState {
    /**
     * Estado de carga - muestra un indicador de progreso
     */
    object Loading : HotelUiState()

    /**
     * Estado de éxito - contiene la lista de hoteles cargados
     * @property hotels Lista de hoteles cargados exitosamente
     */
    data class Success(val hotels: List<Hotel>) : HotelUiState()

    /**
     * Estado de error - contiene un mensaje de error
     * @property message Mensaje de error a mostrar
     */
    data class Error(val message: String) : HotelUiState()

    /**
     * Estado vacío - no hay hoteles para mostrar
     */
    object Empty : HotelUiState()
}