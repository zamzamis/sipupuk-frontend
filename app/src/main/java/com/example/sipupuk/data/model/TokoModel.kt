package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

data class Toko(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("nama_toko")
    val namaToko: String,

    @SerializedName("alamat")
    val alamat: String,

    @SerializedName("desa")
    val desa: String,

    @SerializedName("no_telepon")
    val noTelepon: String,

    @SerializedName("deskripsi")
    val deskripsi: String?,

//    @SerializedName("foto_toko")
//    val fotoToko: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("jam_buka")
    val jamBuka: String?,

    @SerializedName("jam_tutup")
    val jamTutup: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class TokoRequest(
    @SerializedName("nama_toko")
    val namaToko: String,

    @SerializedName("alamat")
    val alamat: String,

    @SerializedName("desa")
    val desa: String,

    @SerializedName("no_telepon")
    val noTelepon: String,

    @SerializedName("deskripsi")
    val deskripsi: String? = null,

    @SerializedName("jam_buka")
    val jamBuka: String? = null,

    @SerializedName("jam_tutup")
    val jamTutup: String? = null
)

data class TokoData(
    @SerializedName("toko")
    val toko: Toko? = null
)