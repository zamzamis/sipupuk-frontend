package com.example.sipupuk.data.repository

import android.content.Context
import com.example.sipupuk.data.local.TokenManager
import com.example.sipupuk.data.model.LaporanBulanan
import com.example.sipupuk.data.remote.ApiService
import com.example.sipupuk.data.remote.RetrofitClient
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class LaporanRepository(
    private val context: Context
) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)
    fun getLaporanBulanan(): Flow<Resource<LaporanBulanan>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getLaporanBulanan("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil laporan"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }
}
