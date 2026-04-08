package com.example.qrapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qrapplication.model.ScanRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scan_history")

class ScanRepository(context: Context) {

    private val dataStore = context.dataStore

    private val historyKey = stringPreferencesKey("scan_history_list")

    val scans: Flow<List<ScanRecord>> = dataStore.data
        .map { preferences ->
            val json = preferences[historyKey] ?: "[]"
            runCatching {
                Json.decodeFromString<List<ScanRecord>>(json)
            }.getOrDefault(emptyList())
        }

    suspend fun saveScan(record: ScanRecord) {
        dataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val currentList = runCatching {
                Json.decodeFromString<List<ScanRecord>>(currentJson)
            }.getOrDefault(emptyList())
            val updatedList = listOf(record) + currentList
            preferences[historyKey] = Json.encodeToString(updatedList)
        }
    }

    suspend fun deleteScan(id: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val currentList = runCatching {
                Json.decodeFromString<List<ScanRecord>>(currentJson)
            }.getOrDefault(emptyList())
            val updatedList = currentList.filter { it.id != id }
            preferences[historyKey] = Json.encodeToString(updatedList)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(historyKey)
        }
    }
}
