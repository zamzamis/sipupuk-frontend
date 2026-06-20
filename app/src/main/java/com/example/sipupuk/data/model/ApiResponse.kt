package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

// Generic API Response
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)

// Auth Data Response
data class AuthData(
    @SerializedName("user")
    val user: User,

    @SerializedName("token")
    val token: String,

    @SerializedName("token_type")
    val tokenType: String
)

// User Model
data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("role")
    val role: String
)

// User Data Response (untuk endpoint /user)
data class UserKata(
    @SerializedName("user")
    val user: User
)