package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

data class Pesanan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kode_pesanan")
    val kodePesanan: String,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("toko_id")
    val tokoId: Int,

    @SerializedName("total_harga")
    val totalHarga: Double,

    @SerializedName("total_item")
    val totalItem: Int,

    @SerializedName("status")
    val status: String, // pending, dikonfirmasi, siap_diambil, selesai, dibatalkan

    @SerializedName("catatan_pembeli")
    val catatanPembeli: String?,

    @SerializedName("catatan_toko")
    val catatanToko: String?,

    @SerializedName("tanggal_konfirmasi")
    val tanggalKonfirmasi: String?,

    @SerializedName("tanggal_siap")
    val tanggalSiap: String?,

    @SerializedName("tanggal_selesai")
    val tanggalSelesai: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("toko")
    val toko: Toko? = null,

    @SerializedName("detail_pesanans")
    val detailPesanans: List<DetailPesanan>? = null
)

data class DetailPesanan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("pesanan_id")
    val pesananId: Int,

    @SerializedName("produk_id")
    val produkId: Int,

    @SerializedName("nama_produk")
    val namaProduk: String,

    @SerializedName("merk")
    val merk: String,

    @SerializedName("kategori_produk") // ✅ TAMBAH INI
    val kategoriProduk: String?,

    @SerializedName("harga")
    val harga: Double,

    @SerializedName("jumlah")
    val jumlah: Int,

    @SerializedName("satuan") // ✅ TAMBAH INI
    val satuan: String,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("produk")
    val produk: Produk? = null
)

data class PesananListData(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("data")
    val data: List<Pesanan>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("last_page")
    val lastPage: Int
)

data class PesananStatistics(
    @SerializedName("total")
    val total: Int,

    @SerializedName("pending")
    val pending: Int,

    @SerializedName("dikonfirmasi")
    val dikonfirmasi: Int,

    @SerializedName("siap_diambil")
    val siapDiambil: Int,

    @SerializedName("selesai")
    val selesai: Int,

    @SerializedName("dibatalkan")
    val dibatalkan: Int
)

data class KonfirmasiPesananRequest(
    @SerializedName("catatan_toko")
    val catatanToko: String?
)

data class TolakPesananRequest(
    @SerializedName("catatan_toko")
    val catatanToko: String
)