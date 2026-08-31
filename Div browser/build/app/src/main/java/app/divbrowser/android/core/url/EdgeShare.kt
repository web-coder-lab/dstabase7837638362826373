package app.divbrowser.android.core.url

import app.divbrowser.android.BuildConfig
import app.divbrowser.android.data.api.DivApi

/**
 * Phase 7 — public edge share links (open.divbrowser.app → app / install).
 */
object EdgeShare {

    private val base: String
        get() = BuildConfig.EDGE_BASE.trimEnd('/').ifBlank { DivApi.edgeBase }

    /** https://open.divbrowser.app/d/{slug} */
    fun linkForDomain(domain: String): String {
        val slug = domain.trim()
            .removePrefix("div://")
            .removePrefix("divs://")
            .substringBefore('/')
            .lowercase()
        return "$base/d/$slug"
    }

    fun linkFor(target: DivUrl.Target): String {
        val slug = when {
            !target.domain.isNullOrBlank() -> target.domain
            target.display.startsWith("div://") || target.display.startsWith("divs://") ->
                target.display.substringAfter("://").substringBefore('/')
            else -> target.httpUrl?.let { hostOf(it) } ?: "app"
        }
        return linkForDomain(slug)
    }

    fun shareText(target: DivUrl.Target): String {
        val link = linkFor(target)
        return buildString {
            append("Open in Div Browser\n")
            append(target.display)
            append("\n")
            append(link)
        }
    }

    private fun hostOf(url: String): String? = try {
        java.net.URI(url).host?.lowercase()
    } catch (_: Exception) {
        null
    }
}
