package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.hoteru.model.Booking
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class LogicBooking : ViewModel() {

    private val _bookingResult = MutableStateFlow<Boolean?>(null)
    val bookingResult: StateFlow<Boolean?> = _bookingResult

    fun makeBooking(booking: Booking) {
        viewModelScope.launch(Dispatchers.IO) {
            MongoDBConnection.insertBooking(booking)
        }
    }

    fun resetResult() {
        _bookingResult.value = null
    }

}