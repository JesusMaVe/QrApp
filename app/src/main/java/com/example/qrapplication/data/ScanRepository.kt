package com.example.qrapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qrapplication.model.ScanRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scan_history")

class ScanRepository(context: Context) {

    private val dataStore = context.dataStore

    private val historyKey = stringPreferencesKey("scan_history_list")

    // Use dedicated scope instead of GlobalScope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In-memory cache to avoid repeated DataStore reads
    // Uses AtomicReference for thread-safe updates
    private val cacheReference = AtomicReference<List<ScanRecord>>(emptyList())

    // Fast access cache - provides immediate data without waiting for DataStore
    // Uses concurrent map for O(1) lookups by content
    private val contentIndexCache = ConcurrentHashMap<String, ScanRecord>()

    // StateFlow for reactive UI updates - shares cached data immediately
    // Starts with cached data if available, otherwise empty list
    private val _scansFlow = MutableStateFlow<List<ScanRecord>>(emptyList())

    val scans: StateFlow<List<ScanRecord>> = _scansFlow.asStateFlow()

    init {
        // Pre-load data from DataStore on initialization
        // This runs in background and updates the cache when ready
        scope.launch {
            dataStore.data.collect { preferences ->
                val json = preferences[historyKey] ?: "[]"
                val loadedList = runCatching {
                    Json.decodeFromString<List<ScanRecord>>(json)
                }.getOrDefault(emptyList())

                // Update both cache and index
                cacheReference.set(loadedList)
                rebuildContentIndex(loadedList)
                _scansFlow.value = loadedList
            }
        }
    }

    private fun rebuildContentIndex(list: List<ScanRecord>) {
        contentIndexCache.clear()
        list.forEach { record ->
            contentIndexCache[record.content] = record
        }
    }

    private suspend fun getCurrentList(): List<ScanRecord> {
        // Check cache first for performance
        val cached = cacheReference.get()
        if (cached.isNotEmpty()) {
            return cached
        }

        // Fallback to DataStore if cache is empty
        return dataStore.data.first()[historyKey]?.let { json ->
            runCatching {
                Json.decodeFromString<List<ScanRecord>>(json)
            }.getOrDefault(emptyList()).also {
                cacheReference.set(it)
                rebuildContentIndex(it)
            }
        } ?: emptyList()
    }

    suspend fun saveScan(record: ScanRecord) {
        dataStore.edit { preferences ->
            val currentList = cacheReference.get().ifEmpty {
                preferences[historyKey]?.let { json ->
                    runCatching {
                        Json.decodeFromString<List<ScanRecord>>(json)
                    }.getOrDefault(emptyList())
                } ?: emptyList()
            }

            val existingIndex = currentList.indexOfFirst { it.content == record.content }
            val updatedList = if (existingIndex >= 0) {
                val existing = currentList[existingIndex]
                val updated = existing.copy(timestamp = System.currentTimeMillis())
                currentList.toMutableList().apply {
                    removeAt(existingIndex)
                    add(0, updated)
                }
            } else {
                listOf(record) + currentList
            }

            // Update cache immediately (optimistic update)
            cacheReference.set(updatedList)
            rebuildContentIndex(updatedList)
            _scansFlow.value = updatedList

            preferences[historyKey] = Json.encodeToString(updatedList)
        }
    }

    suspend fun findByContent(content: String): ScanRecord? {
        // O(1) lookup from cache
        return contentIndexCache[content]
    }

    suspend fun deleteScan(id: String) {
        dataStore.edit { preferences ->
            val currentList = cacheReference.get().ifEmpty {
                preferences[historyKey]?.let { json ->
                    runCatching {
                        Json.decodeFromString<List<ScanRecord>>(json)
                    }.getOrDefault(emptyList())
                } ?: emptyList()
            }

            val updatedList = currentList.filter { it.id != id }

            // Update cache immediately
            cacheReference.set(updatedList)
            rebuildContentIndex(updatedList)
            _scansFlow.value = updatedList

            preferences[historyKey] = Json.encodeToString(updatedList)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            // Clear cache immediately
            cacheReference.set(emptyList())
            contentIndexCache.clear()
            _scansFlow.value = emptyList()

            preferences.remove(historyKey)
        }
    }
}

// Extension to use empty list as default
private fun <T> List<T>.ifEmpty(default: () -> List<T>): List<T> {
    return if (isEmpty()) default() else this
}