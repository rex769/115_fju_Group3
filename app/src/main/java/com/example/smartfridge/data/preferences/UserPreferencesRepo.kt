package com.example.smartfridge.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepo(private val context: Context) {
    companion object {
        val IS_SENIOR_MODE = booleanPreferencesKey("is_senior_mode")
    }

    val isSeniorMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_SENIOR_MODE] ?: false
    }

    suspend fun setSeniorMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SENIOR_MODE] = enabled
        }
    }
}
