package app.divbrowser.android.ui.pane

import java.util.UUID

/**
 * Phase 37 — per-pane state
 */
data class PaneState(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://example.com",
    val title: String = "",
    val mode: PaneMode = PaneMode.WEB,
    val online: Boolean = true,
)

fun PaneState.withUrl(newUrl: String): PaneState {
    val mode = paneModeForUrl(newUrl)
    val title = newUrl
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("divs://")
        .removePrefix("div://")
        .take(48)
    return copy(url = newUrl, mode = mode, title = title)
}
