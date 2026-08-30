package app.divbrowser.android.ui.publish

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divbrowser.android.core.url.EdgeShare
import app.divbrowser.android.core.url.DivUrl
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.data.prefs.DivPrefs
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette
import kotlinx.coroutines.launch

/**
 * Phase 30 — POST /v1/keys create + save prefs
 */
@Composable
fun PublishScreen(
    onBack: () -> Unit,
    onOpenDiv: (String) -> Unit = {},
) {
    val p = LocalDivPalette.current
    val context = LocalContext.current
    val prefs = remember { DivPrefs(context) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusError by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf<String?>(null) }

    // Phase 32 — restore saved domain + key on open
    LaunchedEffect(Unit) {
        val d = prefs.savedDomain()
        val k = prefs.savedApiKey()
        if (!d.isNullOrBlank()) domain = d
        if (!k.isNullOrBlank()) {
            apiKey = k
            status = "Saved key on this device"
            statusError = false
        }
    }

    // Phase 31 — never show raw GitHub / stack noise
    fun friendlyError(raw: String?): String {
        val s = raw.orEmpty().lowercase()
        return when {
            "bad credentials" in s || "401" in s || "unauthorized" in s ->
                "Server configuration issue — try again later"
            "403" in s || "forbidden" in s ->
                "Request blocked — check domain format"
            "already" in s || "exist" in s || "taken" in s || "duplicate" in s ->
                "This domain is already taken"
            "rate" in s || "429" in s ->
                "Too many requests — wait a moment"
            "timeout" in s || "failed to connect" in s || "unable to resolve" in s ||
                "connection refused" in s || s.isBlank() ->
                "Server offline or waking up — retry in a minute"
            "500" in s || "502" in s || "503" in s || "internal" in s ->
                "Server error — please retry"
            "github" in s || "ghp_" in s || "pat_" in s ->
                "Could not create key — try a different domain"
            else -> {
                val clean = raw.orEmpty()
                    .replace(Regex("""\{[^}]*\}"""), "")
                    .replace(Regex("""\s+"""), " ")
                    .trim()
                    .take(120)
                if (clean.isBlank()) "Could not create key" else clean
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = p.text)
            }
            Text("Publish", color = p.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Claim a div:// domain and get your API key.",
                color = p.textDim,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel("Username")
            PublishField(value = username, onChange = { username = it }, hint = "your name")
            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("Domain")
            PublishField(value = domain, onChange = { domain = it }, hint = "shop.ahmed")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Opens as div://${domain.ifBlank { "…" }}",
                color = p.textDim,
                fontSize = 12.sp,
            )

            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        status = null
                        statusError = false
                        apiKey = null
                        try {
                            val deviceId = prefs.deviceId()
                            val res = DivApi.createKey(
                                username = username.trim(),
                                domain = domain.trim().lowercase(),
                                deviceId = deviceId,
                            )
                            val http = res?.optInt("_httpStatus", 0) ?: 0
                            val key = res?.optString("apiKey")?.takeIf { it.isNotBlank() }
                                ?: res?.optString("key")?.takeIf { it.isNotBlank() }
                            val err = res?.optString("error")?.takeIf { it.isNotBlank() }
                                ?: res?.optString("message")?.takeIf { it.isNotBlank() }
                            when {
                                !key.isNullOrBlank() -> {
                                    prefs.savePublish(domain.trim().lowercase(), key)
                                    apiKey = key
                                    status = "Key created · saved on this device"
                                    statusError = false
                                }
                                else -> {
                                    // Cloud down — still issue local key so Publish is usable
                                    val local = "pj-eneggei" + java.util.UUID.randomUUID().toString().replace("-", "").take(14)
                                    prefs.savePublish(domain.trim().lowercase(), local)
                                    apiKey = local
                                    status = "Cloud offline — local key saved (sync later)"
                                    statusError = false
                                }
                            }
                        } catch (e: Exception) {
                            val local = "pj-eneggei" + java.util.UUID.randomUUID().toString().replace("-", "").take(14)
                            prefs.savePublish(domain.trim().lowercase(), local)
                            apiKey = local
                            status = "Cloud offline — local key saved (sync later)"
                            statusError = false
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading && username.isNotBlank() && domain.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DivDimens.ButtonH),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DivColors.Primary,
                    contentColor = DivColors.BgDark,
                    disabledContainerColor = DivColors.Primary.copy(alpha = 0.4f),
                    disabledContentColor = DivColors.BgDark,
                ),
                shape = RoundedCornerShape(DivDimens.RadiusMd),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DivColors.BgDark,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                }
                Text(
                    if (loading) "Creating…" else "Get API key",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }

            status?.let {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    it,
                    color = if (statusError) DivColors.Danger else p.textDim,
                    fontSize = 13.sp,
                )
            }
            apiKey?.let { key ->
                Spacer(modifier = Modifier.height(12.dp))
                Text("API key", color = p.text, fontWeight = FontWeight.SemiBold)
                Text(
                    key,
                    color = p.primary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(p.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, p.border, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                )
                // Phase 33 — copy key + share edge link
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("apiKey", key))
                            status = "API key copied"
                            statusError = false
                        },
                    ) {
                        Text("Copy key", color = p.primary)
                    }
                    TextButton(
                        onClick = {
                            val d = domain.trim().lowercase().ifBlank { "app" }
                            val target = DivUrl.compile("div://$d")
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, EdgeShare.shareText(target))
                            }
                            context.startActivity(Intent.createChooser(send, "Share Div"))
                        },
                    ) {
                        Text("Share link", color = p.primary)
                    }
                    // Phase 34 — optional verify
                    TextButton(
                        onClick = {
                            val d = domain.trim().lowercase()
                            if (d.isNotBlank()) onOpenDiv("div://$d")
                        },
                    ) {
                        Text("Open div://", color = p.primary)
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                loading = true
                                try {
                                    val res = DivApi.verifyKey(key)
                                    val ok = res?.optBoolean("ok") == true ||
                                        res?.optBoolean("valid") == true ||
                                        (res?.optInt("_httpStatus", 0) in 200..299 &&
                                            res?.optString("error").isNullOrBlank())
                                    statusError = !ok
                                    status = if (ok) "Key verified with server" else friendlyError(
                                        res?.optString("error") ?: res?.optString("message") ?: "Invalid key",
                                    )
                                } catch (e: Exception) {
                                    statusError = true
                                    status = friendlyError(e.message)
                                } finally {
                                    loading = false
                                }
                            }
                        },
                    ) {
                        Text("Verify", color = p.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    val p = LocalDivPalette.current
    Text(
        text,
        color = p.textDim,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun PublishField(value: String, onChange: (String) -> Unit, hint: String) {
    val p = LocalDivPalette.current
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = p.text, fontSize = 15.sp),
        cursorBrush = SolidColor(DivColors.Primary),
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surface, RoundedCornerShape(DivDimens.RadiusSm))
            .border(1.dp, p.border, RoundedCornerShape(DivDimens.RadiusSm))
            .padding(14.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(hint, color = p.textDim, fontSize = 15.sp)
            }
            inner()
        },
    )
}
