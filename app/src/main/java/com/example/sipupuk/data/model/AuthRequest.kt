package com.example.sipupuk.data.model

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("phone")
    val phone: String,

    @SerializedName("password")
    val password: String
)

// Register Request
data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("password_confirmation")
    val passwordConfirmation: String,

    @SerializedName("role")
    val role: String = "pembeli"
)