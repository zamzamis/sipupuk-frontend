package com.example.sipupuk.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipupuk.data.model.*
import com.example.sipupuk.data.repository.ProdukRepository
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProdukViewModel(context: Context) : ViewModel() {

    private val repository = ProdukRepository(context)

    // ========== KATEGORI & SATUAN STATE ==========

    private val _kategoriListState = MutableStateFlow<Resource<List<KategoriProduk>>?>(null)
    val kategoriListState: StateFlow<Resource<List<KategoriProduk>>?> = _kategoriListState.asStateFlow()

    private val _satuanListState = MutableStateFlow<Resource<List<Satuan>>?>(null)
    val satuanListState: StateFlow<Resource<List<Satuan>>?> = _satuanListState.asStateFlow()

    // ========== PRODUK STATE ==========

    // Get Produk List State
    private val _produkListState = MutableStateFlow<Resource<ProdukListData>?>(null)
    val produkListState: StateFlow<Resource<ProdukListData>?> = _produkListState.asStateFlow()

    // Create Produk State
    private val _createProdukState = MutableStateFlow<Resource<Produk>?>(null)
    val createProdukState: StateFlow<Resource<Produk>?> = _createProdukState.asStateFlow()

    // Update Produk State
    private val _updateProdukState = MutableStateFlow<Resource<Produk>?>(null)
    val updateProdukState: StateFlow<Resource<Produk>?> = _updateProdukState.asStateFlow()

    // Delete Produk State
    private val _deleteProdukState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteProdukState: StateFlow<Resource<Unit>?> = _deleteProdukState.asStateFlow()

    // Produk List (for local use)
    private val _produkList = MutableStateFlow<List<Produk>>(emptyList())
    val produkList: StateFlow<List<Produk>> = _produkList.asStateFlow()

    // ========== KATEGORI & SATUAN METHODS ==========

    /**
     * Get list kategori produk
     */
    fun getKategoriProduk() {
        viewModelScope.launch {
            repository.getKategoriProduk().collect { result ->
                _kategoriListState.value = result
            }
        }
    }

    /**
     * Get satuan berdasarkan kategori
     */
    fun getSatuanByKategori(kategoriId: Int) {
        viewModelScope.launch {
            repository.getSatuanByKategori(kategoriId).collect { result ->
                _satuanListState.value = result
            }
        }
    }

    // ========== PRODUK PENJUAL METHODS ==========

    /**
     * Get produk milik penjual
     */
    fun getMyProduk(
        perPage: Int = 50,
        kategoriId: Int? = null,
        status: String? = null,
        search: String? = null,
        page: Int = 1
    ) {
        viewModelScope.launch {
            repository.getMyProduk(
                perPage = perPage,
                kategoriId = kategoriId,
                status = status,
                search = search,
                page = page
            ).collect { result ->
                _produkListState.value = result

                if (result is Resource.Success) {
                    _produkList.value = result.data?.data ?: emptyList()
                }
            }
        }
    }

    /**
     * Create produk baru
     */
    fun createProduk(
        kategoriProdukId: Int,
        satuanId: Int,
        namaProduk: String,
        merk: String,
        deskripsi: String?,
        berat: Double?,
        harga: Double,
        stok: Int,
        fotoUri: Uri?
    ) {
        viewModelScope.launch {
            repository.createProduk(
                kategoriProdukId = kategoriProdukId,
                satuanId = satuanId,
                namaProduk = namaProduk,
                merk = merk,
                deskripsi = deskripsi,
                berat = berat,
                harga = harga,
                stok = stok,
                fotoUri = fotoUri
            ).collect { result ->
                _createProdukState.value = result

                // Refresh list if success
                if (result is Resource.Success) {
                    getMyProduk()
                }
            }
        }
    }

    /**
     * Update produk
     */
    fun updateProduk(
        id: Int,
        kategoriProdukId: Int,
        satuanId: Int,
        namaProduk: String,
        merk: String,
        deskripsi: String?,
        berat: Double?,
        harga: Double,
        stok: Int,
        status: String?,
        fotoUri: Uri?
    ) {
        viewModelScope.launch {
            repository.updateProduk(
                id = id,
                kategoriProdukId = kategoriProdukId,
                satuanId = satuanId,
                namaProduk = namaProduk,
                merk = merk,
                deskripsi = deskripsi,
                berat = berat,
                harga = harga,
                stok = stok,
                status = status,
                fotoUri = fotoUri
            ).collect { result ->
                _updateProdukState.value = result

                // Refresh list if success
                if (result is Resource.Success) {
                    getMyProduk()
                }
            }
        }
    }

    /**
     * Delete produk
     */
    fun deleteProduk(id: Int) {
        viewModelScope.launch {
            repository.deleteProduk(id).collect { result ->
                _deleteProdukState.value = result

                // Refresh list if success
                if (result is Resource.Success) {
                    getMyProduk()
                }
            }
        }
    }

    // ========== PRODUK PEMBELI METHODS ==========

    /**
     * Get produk untuk pembeli (dengan filter kategori)
     */
    fun getProduk(
        perPage: Int = 20,
        kategoriId: Int? = null,
        search: String? = null,
        tokoId: Int? = null,
        page: Int = 1
    ) {
        viewModelScope.launch {
            repository.getProduk(
                perPage = perPage,
                kategoriId = kategoriId,
                search = search,
                tokoId = tokoId,
                page = page
            ).collect { result ->
                _produkListState.value = result

                if (result is Resource.Success) {
                    _produkList.value = result.data?.data ?: emptyList()
                }
            }
        }
    }

    /**
     * Get detail produk
     */
    fun getProdukDetail(id: Int) {
        viewModelScope.launch {
            repository.getProdukDetail(id).collect { result ->
                // You can create separate state for detail if needed
                // For now, just log or handle accordingly
            }
        }
    }

    // ========== RESET STATES ==========

    fun resetProdukListState() {
        _produkListState.value = null
    }

    fun resetCreateProdukState() {
        _createProdukState.value = null
    }

    fun resetUpdateProdukState() {
        _updateProdukState.value = null
    }

    fun resetDeleteProdukState() {
        _deleteProdukState.value = null
    }

    fun resetKategoriListState() {
        _kategoriListState.value = null
    }

    fun resetSatuanListState() {
        _satuanListState.value = null
    }
}