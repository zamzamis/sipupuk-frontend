package com.example.sipupuk.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    // Save token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    // Get token
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    // Save user data
    suspend fun saveUserData(userId: Int, name: String, phone: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId.toString()
            preferences[USER_NAME_KEY] = name
            preferences[USER_PHONE_KEY] = phone
            preferences[USER_ROLE_KEY] = role
        }
    }

    // Get user data
    fun getUserData(): Flow<Map<String, String?>> {
        return context.dataStore.data.map { preferences ->
            mapOf(
                "userId" to preferences[USER_ID_KEY],
                "name" to preferences[USER_NAME_KEY],
                "phone" to preferences[USER_PHONE_KEY],
                "role" to preferences[USER_ROLE_KEY]
            )
        }
    }

    // Clear all data (logout)
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY] != null
        }
    }
}