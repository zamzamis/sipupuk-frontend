package com.example.sipupuk.data.repository

import android.content.Context
import android.net.Uri
import com.example.sipupuk.data.local.TokenManager
import com.example.sipupuk.data.model.Toko
import com.example.sipupuk.data.remote.RetrofitClient
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class TokoRepository(
    private val context: Context
) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)



    /* ===================== GET TOKO ===================== */

    fun getMyToko(): Flow<Resource<Toko>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()

            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.getMyToko("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Data toko tidak ditemukan"))
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

    /* ===================== CREATE TOKO ===================== */

    fun createToko(
        namaToko: String,
        alamat: String,
        desa: String,
        noTelepon: String,
        deskripsi: String?,
        jamBuka: String?,
        jamTutup: String?,
//        fotoUri: Uri?
    ): Flow<Resource<Toko>> = flow {
        emit(Resource.Loading())

        try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            val response = apiService.createToko(
                token = "Bearer $token",
                namaToko = namaToko.toText(),
                alamat = alamat.toText(),
                desa = desa.toText(),
                noTelepon = noTelepon.toText(),
                deskripsi = deskripsi?.toText(),
                jamBuka = jamBuka?.toText(),
                jamTutup = jamTutup?.toText(),
//                fotoToko = fotoUri?.let { uriToMultipart(it) }
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal membuat toko"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }

    /* ===================== UPDATE TOKO ===================== */

    fun updateToko(
        namaToko: String,
        alamat: String,
        desa: String,
        noTelepon: String,
        deskripsi: String?,
        jamBuka: String?,
        jamTutup: String?,
//        fotoUri: Uri?
    ): Flow<Resource<Toko>> = flow {
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
                .addFormDataPart("nama_toko", namaToko)
                .addFormDataPart("alamat", alamat)
                .addFormDataPart("desa", desa)
                .addFormDataPart("no_telepon", noTelepon)

            deskripsi?.let { builder.addFormDataPart("deskripsi", it) }
            jamBuka?.let { builder.addFormDataPart("jam_buka", it) }
            jamTutup?.let { builder.addFormDataPart("jam_tutup", it) }

//            fotoUri?.let { uri ->
//                val part = uriToMultipart(uri)
//                builder.addPart(part)
//            }

            val requestBody = builder.build()

            val response = apiService.updateTokoRaw(
                token = "Bearer $token",
                body = requestBody
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal update toko"))
                }
            } else {
                emit(Resource.Error("Error ${response.code()}"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan"))
        }
    }



    /* ===================== HELPER ===================== */

    private fun String.toText(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    /**
     * HARUS DI REPOSITORY
     * Convert Uri -> MultipartBody.Part
     */
    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IOException("Tidak bisa membuka file")

        val bytes = inputStream.readBytes()
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            "foto_toko", // WAJIB sama dengan Laravel
            "toko.jpg",
            requestBody
        )
    }
}