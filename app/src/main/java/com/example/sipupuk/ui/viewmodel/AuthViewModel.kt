package com.example.sipupuk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipupuk.data.model.AuthData
import com.example.sipupuk.data.model.User
import com.example.sipupuk.data.repository.AuthRepository
import com.example.sipupuk.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {

    private val repository = AuthRepository(context)

    // Login State
    private val _loginState = MutableStateFlow<Resource<AuthData>?>(null)
    val loginState: StateFlow<Resource<AuthData>?> = _loginState.asStateFlow()

    // Register State
    private val _registerState = MutableStateFlow<Resource<AuthData>?>(null)
    val registerState: StateFlow<Resource<AuthData>?> = _registerState.asStateFlow()

    // Logout State
    private val _logoutState = MutableStateFlow<Resource<Unit>?>(null)
    val logoutState: StateFlow<Resource<Unit>?> = _logoutState.asStateFlow()

    // Current User State
    private val _userState = MutableStateFlow<Resource<User>?>(null)
    val userState: StateFlow<Resource<User>?> = _userState.asStateFlow()

    // Is Logged In State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    // Check if user is logged in
    private fun checkLoginStatus() {
        viewModelScope.launch {
            repository.isLoggedIn().collect { loggedIn ->
                _isLoggedIn.value = loggedIn
            }
        }
    }

    // Login Function
    fun login(phone: String, password: String) {
        viewModelScope.launch {
            repository.login(phone, password).collect { result ->
                _loginState.value = result
                if (result is Resource.Success) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    // Register Function
    fun register(
        name: String,
        phone: String,
        password: String,
        passwordConfirmation: String,
        role: String = "pembeli"
    ) {
        viewModelScope.launch {
            repository.register(
                name = name,
                phone = phone,
                password = password,
                passwordConfirmation = passwordConfirmation,
                role = role
            ).collect { result ->
                _registerState.value = result
                if (result is Resource.Success) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    // Logout Function
    fun logout() {
        viewModelScope.launch {
            repository.logout().collect { result ->
                _logoutState.value = result
                if (result is Resource.Success) {
                    _isLoggedIn.value = false
                    _loginState.value = null
                    _registerState.value = null
                    _userState.value = null
                }
            }
        }
    }

    // Get Current User
    fun getCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUser().collect { result ->
                _userState.value = result
            }
        }
    }

    // Get User Data from Local Storage
    fun getUserData() = repository.getUserData()

    // Get Token
    fun getToken() = repository.getToken()

    // Reset Login State (untuk clear error message)
    fun resetLoginState() {
        _loginState.value = null
    }

    // Reset Register State
    fun resetRegisterState() {
        _registerState.value = null
    }

    // Reset Logout State
    fun resetLogoutState() {
        _logoutState.value = null
    }
}