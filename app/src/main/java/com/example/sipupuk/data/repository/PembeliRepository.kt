package com.example.sipupuk.data.repository

import android.content.Context
import com.example.sipupuk.data.local.TokenManager
import com.example.sipupuk.data.model.*
import com.example.sipupuk.data.remote.RetrofitClient
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class PembeliRepository(
    private val context: Context
) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)

    /* ===================== CREATE PESANAN (CHECKOUT) ===================== */

    /**
     * Buat pesanan baru (checkout)
     */
    fun createPesanan(
        tokoId: Int,
        items: List<CheckoutItem>,
        catatanPembeli: String?
    ): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val request = CheckoutRequest(
                tokoId = tokoId,
                items = items,
                catatanPembeli = catatanPembeli
            )

            val response = apiService.createPesanan(
                token = "Bearer $token",
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal membuat pesanan"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Error ${response.code()}: ${errorBody ?: "Unknown error"}"))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /* ===================== GET MY PESANAN ===================== */

    /**
     * Get daftar pesanan pembeli
     */
    fun getMyPesanan(
        perPage: Int = 50,
        status: String? = null
    ): Flow<Resource<PesananListData>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getMyPesanan(
                token = "Bearer $token",
                perPage = perPage,
                status = status
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Data tidak ditemukan"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /* ===================== GET MY PESANAN DETAIL ===================== */

    /**
     * Get detail pesanan pembeli
     */
    fun getMyPesananDetail(id: Int): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getMyPesananDetail(
                token = "Bearer $token",
                id = id
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Data tidak ditemukan"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Error ${response.code()}: ${errorBody ?: "Unknown error"}"))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /* ===================== CANCEL PESANAN ===================== */

    /**
     * Batalkan pesanan
     */
    fun cancelPesanan(id: Int): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.cancelPesanan(
                token = "Bearer $token",
                id = id
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal membatalkan pesanan"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Error ${response.code()}: ${errorBody ?: "Unknown error"}"))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }
}