package com.example.sipupuk.data.repository

import android.content.Context
import com.example.sipupuk.data.local.TokenManager
import com.example.sipupuk.data.model.*
import com.example.sipupuk.data.remote.RetrofitClient
import com.example.sipupuk.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)

    // Login
    fun login(phone: String, password: String): Flow<Resource<AuthData>> = flow {
        emit(Resource.Loading())

        try {
            val request = LoginRequest(phone, password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // Simpan token dan data user
                    body.data?.let { authData ->
                        tokenManager.saveToken(authData.token)
                        tokenManager.saveUserData(
                            userId = authData.user.id,
                            name = authData.user.name,
                            phone = authData.user.phone,
                            role = authData.user.role
                        )
                    }
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Login gagal"))
                }
            } else {
                // Handle error response
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Login gagal: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        }
    }

    // Register
    fun register(
        name: String,
        phone: String,
        password: String,
        passwordConfirmation: String,
        role: String = "pembeli"
    ): Flow<Resource<AuthData>> = flow {
        emit(Resource.Loading())

        try {
            val request = RegisterRequest(
                name = name,
                phone = phone,
                password = password,
                passwordConfirmation = passwordConfirmation,
                role = role
            )
            val response = apiService.register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // Simpan token dan data user
                    body.data?.let { authData ->
                        tokenManager.saveToken(authData.token)
                        tokenManager.saveUserData(
                            userId = authData.user.id,
                            name = authData.user.name,
                            phone = authData.user.phone,
                            role = authData.user.role
                        )
                    }
                    emit(Resource.Success(body.data!!))
                } else {
                    // Handle validation errors
                    val errorMessage = body?.errors?.values?.flatten()?.joinToString(", ")
                        ?: body?.message
                        ?: "Registrasi gagal"
                    emit(Resource.Error(errorMessage))
                }
            } else {
                emit(Resource.Error("Registrasi gagal: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        }
    }

    // Logout
    fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val token = tokenManager.getToken().firstOrNull()

        try {
            if (!token.isNullOrEmpty()) {
                val response = apiService.logout("Bearer $token")

                if (!response.isSuccessful) {
                    // optional: log error
                }
            }
        } catch (e: Exception) {
            // network error diabaikan
        } finally {
            // WAJIB: selalu clear data lokal
            tokenManager.clearAllData()
            emit(Resource.Success(Unit))
        }
    }


    // Get Current User
    fun getCurrentUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        try {
            // Get token from datastore
            var token: String? = null
            tokenManager.getToken().collect { savedToken ->
                token = savedToken
            }

            if (token != null) {
                val response = apiService.getUser("Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        emit(Resource.Success(body.data!!.user))
                    } else {
                        emit(Resource.Error(body?.message ?: "Gagal mengambil data user"))
                    }
                } else {
                    emit(Resource.Error("Error: ${response.message()}"))
                }
            } else {
                emit(Resource.Error("Token tidak ditemukan. Silakan login kembali."))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server."))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        }
    }

    fun checkPhone(phone: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            val request = CheckPhoneRequest(phone = phone)
            val response = apiService.checkPhone(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.message ?: "Nomor HP ditemukan"))
                } else {
                    emit(Resource.Error(body?.message ?: "Nomor HP tidak terdaftar"))
                }
            } else {
                // Backend kirim 404 kalau nomor gak ada, ini akan masuk ke else
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    ?: "Nomor HP tidak terdaftar"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        }
    }

    fun resetPassword(
        phone: String,
        password: String,
        passwordConfirmation: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            val request = ResetPasswordRequest(
                phone = phone,
                password = password,
                passwordConfirmation = passwordConfirmation
            )
            val response = apiService.resetPassword(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.message ?: "Password berhasil diubah"))
                } else {
                    val errorMessage = body?.errors?.values?.flatten()?.joinToString(", ")
                        ?: body?.message
                        ?: "Gagal mengubah password"
                    emit(Resource.Error(errorMessage))
                }
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    ?: "Gagal mengubah password: ${response.message()}"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Terjadi kesalahan"}"))
        }
    }

    private fun parseErrorMessage(errorBodyString: String?): String? {
        return try {
            val gson = Gson()
            val errorResponse = gson.fromJson(errorBodyString, ApiResponse::class.java)
            errorResponse?.message
        } catch (e: Exception) {
            null
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.isLoggedIn()
    }

    // Get saved token
    fun getToken(): Flow<String?> {
        return tokenManager.getToken()
    }

    // Get saved user data
    fun getUserData(): Flow<Map<String, String?>> {
        return tokenManager.getUserData()
    }
}