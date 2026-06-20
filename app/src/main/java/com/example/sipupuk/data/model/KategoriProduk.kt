package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

data class KategoriProduk(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama_kategori")
    val namaKategori: String,

    @SerializedName("icon")
    val icon: String?,

    @SerializedName("deskripsi")
    val deskripsi: String?,

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("satuans") // ✅ Untuk response getSatuanByKategori
    val satuans: List<Satuan>? = null
)

data class KategoriProdukListData(
    @SerializedName("data")
    val data: List<KategoriProduk>
)