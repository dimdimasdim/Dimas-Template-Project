package com.dimas.dimasproject.core.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettings(
    private val dataStore: DataStore<Preferences>
) {

    // ── Keys ────────────────────────────────────────────────────────────────────
    companion object Keys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = intPreferencesKey("user_id")
    }

    // ── Auth Token ───────────────────────────────────────────────────────────────
    val authToken: Flow<String?> = dataStore.data.map { it[AUTH_TOKEN] }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { it[AUTH_TOKEN] = token }
    }

    suspend fun clearAuthToken() {
        dataStore.edit { it.remove(AUTH_TOKEN) }
    }

    // ── Login State ──────────────────────────────────────────────────────────────
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN] ?: false }

    suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { it[IS_LOGGED_IN] = value }
    }

    // ── User ID ──────────────────────────────────────────────────────────────────
    val userId: Flow<Int?> = dataStore.data.map { it[USER_ID] }

    suspend fun saveUserId(id: Int) {
        dataStore.edit { it[USER_ID] = id }
    }

    // ── Clear All ────────────────────────────────────────────────────────────────
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
