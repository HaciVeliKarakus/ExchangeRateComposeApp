package com.hvk.exchangerate.compose.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        
        val REFRESH_INTERVAL_KEY = intPreferencesKey("refresh_interval")
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        
        const val DEFAULT_REFRESH_INTERVAL = 10 // dakika
    }
    
    val refreshInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_INTERVAL_KEY] ?: DEFAULT_REFRESH_INTERVAL
    }
    
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }
    
    suspend fun setRefreshInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_INTERVAL_KEY] = minutes
        }
    }
    
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }
} 