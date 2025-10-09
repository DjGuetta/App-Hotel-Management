package com.example.hoteru.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoteru.model.Comment
import com.example.hoteru.model.MongoDBConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import kotlin.collections.toList

class logicComments : ViewModel() {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun loadComments(hotelId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = MongoDBConnection.getComments(hotelId)
            _comments.value = results
        }
    }


    fun insertComment(comment: Comment) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = MongoDBConnection.insertCommentDb(comment)
            if (success) {
                // Reload comments for this hotel
                comment.hotelId?.let { loadComments(it) }
            }
        }
    }
}