package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipupuk.data.model.*
import com.example.sipupuk.data.repository.PembeliRepository
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PembeliViewModel(context: Context) : ViewModel() {

    private val repository = PembeliRepository(context)

    // ==================== PESANAN STATE ====================

    private val _createPesananState = MutableStateFlow<Resource<Pesanan>?>(null)
    val createPesananState: StateFlow<Resource<Pesanan>?> = _createPesananState.asStateFlow()

    private val _myPesananState = MutableStateFlow<Resource<PesananListData>?>(null)
    val myPesananState: StateFlow<Resource<PesananListData>?> = _myPesananState.asStateFlow()

    private val _myPesananList = MutableStateFlow<List<Pesanan>>(emptyList())
    val myPesananList: StateFlow<List<Pesanan>> = _myPesananList.asStateFlow()

    private val _cancelPesananState = MutableStateFlow<Resource<Pesanan>?>(null)
    val cancelPesananState: StateFlow<Resource<Pesanan>?> = _cancelPesananState.asStateFlow()

    // ==================== PESANAN FUNCTIONS ====================

    /**
     * Create pesanan (checkout)
     */
    fun createPesanan(
        tokoId: Int,
        items: List<CheckoutItem>,
        catatanPembeli: String?
    ) {
        viewModelScope.launch {
            repository.createPesanan(tokoId, items, catatanPembeli).collect { result ->
                _createPesananState.value = result
            }
        }
    }

    /**
     * Get pesanan pembeli
     */
    fun getMyPesanan(
        perPage: Int = 50,
        status: String? = null
    ) {
        viewModelScope.launch {
            repository.getMyPesanan(perPage, status).collect { result ->
                _myPesananState.value = result

                if (result is Resource.Success) {
                    _myPesananList.value = result.data?.data ?: emptyList()
                }
            }
        }
    }

    /**
     * Cancel pesanan
     */
    fun cancelPesanan(id: Int) {
        viewModelScope.launch {
            repository.cancelPesanan(id).collect { result ->
                _cancelPesananState.value = result

                if (result is Resource.Success) {
                    getMyPesanan() // Refresh list
                }
            }
        }
    }

    // ==================== RESET STATES ====================

    fun resetCreatePesananState() {
        _createPesananState.value = null
    }

    fun resetCancelPesananState() {
        _cancelPesananState.value = null
    }
}