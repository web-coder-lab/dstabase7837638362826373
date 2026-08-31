package app.divbrowser.android.ui.pane

/**
 * Phase 35 — pane mode classification
 */
enum class PaneMode {
    WEB,
    DIV,
}

fun paneModeForUrl(url: String): PaneMode {
    val u = url.trim().lowercase()
    return when {
        u.startsWith("div://") || u.startsWith("divs://") -> PaneMode.DIV
        else -> PaneMode.WEB
    }
}
