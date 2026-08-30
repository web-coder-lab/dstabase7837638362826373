package app.divbrowser.android.core.url

/**
 * Official divs:// surfaces + offline HTML fallback (works even if cloud is down).
 */
object DivLegal {
    const val Privacy = "divs://browser.com/privacy_policy"
    const val Terms = "divs://browser.com/term_policy"
    const val About = "divs://browser.com/about"

    fun isLegal(displayOrUrl: String): Boolean {
        val u = displayOrUrl.trim().lowercase()
        return u.startsWith("divs://browser.com/privacy") ||
            u.startsWith("divs://browser.com/term") ||
            u.startsWith("divs://browser.com/about") ||
            u == Privacy || u == Terms || u == About
    }

    fun offlineHtml(path: String): String {
        val p = path.lowercase()
        return when {
            p.contains("privacy") -> PRIVACY_HTML
            p.contains("term") -> TERMS_HTML
            p.contains("about") -> ABOUT_HTML
            else -> """
                <html><body style="background:#070A12;color:#8B95A8;font-family:sans-serif;padding:24px">
                <h3 style="color:#FF5C7A">404</h3><p>Unknown page</p></body></html>
            """.trimIndent()
        }
    }

    private const val PRIVACY_HTML = """
<!DOCTYPE html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<style>body{background:#070A12;color:#E8EEF9;font-family:system-ui,sans-serif;padding:24px;line-height:1.55}h1{color:#3EDCFF}</style></head>
<body><h1>Privacy Policy</h1>
<p>Div Browser stores theme and pane session on this device only.</p>
<p>Publish domain and API key stay on your phone unless you publish. Cloud storage (if used) is limited to domain/key mapping under the Div browser data folder.</p>
<p>We do not sell personal data.</p>
<p style="opacity:.6;font-size:12px">divs://browser.com/privacy_policy</p></body></html>
"""

    private const val TERMS_HTML = """
<!DOCTYPE html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<style>body{background:#070A12;color:#E8EEF9;font-family:system-ui,sans-serif;padding:24px;line-height:1.55}h1{color:#8B5CFF}</style></head>
<body><h1>Terms of Service</h1>
<p>Use Div Browser lawfully. Do not abuse publish, tunnel, or share links. Claimed domains must not impersonate others.</p>
<p style="opacity:.6;font-size:12px">divs://browser.com/term_policy</p></body></html>
"""

    private const val ABOUT_HTML = """
<!DOCTYPE html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<style>body{background:#070A12;color:#E8EEF9;font-family:system-ui,sans-serif;padding:24px;line-height:1.55}h1{color:#3EDCFF}</style></head>
<body><h1>Div Browser</h1>
<p>Web + Div in one shell. Open any site, claim div://, share edge links.</p>
<p style="opacity:.6;font-size:12px">divs://browser.com/about</p></body></html>
"""
}
