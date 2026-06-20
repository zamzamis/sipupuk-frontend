package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PembeliViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PembeliViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PembeliViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}