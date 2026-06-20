package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

data class Produk(
    @SerializedName("id")
    val id: Int,

    @SerializedName("toko_id")
    val tokoId: Int,

    @SerializedName("kategori_produk_id") // ✅ TAMBAH INI
    val kategoriProdukId: Int,

    @SerializedName("satuan_id") // ✅ TAMBAH INI
    val satuanId: Int,

    @SerializedName("nama_produk")
    val namaProduk: String,

    @SerializedName("merk")
    val merk: String,

    @SerializedName("deskripsi")
    val deskripsi: String?,

    @SerializedName("berat")
    val berat: Double?,

    @SerializedName("harga")
    val harga: Double,

    @SerializedName("stok")
    val stok: Int,

    @SerializedName("foto_produk")
    val fotoProduk: String?,

    @SerializedName("foto_produk_url")
    val fotoProdukUrl: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("toko")
    val toko: Toko? = null,

    @SerializedName("kategori_produk") // ✅ TAMBAH INI
    val kategoriProduk: KategoriProduk? = null,

    @SerializedName("satuan") // ✅ TAMBAH INI
    val satuan: Satuan? = null
)

data class ProdukRequest(
    @SerializedName("kategori_produk_id") // ✅ TAMBAH INI
    val kategoriProdukId: Int,

    @SerializedName("satuan_id") // ✅ TAMBAH INI
    val satuanId: Int,

    @SerializedName("nama_produk")
    val namaProduk: String,

    @SerializedName("merk")
    val merk: String,

    @SerializedName("deskripsi")
    val deskripsi: String? = null,

    @SerializedName("berat")
    val berat: Double?,

    @SerializedName("harga")
    val harga: Double,

    @SerializedName("stok")
    val stok: Int
)

data class ProdukListData(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("data")
    val data: List<Produk>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("last_page")
    val lastPage: Int
)

// ========== USER DATA (untuk relasi) ==========
data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("role")
    val role: String
)

data class CartItem(
    val produk: Produk,
    var jumlah: Int
)

data class CheckoutRequest(
    @SerializedName("toko_id")
    val tokoId: Int,

    @SerializedName("items")
    val items: List<CheckoutItem>,

    @SerializedName("catatan_pembeli")
    val catatanPembeli: String?
)

data class CheckoutItem(
    @SerializedName("produk_id")
    val produkId: Int,

    @SerializedName("jumlah")
    val jumlah: Int
)