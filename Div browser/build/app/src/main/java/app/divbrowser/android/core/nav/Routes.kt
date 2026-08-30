package app.divbrowser.android.core.nav

/**
 * Phase 3 — app navigation routes
 */
object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Browser = "browser"
    const val Settings = "settings"
    const val Publish = "publish"
    const val Dual = "dual"
    const val Multi = "multi"

    fun browser(url: String = ""): String {
        if (url.isBlank()) return Browser
        return "browser?url=${android.net.Uri.encode(url)}"
    }

    fun multi(count: Int = 2): String = "multi?count=$count"
}
