package com.toka.studyboost.red

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion_usuario")

class SesionUsuario(private val context: Context) {
    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userId: Flow<String?> = context.dataStore.data.map { pref -> pref[USER_ID] }
    val userName: Flow<String?> = context.dataStore.data.map { pref -> pref[USER_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { pref -> pref[USER_EMAIL] }

    suspend fun guardarSesion(id: String, nombre: String, email: String) {
        context.dataStore.edit { pref ->
            pref[USER_ID] = id
            pref[USER_NAME] = nombre
            pref[USER_EMAIL] = email
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { pref ->
            pref.clear()
        }
    }
}
