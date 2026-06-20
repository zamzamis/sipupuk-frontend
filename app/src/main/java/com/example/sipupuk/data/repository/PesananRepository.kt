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

class PesananRepository(context: Context) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)

    // ========== PENJUAL ==========

    /**
     * Get pesanan toko (penjual)
     */
    fun getTokoPesanan(
        perPage: Int = 50,
        status: String? = null,
        page: Int = 1
    ): Flow<Resource<PesananListData>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getTokoPesanan(
                token = "Bearer $token",
                perPage = perPage,
                status = status,
                page = page
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil data pesanan"))
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

    /**
     * Get statistik pesanan (penjual atau pembeli)
     */
    fun getPesananStatistics(): Flow<Resource<PesananStatistics>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getPesananStatistics("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil statistik"))
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

    /**
     * Get detail pesanan
     */
    fun getPesananDetail(id: Int): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getPesananDetail("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil detail pesanan"))
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

    /**
     * Konfirmasi pesanan (penjual)
     */
    fun konfirmasiPesanan(id: Int, catatanToko: String?): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val request = KonfirmasiPesananRequest(catatanToko)
            val response = apiService.konfirmasiPesanan("Bearer $token", id, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal konfirmasi pesanan"))
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

    /**
     * Set pesanan siap diambil (penjual)
     */
    fun siapDiambilPesanan(id: Int): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.siapDiambilPesanan("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal set pesanan siap diambil"))
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

    /**
     * Selesaikan pesanan (penjual)
     */
    fun selesaiPesanan(id: Int): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.selesaiPesanan("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal selesaikan pesanan"))
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

    /**
     * Tolak pesanan (penjual)
     */
    fun tolakPesanan(id: Int, catatanToko: String): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val request = TolakPesananRequest(catatanToko)
            val response = apiService.tolakPesanan("Bearer $token", id, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal tolak pesanan"))
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

    // ========== PEMBELI ==========

    /**
     * Create pesanan (checkout)
     */
    fun createPesanan(request: CheckoutRequest): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.createPesanan("Bearer $token", request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal membuat pesanan"))
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

    /**
     * Get pesanan pembeli
     */
    fun getMyPesanan(
        perPage: Int = 50,
        status: String? = null,
        page: Int = 1
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
                status = status,
                page = page
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil data pesanan"))
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

    /**
     * Get statistik pesanan pembeli
     */
    fun getMyPesananStatistics(): Flow<Resource<PesananStatistics>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getMyPesananStatistics("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil statistik"))
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

            val response = apiService.getMyPesananDetail("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil detail pesanan"))
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

    /**
     * Cancel pesanan (pembeli)
     */
    fun cancelPesanan(id: Int): Flow<Resource<Pesanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.cancelPesanan("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal membatalkan pesanan"))
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
}