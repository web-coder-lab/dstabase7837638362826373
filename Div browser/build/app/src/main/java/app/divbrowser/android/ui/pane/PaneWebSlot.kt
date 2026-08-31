package app.divbrowser.android.ui.pane

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.divbrowser.android.core.url.DivUrl
import app.divbrowser.android.data.api.DivApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Phase 38 — lightweight WebView per pane
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaneWebSlot(
    url: String,
    modifier: Modifier = Modifier,
    onTitle: (String) -> Unit = {},
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

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

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean = false

                    override fun onPageFinished(view: WebView?, u: String?) {
                        onTitle(view?.title?.takeIf { !it.isNullOrBlank() } ?: (u ?: url))
                    }
                }
                webChromeClient = WebChromeClient()
                webView = this
            }
        },
    )

    LaunchedEffect(url) {
        val wv = webView ?: return@LaunchedEffect
        val t = DivUrl.compile(url)
        when {
            t.isDivs -> {
                val host = t.domain ?: "browser.com"
                val path = t.path.ifBlank { "/" }
                val html = DivApi.divsPageHtml(host, path)
                    ?: "<html><body style='background:#070A12;color:#8B95A8;padding:16px'>Unavailable</body></html>"
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
            url.startsWith("http://") || url.startsWith("https://") -> {
                withContext(Dispatchers.Main) { wv.loadUrl(url) }
            }
            else -> {
                val host = t.domain ?: "div"
                val html = """
                    <html><body style="background:#070A12;color:#8B95A8;font-family:sans-serif;padding:16px">
                    <h3 style="color:#B47CFF">div://$host</h3>
                    <p>Div surface — online when publisher tunnel is up.</p>
                    </body></html>
                """.trimIndent()
                withContext(Dispatchers.Main) {
                    wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                }
            }
        }
    }
}
