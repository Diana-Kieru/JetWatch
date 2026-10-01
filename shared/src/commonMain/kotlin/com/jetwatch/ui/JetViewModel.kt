package com.jetwatch.ui

import androidx.lifecycle.ViewModel

abstract class JetViewModel : ViewModel() {
    fun close() {
        onCleared()
    }
}
