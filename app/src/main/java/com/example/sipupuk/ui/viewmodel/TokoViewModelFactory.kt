package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TokoViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TokoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TokoViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}