package com.example.sipupuk.data.repository

import android.content.Context
import android.net.Uri
import com.example.sipupuk.data.local.TokenManager
import com.example.sipupuk.data.model.*
import com.example.sipupuk.data.remote.RetrofitClient
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class ProdukRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)

    // ========== KATEGORI & SATUAN (Public - No Auth) ==========

    /**
     * Get list kategori produk untuk dropdown
     */
    fun getKategoriProduk(): Flow<Resource<List<KategoriProduk>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getKategoriProduk()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil kategori"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /**
     * Get satuan berdasarkan kategori untuk dropdown dinamis
     */
    fun getSatuanByKategori(kategoriId: Int): Flow<Resource<List<Satuan>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getSatuanByKategori(kategoriId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil satuan"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    // ========== PRODUK PENJUAL ==========

    /**
     * Get produk milik penjual yang login
     */
    fun getMyProduk(
        perPage: Int = 50,
        kategoriId: Int? = null,
        status: String? = null,
        search: String? = null,
        page: Int = 1
    ): Flow<Resource<ProdukListData>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getMyProduk(
                token = "Bearer $token",
                perPage = perPage,
                kategoriId = kategoriId,
                status = status,
                search = search,
                page = page
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Data produk kosong"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
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
    ): Flow<Resource<Produk>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.createProduk(
                token = "Bearer $token",
                kategoriProdukId = kategoriProdukId.toString().toText(),
                satuanId = satuanId.toString().toText(),
                namaProduk = namaProduk.toText(),
                merk = merk.toText(),
                deskripsi = deskripsi?.toText(),
                berat = berat?.toString()?.toText(),
                harga = harga.toString().toText(),
                stok = stok.toString().toText(),
                fotoProduk = fotoUri?.let { uriToMultipart(it) }
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal menambahkan produk"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
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
    ): Flow<Resource<Produk>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_method", "PUT")
                .addFormDataPart("kategori_produk_id", kategoriProdukId.toString())
                .addFormDataPart("satuan_id", satuanId.toString())
                .addFormDataPart("nama_produk", namaProduk)
                .addFormDataPart("merk", merk)
                .addFormDataPart("harga", harga.toString())
                .addFormDataPart("stok", stok.toString())

            deskripsi?.let {
                builder.addFormDataPart("deskripsi", it)
            }

            berat?.let {
                builder.addFormDataPart("berat", it.toString())
            }

            status?.let {
                builder.addFormDataPart("status", it)
            }

            fotoUri?.let { uri ->
                builder.addPart(uriToMultipart(uri))
            }

            val requestBody = builder.build()

            val response = apiService.updateProdukRaw(
                token = "Bearer $token",
                id = id,
                body = requestBody
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal update produk"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /**
     * Delete produk
     */
    fun deleteProduk(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().firstOrNull()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.deleteProduk("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal menghapus produk"))
                }
            } else {
                emit(Resource.Error("Error: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        }
    }

    // ========== PRODUK PEMBELI ==========

    /**
     * Get produk untuk pembeli dengan filter kategori
     */
    fun getProduk(
        perPage: Int = 20,
        kategoriId: Int? = null,
        search: String? = null,
        tokoId: Int? = null,
        page: Int = 1
    ): Flow<Resource<ProdukListData>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getProduk(
                token = "Bearer $token",
                perPage = perPage,
                kategoriId = kategoriId,
                search = search,
                tokoId = tokoId,
                page = page
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Data produk kosong"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /**
     * Get detail produk
     */
    fun getProdukDetail(id: Int): Flow<Resource<Produk>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getProdukDetail("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Produk tidak ditemukan"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    // ========== HELPER FUNCTIONS ==========

    private fun String.toText(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IOException("Tidak bisa membuka file")

        val bytes = inputStream.readBytes()
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            "foto_produk", // WAJIB sama dengan Laravel
            "produk_${System.currentTimeMillis()}.jpg",
            requestBody
        )
    }
}