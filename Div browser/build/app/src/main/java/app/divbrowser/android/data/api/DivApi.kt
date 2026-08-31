package app.divbrowser.android.data.api

import app.divbrowser.android.BuildConfig
import app.divbrowser.android.core.url.DivLegal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Phase 5 — HTTP client for Div Cloud (CORE / TUNNEL / EDGE).
 * All app traffic that is not plain WebView http(s) goes through here.
 */
object DivApi {

    private val JSON = "application/json; charset=utf-8".toMediaType()
    private const val CLIENT = "div-browser/0.1.0"

    val coreBase: String = BuildConfig.CORE_BASE.trimEnd('/')
    val tunnelBase: String = BuildConfig.TUNNEL_BASE.trimEnd('/')
    val edgeBase: String = BuildConfig.EDGE_BASE.trimEnd('/')

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun reqBuilder(url: String, apiKey: String? = null): Request.Builder {
        val b = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("X-Div-Client", CLIENT)
            .header("User-Agent", CLIENT)
        if (!apiKey.isNullOrBlank()) {
            b.header("Authorization", "Bearer $apiKey")
            b.header("X-Api-Key", apiKey)
        }
        return b
    }

    private suspend fun getJson(
        base: String,
        path: String,
        apiKey: String? = null,
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = base.trimEnd('/') + path
            val res = http.newCall(reqBuilder(url, apiKey).get().build()).execute()
            val body = res.body?.string().orEmpty()
            if (body.isBlank()) {
                return@withContext JSONObject().put("ok", res.isSuccessful).put("_httpStatus", res.code)
            }
            val obj = try {
                JSONObject(body)
            } catch (_: Exception) {
                JSONObject().put("ok", res.isSuccessful).put("raw", body.take(500))
            }
            obj.put("_httpStatus", res.code)
            obj
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun postJson(
        base: String,
        path: String,
        payload: JSONObject,
        apiKey: String? = null,
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = base.trimEnd('/') + path
            val body = payload.toString().toRequestBody(JSON)
            val res = http.newCall(reqBuilder(url, apiKey).post(body).build()).execute()
            val text = res.body?.string().orEmpty()
            if (text.isBlank()) {
                return@withContext JSONObject().put("ok", res.isSuccessful).put("_httpStatus", res.code)
            }
            val obj = try {
                JSONObject(text)
            } catch (_: Exception) {
                JSONObject().put("ok", res.isSuccessful).put("raw", text.take(500))
            }
            obj.put("_httpStatus", res.code)
            obj
        } catch (_: Exception) {
            null
        }
    }

    // --- Health ---
    /** True only for real 2xx + ok/status — never treat HTML 404 as success. */
    fun isHealthy(j: JSONObject?): Boolean {
        if (j == null) return false
        val code = j.optInt("_httpStatus", 0)
        if (code !in 200..299) return false
        if (j.optBoolean("ok", false)) return true
        if (j.optString("status").equals("ok", ignoreCase = true)) return true
        // artists-studio style without "ok" key but 200 JSON
        if (!j.has("error") && code in 200..299 && j.length() >= 1) return true
        return false
    }

    suspend fun health(): JSONObject? {
        val paths = listOf(
            coreBase to "/health",
            coreBase to "/v1/health",
            coreBase to "/api/v1/health",
            "https://artists-studio.onrender.com" to "/api/v1/health",
            "https://artists-studio.onrender.com" to "/health",
            "https://div-store.onrender.com" to "/api/health",
        )
        for ((base, path) in paths) {
            val j = getJson(base, path) ?: continue
            if (isHealthy(j)) return j
        }
        return null
    }

    // --- Keys / publish ---
    suspend fun createKey(username: String, domain: String, deviceId: String): JSONObject? {
        val payload = JSONObject()
            .put("username", username)
            .put("domain", domain)
            .put("deviceId", deviceId)
        return postJson(coreBase, "/v1/keys", payload)
    }

    suspend fun verifyKey(apiKey: String): JSONObject? =
        postJson(coreBase, "/v1/keys/verify", JSONObject().put("apiKey", apiKey), apiKey)

    // --- Divs legal pages ---
    suspend fun divsResolve(divsUrl: String): JSONObject? {
        val enc = java.net.URLEncoder.encode(divsUrl, Charsets.UTF_8.name())
        return getJson(coreBase, "/v1/divs/resolve?url=$enc")
    }

    suspend fun divsPageHtml(host: String, path: String): String? = withContext(Dispatchers.IO) {
        try {
            val q = "host=${java.net.URLEncoder.encode(host, "UTF-8")}" +
                "&path=${java.net.URLEncoder.encode(path, "UTF-8")}"
            val url = "$coreBase/v1/divs/page?$q"
            val res = http.newCall(reqBuilder(url).get().build()).execute()
            val body = res.body?.string().orEmpty()
            if (res.isSuccessful && body.isNotBlank() && !body.contains("\"error\"")) {
                return@withContext body
            }
            // Official browser.com pages always available offline
            if (host.equals("browser.com", ignoreCase = true)) {
                return@withContext DivLegal.offlineHtml(path)
            }
            // Fallback older legal routes
            val p = path.lowercase()
            val legal = when {
                p.contains("privacy") -> "$coreBase/v1/legal/privacy"
                p.contains("term") -> "$coreBase/v1/legal/terms"
                else -> null
            } ?: return@withContext null
            val r2 = http.newCall(reqBuilder(legal).get().build()).execute()
            val b2 = r2.body?.string().orEmpty()
            if (r2.isSuccessful && b2.isNotBlank()) b2
            else DivLegal.offlineHtml(path)
        } catch (_: Exception) {
            DivLegal.offlineHtml(path)
        }
    }

    // --- Tunnel ---
    suspend fun tunnelStatus(domain: String, apiKey: String? = null): JSONObject? {
        val d = java.net.URLEncoder.encode(domain, Charsets.UTF_8.name())
        return getJson(tunnelBase, "/v1/tunnel/status?domain=$d", apiKey)
    }

    suspend fun tunnelFetch(
        domain: String,
        path: String = "/",
        apiKey: String? = null,
    ): JSONObject? {
        val q = "domain=${java.net.URLEncoder.encode(domain, "UTF-8")}" +
            "&path=${java.net.URLEncoder.encode(path, "UTF-8")}"
        return getJson(tunnelBase, "/v1/tunnel/fetch?$q", apiKey)
    }

    // --- Device ---
    suspend fun registerDevice(deviceId: String, label: String = "android"): JSONObject? {
        val payload = JSONObject()
            .put("deviceId", deviceId)
            .put("label", label)
            .put("platform", "android")
        return postJson(coreBase, "/v1/device/register", payload)
    }

    // --- Update ---
    data class UpdateInfo(
        val available: Boolean,
        val versionCode: Int,
        val versionName: String,
        val notes: String,
        val downloadUrl: String,
    )

    suspend fun checkUpdate(currentCode: Int = BuildConfig.VERSION_CODE): UpdateInfo? {
        val q = "platform=android&versionCode=$currentCode"
        val r = getJson(coreBase, "/v1/update/check?$q") ?: return null
        val available = r.optBoolean("update") || r.optBoolean("available")
        return UpdateInfo(
            available = available,
            versionCode = r.optInt("versionCode", currentCode),
            versionName = r.optString("versionName", ""),
            notes = r.optString("notes", r.optString("changelog", "")),
            downloadUrl = r.optString("url").ifBlank {
                r.optString("downloadUrl")
            }.let {
                if (it.isBlank()) "$coreBase/v1/update/download?platform=android" else it
            },
        )
    }
}
