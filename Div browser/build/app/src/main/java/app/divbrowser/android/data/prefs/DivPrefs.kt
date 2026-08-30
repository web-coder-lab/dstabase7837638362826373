package app.divbrowser.android.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.divDataStore by preferencesDataStore("div_prefs")

/**
 * Phase 4 — local preferences (theme, device, publish key, pane session)
 */
class DivPrefs(private val context: Context) {

    private val darkKey = booleanPreferencesKey("dark_mode")
    private val deviceKey = stringPreferencesKey("device_id")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val domainKey = stringPreferencesKey("domain")
    private val paneCountKey = intPreferencesKey("pane_count")
    private val paneUrlsKey = stringPreferencesKey("pane_urls")

    val darkMode: Flow<Boolean> = context.divDataStore.data.map { it[darkKey] ?: true }

    suspend fun setDarkMode(enabled: Boolean) {
        context.divDataStore.edit { it[darkKey] = enabled }
    }

    suspend fun deviceId(): String {
        val existing = context.divDataStore.data.map { it[deviceKey] }.first()
        if (!existing.isNullOrBlank()) return existing
        val id = "d_" + UUID.randomUUID().toString().replace("-", "").take(16)
        context.divDataStore.edit { it[deviceKey] = id }
        return id
    }

    suspend fun savePublish(domain: String, apiKey: String) {
        context.divDataStore.edit {
            it[domainKey] = domain.trim()
            it[apiKeyKey] = apiKey.trim()
        }
    }

    suspend fun clearPublish() {
        context.divDataStore.edit {
            it.remove(domainKey)
            it.remove(apiKeyKey)
        }
    }

    suspend fun savedDomain(): String? =
        context.divDataStore.data.map { it[domainKey] }.first()?.takeIf { it.isNotBlank() }

    suspend fun savedApiKey(): String? =
        context.divDataStore.data.map { it[apiKeyKey] }.first()?.takeIf { it.isNotBlank() }

    suspend fun savePaneSession(urls: List<String>) {
        val clean = urls.map { it.trim() }.filter { it.isNotEmpty() }.take(4)
        context.divDataStore.edit {
            it[paneCountKey] = clean.size.coerceIn(0, 4)
            it[paneUrlsKey] = clean.joinToString("\n")
        }
    }

    suspend fun loadPaneSession(): List<String>? {
        val raw = context.divDataStore.data.map { it[paneUrlsKey] }.first() ?: return null
        val list = raw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.take(4)
        return list.ifEmpty { null }
    }

    suspend fun paneCount(): Int =
        context.divDataStore.data.map { it[paneCountKey] ?: 0 }.first()
}
