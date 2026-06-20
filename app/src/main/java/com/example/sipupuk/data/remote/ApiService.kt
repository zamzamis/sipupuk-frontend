package com.example.sipupuk.data.remote

import com.example.sipupuk.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== AUTH ==========
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthData>>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

    @GET("user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserKata>>

    @POST("forgot-password/check-phone")
    suspend fun checkPhone(
        @Body request: CheckPhoneRequest
    ): Response<ApiResponse<Unit>>

    @POST("forgot-password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Unit>>


    // ========== TOKO - PENJUAL ==========

    @GET("penjual/laporan/bulanan")
    suspend fun getLaporanBulanan(
        @Header("Authorization") token: String
    ): Response<ApiResponse<LaporanBulanan>>

    @GET("penjual/toko")
    suspend fun getMyToko(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Toko>>

    @Multipart
    @POST("penjual/toko")
    suspend fun createToko(
        @Header("Authorization") token: String,
        @Part("nama_toko") namaToko: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part("desa") desa: RequestBody,
        @Part("no_telepon") noTelepon: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("jam_buka") jamBuka: RequestBody?,
        @Part("jam_tutup") jamTutup: RequestBody?,
//        @Part fotoToko: MultipartBody.Part?
    ): Response<ApiResponse<Toko>>

    @Multipart
    @PUT("penjual/toko")
    suspend fun updateToko(
        @Header("Authorization") token: String,
        @Part("nama_toko") namaToko: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part("desa") desa: RequestBody,
        @Part("no_telepon") noTelepon: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("jam_buka") jamBuka: RequestBody?,
        @Part("jam_tutup") jamTutup: RequestBody?,
//        @Part fotoToko: MultipartBody.Part?
    ): Response<ApiResponse<Toko>>

    @POST("penjual/toko")
    @Headers("Accept: application/json")
    suspend fun updateTokoRaw(
        @Header("Authorization") token: String,
        @Body body: RequestBody
    ): Response<ApiResponse<Toko>>


    // ========== KATEGORI & SATUAN (Public - untuk dropdown) ==========

    @GET("kategori-produk")
    suspend fun getKategoriProduk(): Response<ApiResponse<List<KategoriProduk>>>

    @GET("kategori/{kategoriId}/satuan")
    suspend fun getSatuanByKategori(
        @Path("kategoriId") kategoriId: Int
    ): Response<ApiResponse<List<Satuan>>>


    // ========== PRODUK - PENJUAL ==========

    @GET("penjual/produk")
    suspend fun getMyProduk(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 50,
        @Query("kategori_id") kategoriId: Int? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<ProdukListData>>

    @Multipart
    @POST("penjual/produk")
    suspend fun createProduk(
        @Header("Authorization") token: String,
        @Part("kategori_produk_id") kategoriProdukId: RequestBody,
        @Part("satuan_id") satuanId: RequestBody,
        @Part("nama_produk") namaProduk: RequestBody,
        @Part("merk") merk: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("berat") berat: RequestBody?,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part fotoProduk: MultipartBody.Part?
    ): Response<ApiResponse<Produk>>

    @Multipart
    @POST("penjual/produk/{id}")
    suspend fun updateProduk(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("_method") method: RequestBody, // PUT
        @Part("kategori_produk_id") kategoriProdukId: RequestBody,
        @Part("satuan_id") satuanId: RequestBody,
        @Part("nama_produk") namaProduk: RequestBody,
        @Part("merk") merk: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("berat") berat: RequestBody?,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("status") status: RequestBody?,
        @Part fotoProduk: MultipartBody.Part?
    ): Response<ApiResponse<Produk>>

    @POST("penjual/produk/{id}")
    @Headers("Accept: application/json")
    suspend fun updateProdukRaw(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: RequestBody
    ): Response<ApiResponse<Produk>>

    @DELETE("penjual/produk/{id}")
    suspend fun deleteProduk(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Unit>>


    // ========== PESANAN - PENJUAL ==========

    @GET("penjual/pesanan")
    suspend fun getTokoPesanan(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 50,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PesananListData>>

    @GET("penjual/pesanan/statistics")
    suspend fun getPesananStatistics(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PesananStatistics>>

    @GET("penjual/pesanan/{id}")
    suspend fun getPesananDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Pesanan>>

    @POST("penjual/pesanan/{id}/konfirmasi")
    suspend fun konfirmasiPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: KonfirmasiPesananRequest
    ): Response<ApiResponse<Pesanan>>

    @POST("penjual/pesanan/{id}/siap-diambil")
    suspend fun siapDiambilPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Pesanan>>

    @POST("penjual/pesanan/{id}/selesai")
    suspend fun selesaiPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Pesanan>>

    @POST("penjual/pesanan/{id}/tolak")
    suspend fun tolakPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: TolakPesananRequest
    ): Response<ApiResponse<Pesanan>>


    // ========== PEMBELI - PRODUK ==========

    @GET("pembeli/produk")
    suspend fun getProduk(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 20,
        @Query("kategori_id") kategoriId: Int? = null,
        @Query("search") search: String? = null,
        @Query("toko_id") tokoId: Int? = null,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<ProdukListData>>

    @GET("pembeli/produk/{id}")
    suspend fun getProdukDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Produk>>


    // ========== PEMBELI - PESANAN ==========

    @POST("pembeli/pesanan")
    suspend fun createPesanan(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequest
    ): Response<ApiResponse<Pesanan>>

    @GET("pembeli/pesanan")
    suspend fun getMyPesanan(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 50,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PesananListData>>

    @GET("pembeli/pesanan/statistics")
    suspend fun getMyPesananStatistics(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PesananStatistics>>

    @GET("pembeli/pesanan/{id}")
    suspend fun getMyPesananDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Pesanan>>

    @POST("pembeli/pesanan/{id}/cancel")
    suspend fun cancelPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Pesanan>>
}