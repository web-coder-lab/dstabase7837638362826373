package app.divbrowser.android.core.url

/**
 * Phase 6 — compile user input → div:// | divs:// | http(s) load plan.
 * Network is DivApi / WebView; this only normalizes what we open.
 */
object DivUrl {

    data class Target(
        /** Address bar / navigation display */
        val display: String,
        /** true = load local http only (emulator/device LAN) */
        val localOnly: Boolean,
        /** true = official divs:// system surface */
        val isDivs: Boolean,
        /** WebView http(s) when local or public web */
        val httpUrl: String? = null,
        /** domain slug for tunnel */
        val domain: String? = null,
        /** path starting with / */
        val path: String = "/",
    )

    fun compile(raw: String): Target {
        var input = raw.trim()
        if (input.isEmpty()) {
            return Target(display = "div://", localOnly = false, isDivs = false)
        }

        val lower = input.lowercase()
        when {
            lower == "privacy" || (lower.endsWith("privacy_policy") && !lower.startsWith("http")) ->
                input = "divs://browser.com/privacy_policy"
            lower == "terms" || lower == "term" ||
                (lower.endsWith("term_policy") && !lower.startsWith("http")) ->
                input = "divs://browser.com/term_policy"
            lower == "about" ->
                input = "divs://browser.com/about"
        }

        if (input.startsWith("divs://", ignoreCase = true)) {
            return fromDivs(input)
        }
        if (input.startsWith("div://", ignoreCase = true)) {
            return fromDiv(input)
        }

        // localhost / private IP → auto div:// + local http
        if (looksLocal(input)) {
            return fromDiv(toDivLocal(input))
        }

        // explicit http(s)
        if (input.startsWith("http://", ignoreCase = true) ||
            input.startsWith("https://", ignoreCase = true)
        ) {
            return Target(
                display = input,
                localOnly = false,
                isDivs = false,
                httpUrl = input,
                domain = hostOf(input),
                path = pathOf(input),
            )
        }

        // bare domain / search-like → https
        if (looksLikeDomain(input)) {
            val https = "https://${input.removePrefix("//")}"
            return Target(
                display = https,
                localOnly = false,
                isDivs = false,
                httpUrl = https,
                domain = hostOf(https),
                path = pathOf(https),
            )
        }

        // fallback: treat as div:// slug
        return fromDiv("div://${input.removePrefix("//")}")
    }

    private fun fromDivs(divs: String): Target {
        val body = divs.removePrefix("divs://").removePrefix("DIVS://").trim()
        if (body.isEmpty()) {
            return Target(
                display = "divs://browser.com/",
                localOnly = false,
                isDivs = true,
                domain = "browser.com",
                path = "/",
            )
        }
        val slash = body.indexOf('/')
        val host = (if (slash < 0) body else body.substring(0, slash)).lowercase()
        var path = if (slash < 0) "/" else body.substring(slash)
        if (!path.startsWith("/")) path = "/$path"
        when (path.lowercase()) {
            "/privacy", "/privacy-policy" -> path = "/privacy_policy"
            "/terms", "/tos", "/term-policy", "/term" -> path = "/term_policy"
        }
        val display = "divs://$host${if (path == "/") "" else path}"
        return Target(
            display = display,
            localOnly = false,
            isDivs = true,
            domain = host,
            path = path,
        )
    }

    private fun fromDiv(div: String): Target {
        val body = div.removePrefix("div://").removePrefix("DIV://").trim()
        if (body.isEmpty()) {
            return Target(display = "div://", localOnly = false, isDivs = false)
        }
        val slash = body.indexOf('/')
        val hostPort = if (slash < 0) body else body.substring(0, slash)
        val path = if (slash < 0) "/" else body.substring(slash).ifEmpty { "/" }
        val host = hostPort.substringBefore(':').lowercase()
        val port = hostPort.substringAfter(':', missingDelimiterValue = "")
        val display = "div://$hostPort${if (path == "/") "" else path}"

        if (isLocalHost(host)) {
            val schemePort = port.ifEmpty { "80" }
            val http = buildString {
                append("http://")
                append(
                    when (host) {
                        "localhost", "127.0.0.1" -> "10.0.2.2" // emulator → host machine
                        else -> host
                    },
                )
                if (schemePort != "80") append(":$schemePort")
                append(if (path.startsWith("/")) path else "/$path")
            }
            return Target(
                display = display,
                localOnly = true,
                isDivs = false,
                httpUrl = http,
                domain = host,
                path = if (path.startsWith("/")) path else "/$path",
            )
        }

        return Target(
            display = display,
            localOnly = false,
            isDivs = false,
            domain = host,
            path = if (path.startsWith("/")) path else "/$path",
        )
    }

    private fun looksLocal(input: String): Boolean {
        val h = input.substringBefore('/').substringBefore(':').lowercase()
        return isLocalHost(h) || input.lowercase().startsWith("localhost")
    }

    private fun isLocalHost(host: String): Boolean {
        return host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2" ||
            host.startsWith("192.168.") || host.startsWith("10.") ||
            Regex("""^172\.(1[6-9]|2\d|3[0-1])\.""").containsMatchIn(host)
    }

    private fun toDivLocal(input: String): String {
        val cleaned = input.removePrefix("http://").removePrefix("https://")
        return "div://$cleaned"
    }

    private fun looksLikeDomain(input: String): Boolean {
        if (input.contains(' ')) return false
        if (input.startsWith(".")) return false
        return input.contains('.') || input.matches(Regex("""^[a-zA-Z0-9-]+(:\d+)?(/.*)?$"""))
    }

    private fun hostOf(url: String): String? = try {
        val u = java.net.URI(url)
        u.host?.lowercase()
    } catch (_: Exception) {
        null
    }

    private fun pathOf(url: String): String = try {
        val u = java.net.URI(url)
        val p = u.path
        if (p.isNullOrBlank()) "/" else p
    } catch (_: Exception) {
        "/"
    }
}
