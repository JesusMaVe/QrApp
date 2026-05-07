package com.example.qrapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qrapplication.model.GeneratedQr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.generatorDataStore: DataStore<Preferences> by preferencesDataStore(name = "generator_history")

class GeneratorRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val historyKey = stringPreferencesKey("qr_history")

    val generatedQrs: Flow<List<GeneratedQr>> = context.generatorDataStore.data.map { preferences ->
        val historyJson = preferences[historyKey] ?: "[]"
        runCatching {
            json.decodeFromString<List<GeneratedQr>>(historyJson)
        }.getOrDefault(emptyList())
    }

    suspend fun saveQr(content: String) {
        context.generatorDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val currentList = runCatching {
                json.decodeFromString<List<GeneratedQr>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            val existingIndex = currentList.indexOfFirst { it.content == content }
            if (existingIndex != -1) {
                currentList[existingIndex] = currentList[existingIndex].copy(
                    timestamp = System.currentTimeMillis()
                )
            } else {
                currentList.add(0, GeneratedQr(content = content))
            }

            val newJson = json.encodeToString(currentList.take(20))
            preferences[historyKey] = newJson
        }
    }

    suspend fun deleteQr(id: String) {
        context.generatorDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val currentList = runCatching {
                json.decodeFromString<List<GeneratedQr>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            currentList.removeAll { it.id == id }

            val newJson = json.encodeToString(currentList)
            preferences[historyKey] = newJson
        }
    }
}