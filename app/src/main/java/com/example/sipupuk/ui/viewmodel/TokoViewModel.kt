package com.example.sipupuk.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipupuk.data.model.Toko
import com.example.sipupuk.data.repository.TokoRepository
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class TokoViewModel(context: Context) : ViewModel() {

    private val repository = TokoRepository(context)

    // Get Toko State
    private val _tokoState = MutableStateFlow<Resource<Toko>?>(null)
    val tokoState: StateFlow<Resource<Toko>?> = _tokoState.asStateFlow()

    // Create Toko State
    private val _createTokoState = MutableStateFlow<Resource<Toko>?>(null)
    val createTokoState: StateFlow<Resource<Toko>?> = _createTokoState.asStateFlow()

    // Update Toko State
    private val _updateTokoState = MutableStateFlow<Resource<Toko>?>(null)
    val updateTokoState: StateFlow<Resource<Toko>?> = _updateTokoState.asStateFlow()

    // Has Toko State
    private val _hasToko = MutableStateFlow(false)
    val hasToko: StateFlow<Boolean> = _hasToko.asStateFlow()

    // Current Toko Data
    private val _currentToko = MutableStateFlow<Toko?>(null)
    val currentToko: StateFlow<Toko?> = _currentToko.asStateFlow()

    // Get My Toko
    fun getMyToko() {
        viewModelScope.launch {
            repository.getMyToko().collect { result ->
                _tokoState.value = result

                if (result is Resource.Success) {
                    _hasToko.value = true
                    _currentToko.value = result.data
                } else if (result is Resource.Error) {
                    _hasToko.value = false
                    _currentToko.value = null
                }
            }
        }
    }

    // Create Toko
    fun createToko(
        namaToko: String,
        alamat: String,
        desa: String,
        noTelepon: String,
        deskripsi: String?,
        jamBuka: String?,
        jamTutup: String?,
//        fotoUri: Uri?
    ) {
        viewModelScope.launch {
            repository.createToko(
                namaToko,
                alamat,
                desa,
                noTelepon,
                deskripsi,
                jamBuka,
                jamTutup,
//                fotoUri
            ).collect { result ->
                _createTokoState.value = result

                if (result is Resource.Success) {
                    _hasToko.value = true
                    _currentToko.value = result.data
                }
            }
        }
    }

    // Update Toko
    fun updateToko(
        namaToko: String,
        alamat: String,
        desa: String,
        noTelepon: String,
        deskripsi: String?,
        jamBuka: String?,
        jamTutup: String?,
//        fotoUri: Uri?
    ) {
        viewModelScope.launch {
            repository.updateToko(
                namaToko,
                alamat,
                desa,
                noTelepon,
                deskripsi,
                jamBuka,
                jamTutup,
//                fotoUri
            ).collect { result ->
                _updateTokoState.value = result

                if (result is Resource.Success) {
                    _currentToko.value = result.data
                }
            }
        }
    }

    // Reset States
    fun resetTokoState() {
        _tokoState.value = null
    }

    fun resetCreateTokoState() {
        _createTokoState.value = null
    }

    fun resetUpdateTokoState() {
        _updateTokoState.value = null
    }
}