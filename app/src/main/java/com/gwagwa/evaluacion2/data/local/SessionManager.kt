package com.gwagwa.evaluacion2.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import androidx.datastore.core.DataStore


/**
 * SessionManager: Guarda y recupera el token JWT de forma segura
 */
class SessionManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "session_prefs")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")

        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }


    /**
     * Guarda el token de autenticación
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
        }
    }


    /**
     * Guarda el rol del usuario.
     */
    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ROLE] = role
        }
    }

    /**
     * Guarda el email del usuario.
     */
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_EMAIL] = email
        }
    }

    /**
    * Guarda el id del usuario.
    */
    suspend fun saveUserId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
        }
    }
    /**
     * Recupera el id guardado
     */
    suspend fun getUserId(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[KEY_USER_ID] }
            .first()
    }

    /**
     * Recupera el rol guardado
     */
    suspend fun getUserRole(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[KEY_USER_ROLE] }
            .first()
    }

    /**
     * Recupera el email guardado
     */
    suspend fun getUserEmail(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[KEY_USER_EMAIL] }
            .first()
    }

    /**
     * Recupera el token guardado
     */
    suspend fun getAuthToken(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[KEY_AUTH_TOKEN] }
            .first()
    }

    /**
     * Expone un Flow que emite el token cada vez que cambia para ver el estado del login
     */
    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_AUTH_TOKEN]
        }

    val userRole: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_USER_ROLE]
        }


    /**
     * Elimina el token (cerrar sesión)
     */
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_TOKEN)
            preferences.remove(KEY_USER_ROLE)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_ID)
        }
    }
}