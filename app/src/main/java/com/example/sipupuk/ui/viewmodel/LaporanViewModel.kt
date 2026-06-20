package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sipupuk.data.model.LaporanBulanan
import com.example.sipupuk.data.remote.RetrofitClient
import com.example.sipupuk.data.repository.LaporanRepository
import com.example.sipupuk.data.repository.TokoRepository
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LaporanViewModel(context: Context) : ViewModel() {

    private val repository = LaporanRepository(context)

    private val _laporanState = MutableStateFlow<Resource<LaporanBulanan>>(Resource.Loading())
    val laporanState: StateFlow<Resource<LaporanBulanan>> = _laporanState

    fun getLaporanBulanan() {
        viewModelScope.launch {
            repository.getLaporanBulanan().collect { resource ->
                _laporanState.value = resource
            }
        }
    }
}

class LaporanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaporanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LaporanViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}