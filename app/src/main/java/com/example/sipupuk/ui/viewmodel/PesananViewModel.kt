package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipupuk.data.model.Pesanan
import com.example.sipupuk.data.model.PesananListData
import com.example.sipupuk.data.model.PesananStatistics
import com.example.sipupuk.data.repository.PesananRepository
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PesananViewModel(context: Context) : ViewModel() {

    private val repository = PesananRepository(context)

    // Pesanan List State
    private val _pesananListState = MutableStateFlow<Resource<PesananListData>?>(null)
    val pesananListState: StateFlow<Resource<PesananListData>?> = _pesananListState.asStateFlow()

    // Statistics State
    private val _statisticsState = MutableStateFlow<Resource<PesananStatistics>?>(null)
    val statisticsState: StateFlow<Resource<PesananStatistics>?> = _statisticsState.asStateFlow()

    // Pesanan Detail State
    private val _pesananDetailState = MutableStateFlow<Resource<Pesanan>?>(null)
    val pesananDetailState: StateFlow<Resource<Pesanan>?> = _pesananDetailState.asStateFlow()

    // Action State (konfirmasi, tolak, dll)
    private val _actionState = MutableStateFlow<Resource<Pesanan>?>(null)
    val actionState: StateFlow<Resource<Pesanan>?> = _actionState.asStateFlow()

    // Pesanan List
    private val _pesananList = MutableStateFlow<List<Pesanan>>(emptyList())
    val pesananList: StateFlow<List<Pesanan>> = _pesananList.asStateFlow()

    // Statistics Data
    private val _statistics = MutableStateFlow<PesananStatistics?>(null)
    val statistics: StateFlow<PesananStatistics?> = _statistics.asStateFlow()

    // Get Pesanan Toko
    fun getTokoPesanan(
        perPage: Int = 50,
        status: String? = null
    ) {
        viewModelScope.launch {
            repository.getTokoPesanan(perPage, status).collect { result ->
                _pesananListState.value = result

                if (result is Resource.Success) {
                    _pesananList.value = result.data?.data ?: emptyList()
                }
            }
        }
    }

    // Get Statistics
    fun getStatistics() {
        viewModelScope.launch {
            repository.getPesananStatistics().collect { result ->
                _statisticsState.value = result

                if (result is Resource.Success) {
                    _statistics.value = result.data
                }
            }
        }
    }

    // Get Pesanan Detail
    fun getPesananDetail(id: Int) {
        viewModelScope.launch {
            repository.getPesananDetail(id).collect { result ->
                _pesananDetailState.value = result
            }
        }
    }

    // Konfirmasi Pesanan
    fun konfirmasiPesanan(id: Int, catatanToko: String?) {
        viewModelScope.launch {
            repository.konfirmasiPesanan(id, catatanToko).collect { result ->
                _actionState.value = result

                // Refresh list if success
                if (result is Resource.Success) {
                    getTokoPesanan()
                    getStatistics()
                }
            }
        }
    }

    // Siap Diambil
    fun siapDiambilPesanan(id: Int) {
        viewModelScope.launch {
            repository.siapDiambilPesanan(id).collect { result ->
                _actionState.value = result

                if (result is Resource.Success) {
                    getTokoPesanan()
                    getStatistics()
                }
            }
        }
    }

    // Selesai
    fun selesaiPesanan(id: Int) {
        viewModelScope.launch {
            repository.selesaiPesanan(id).collect { result ->
                _actionState.value = result

                if (result is Resource.Success) {
                    getTokoPesanan()
                    getStatistics()
                }
            }
        }
    }

    // Tolak Pesanan
    fun tolakPesanan(id: Int, catatanToko: String) {
        viewModelScope.launch {
            repository.tolakPesanan(id, catatanToko).collect { result ->
                _actionState.value = result

                if (result is Resource.Success) {
                    getTokoPesanan()
                    getStatistics()
                }
            }
        }
    }

    // Reset States
    fun resetPesananListState() {
        _pesananListState.value = null
    }

    fun resetActionState() {
        _actionState.value = null
    }

    fun resetPesananDetailState() {
        _pesananDetailState.value = null
    }
}