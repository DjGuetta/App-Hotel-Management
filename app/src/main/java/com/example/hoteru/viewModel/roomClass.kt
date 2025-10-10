package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bson.Document

class roomClass : ViewModel() {
    private val _onedocument = MutableStateFlow<Document?>(null)

    val onedocument: StateFlow<Document?> = _onedocument

    fun loadDetailsDocument(collection: String, id: String?){
        viewModelScope.launch(Dispatchers.IO) {
            val oneDocument = MongoDBConnection.oneDocument(collection, id)
            _onedocument.value = oneDocument

        }

    }
}