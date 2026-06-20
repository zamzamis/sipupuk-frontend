package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

data class LaporanBulanan(
    @SerializedName("periode")
    val periode: Periode?,

    @SerializedName("ringkasan")
    val ringkasan: RingkasanLaporan?,

    @SerializedName("transaksi")
    val transaksi: List<TransaksiLaporan>?
)

data class Periode(
    @SerializedName("bulan")
    val bulan: String?,

    @SerializedName("startDate")
    val startDate: String?,

    @SerializedName("endDate")
    val endDate: String?
)

data class RingkasanLaporan(
    @SerializedName("totalPendapatan")
    val totalPendapatan: Double?,

    @SerializedName("totalPesanan")
    val totalPesanan: Int?,

    @SerializedName("pesananSelesai")
    val pesananSelesai: Int?,

    @SerializedName("totalItemTerjual")
    val totalItemTerjual: Int?,

    @SerializedName("pesananDibatalkan")
    val pesananDibatalkan: Int?
)

data class TransaksiLaporan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kodePesanan")
    val kodePesanan: String?,

    @SerializedName("namaPembeli")
    val namaPembeli: String?,

    @SerializedName("tanggal")
    val tanggal: String?,

    @SerializedName("totalHarga")
    val totalHarga: Double?,

    @SerializedName("totalItem")
    val totalItem: Int?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("catatanPembeli")
    val catatanPembeli: String?
)