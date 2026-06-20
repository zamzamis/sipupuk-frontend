package com.example.sipupuk.ui.viewmodel

// Login UI State
data class LoginUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false
)

// Register UI State
data class RegisterUiState(
    val name: String = "",
    val phone: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val role: String = "pembeli",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmationVisible: Boolean = false
)

// Form Validation
object ValidationHelper {


    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password tidak boleh kosong"
            password.length < 4 -> "Password minimal 4 karakter"
            else -> null
        }
    }

    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama tidak boleh kosong"
            name.length < 3 -> "Nama minimal 3 karakter"
            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Nomor tidak boleh kosong"
            phone.isNotBlank() && phone.length < 10 -> "Nomor telepon minimal 10 digit"
            phone.isNotBlank() && !phone.matches(Regex("^[0-9]+$")) -> "Nomor telepon hanya boleh angka"
            else -> null
        }
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? {
        return when {
            confirmation.isBlank() -> "Konfirmasi password tidak boleh kosong"
            password != confirmation -> "Password tidak cocok"
            else -> null
        }
    }
}