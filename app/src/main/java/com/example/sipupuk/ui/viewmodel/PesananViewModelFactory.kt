package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PesananViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PesananViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PesananViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}