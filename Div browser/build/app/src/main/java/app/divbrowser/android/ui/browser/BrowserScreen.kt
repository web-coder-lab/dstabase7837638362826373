package app.divbrowser.android.ui.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.divbrowser.android.core.url.DivLegal
import app.divbrowser.android.core.url.DivUrl
import app.divbrowser.android.core.url.EdgeShare
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.data.prefs.DivPrefs
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Phase 27 — Home/Settings nav + share/shield/research/eye
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    initialUrl: String,
    onHome: () -> Unit,
    onSettings: () -> Unit = {},
) {
    val p = LocalDivPalette.current
    val context = LocalContext.current
    val prefs = remember { DivPrefs(context) }

    var currentUrl by rememberSaveable { mutableStateOf(initialUrl.ifBlank { "https://example.com" }) }
    var loadToken by remember { mutableStateOf(0) }
    var chromeVisible by rememberSaveable { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf(currentUrl) }
    var showResearch by remember { mutableStateOf(false) }
    var showShield by remember { mutableStateOf(false) }
    val jsEnabled by prefs.jsEnabled.collectAsState(initial = true)
    val blockPopups by prefs.blockPopups.collectAsState(initial = true)
    var divOnline by remember { mutableStateOf(true) }

    // Phase 51 — live tunnel status for remote div://
    LaunchedEffect(currentUrl) {
        val t = DivUrl.compile(currentUrl)
        if (t.isDivs || t.localOnly || t.httpUrl != null) {
            divOnline = true
            return@LaunchedEffect
        }
        val domain = t.domain ?: return@LaunchedEffect
        while (isActive) {
            val key = try { prefs.savedApiKey() } catch (_: Exception) { null }
            val st = try { DivApi.tunnelStatus(domain, key) } catch (_: Exception) { null }
            divOnline = st?.optBoolean("online") == true ||
                st?.optBoolean("ok") == true ||
                st?.optString("status") == "online"
            delay(12_000)
        }
    }


    val target = DivUrl.compile(currentUrl)
    val badge = when {
        target.isDivs -> "DIVS"
        target.localOnly -> "LOCAL"
        target.display.startsWith("div://", ignoreCase = true) -> "DIV"
        else -> "WEB"
    }
    val badgeColor = when (badge) {
        "DIVS", "DIV" -> DivColors.Secondary
        "LOCAL" -> DivColors.Success
        else -> DivColors.Primary
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
            webView = null
        }
    }

    LaunchedEffect(currentUrl, loadToken, webView) {
        val wv = webView ?: return@LaunchedEffect
        val t = DivUrl.compile(currentUrl)
        loading = true
        progress = 0.08f
        when {
            t.isDivs -> {
                val host = t.domain ?: "browser.com"
                val path = t.path.ifBlank { "/" }
                // Official pages: offline first (never blank when cloud is down)
                val html = if (host.equals("browser.com", ignoreCase = true)) {
                    DivApi.divsPageHtml(host, path) ?: DivLegal.offlineHtml(path)
                } else {
                    DivApi.divsPageHtml(host, path)
                        ?: """
                    <html><body style="background:#070A12;color:#8B95A8;font-family:sans-serif;padding:24px">
                    <h3 style="color:#B47CFF">Unavailable</h3>
                    <p>Could not load ${t.display}</p>
                    </body></html>
                    """.trimIndent()
                }
                withContext(Dispatchers.Main) {
                    wv.loadDataWithBaseURL("https://$host/", html, "text/html", "utf-8", null)
                }
            }
            t.localOnly && !t.httpUrl.isNullOrBlank() -> {
                withContext(Dispatchers.Main) { wv.loadUrl(t.httpUrl!!) }
            }
            !t.httpUrl.isNullOrBlank() -> {
                withContext(Dispatchers.Main) { wv.loadUrl(t.httpUrl!!) }
            }
            t.display.startsWith("http://") || t.display.startsWith("https://") -> {
                withContext(Dispatchers.Main) { wv.loadUrl(t.display) }
            }
            else -> {
                val domain = t.domain
                val path = t.path.ifBlank { "/" }
                val key = prefs.savedApiKey()
                var loaded = false
                if (!domain.isNullOrBlank()) {
                    val fetched = try {
                        DivApi.tunnelFetch(domain, path, key)
                    } catch (_: Exception) {
                        null
                    }
                    val htmlBody = fetched?.optString("html")?.takeIf { it.isNotBlank() }
                        ?: fetched?.optString("body")?.takeIf { it.isNotBlank() }
                    if (!htmlBody.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            wv.loadDataWithBaseURL("https://$domain/", htmlBody, "text/html", "utf-8", null)
                        }
                        loaded = true
                    }
                }
                if (!loaded) {
                    val host = domain ?: "div"
                    val html = """
                        <html><body style="background:#070A12;color:#8B95A8;font-family:sans-serif;padding:24px">
                        <h3 style="color:#B47CFF">div://$host</h3>
                        <p>Div surface — opens when the publisher tunnel is online.</p>
                        </body></html>
                    """.trimIndent()
                    withContext(Dispatchers.Main) {
                        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = if (chromeVisible) DivDimens.TopBarH else 0.dp),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = jsEnabled
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.mediaPlaybackRequiresUserGesture = true
                    settings.userAgentString = settings.userAgentString + " DivBrowser/0.1"
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean = false

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: android.graphics.Bitmap?,
                        ) {
                            loading = true
                            progress = 0.05f
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            loading = false
                            progress = 1f
                            pageTitle = view?.title?.takeIf { !it.isNullOrBlank() } ?: (url ?: currentUrl)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame != true) return
                            loading = false
                            progress = 1f
                            val msg = error?.description?.toString() ?: "Page unavailable"
                            val html = """
                                <html><body style="background:#070A12;color:#8B95A8;font-family:sans-serif;padding:24px">
                                <h3 style="color:#FF5C7A">Could not load</h3>
                                <p>$msg</p>
                                </body></html>
                            """.trimIndent()
                            view?.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress / 100f
                            loading = newProgress in 1..99
                        }
                    }
                    webView = this
                }
            },
            update = { wv ->
                wv.settings.javaScriptEnabled = jsEnabled
                // blockPopups: deny window.open when enabled
                wv.webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        progress = newProgress / 100f
                        loading = newProgress in 1..99
                    }
                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: android.os.Message?,
                    ): Boolean {
                        if (blockPopups) return true // swallow popup
                        return false
                    }
                }
            },
        )

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
                .statusBarsPadding(),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DivDimens.TopBarH)
                        .background(p.surface.copy(alpha = 0.96f))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onHome) {
                        Icon(Icons.Rounded.Home, contentDescription = "Home", tint = p.text)
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(DivDimens.RadiusSm))
                            .background(p.surface2)
                            .clickable { showResearch = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ModeBadge(label = badge, color = badgeColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pageTitle,
                            color = p.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    IconButton(
                        onClick = {
                            val t = DivUrl.compile(currentUrl)
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, EdgeShare.shareText(t))
                            }
                            context.startActivity(Intent.createChooser(send, "Share"))
                        },
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = "Share", tint = p.textDim)
                    }
                    IconButton(onClick = { showShield = true }) {
                        Icon(Icons.Rounded.Shield, contentDescription = "Shield", tint = p.primary)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = p.textDim)
                    }
                }
                if (loading) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = DivColors.Primary,
                        trackColor = Color.Transparent,
                    )
                }
            }
        }

        // Phase 25 — Eye FAB (always reachable corner)
        IconButton(
            onClick = { chromeVisible = !chromeVisible },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
                .size(DivDimens.Fab)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (chromeVisible) p.surface2
                    else DivColors.Primary.copy(alpha = 0.2f),
                ),
        ) {
            Icon(
                imageVector = if (chromeVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = if (chromeVisible) "Hide browser chrome" else "Show browser chrome",
                tint = DivColors.Primary,
            )
        }
    }

    if (showResearch) {
        ResearchSheet(
            initialUrl = currentUrl,
            onDismiss = { showResearch = false },
            onGo = { raw ->
                val compiled = DivUrl.compile(raw).display
                currentUrl = compiled
                pageTitle = compiled
                loadToken++
                showResearch = false
            },
        )
    }
    if (showShield) {
        ShieldSheet(
            siteLabel = currentUrl,
            onDismiss = { showShield = false },
        )
    }
}


@Composable
private fun ModeBadge(label: String, color: Color) {
    Text(
        text = label,
        color = DivColors.BgDark,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}