package com.example.hoteru.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ToastMessage(
    message: String?,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = SnackbarHostState()

    message?.let {
        LaunchedEffect(it) {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    ){ data ->
        Snackbar(
            snackbarData = data,
            modifier = Modifier.padding(16.dp)
        )
    }
}