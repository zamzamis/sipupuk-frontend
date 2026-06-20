package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

data class Satuan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nama_satuan")
    val namaSatuan: String,

    @SerializedName("singkatan")
    val singkatan: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class SatuanListData(
    @SerializedName("data")
    val data: List<Satuan>
)