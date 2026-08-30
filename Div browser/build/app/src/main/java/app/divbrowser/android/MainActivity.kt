package app.divbrowser.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.divbrowser.android.core.nav.Routes
import app.divbrowser.android.core.url.DivUrl
import app.divbrowser.android.data.prefs.DivPrefs
import app.divbrowser.android.ui.browser.BrowserScreen
import app.divbrowser.android.ui.home.HomeScreen
import app.divbrowser.android.ui.pane.DualPaneScreen
import app.divbrowser.android.ui.pane.MultiPaneScreen
import app.divbrowser.android.ui.publish.PublishScreen
import app.divbrowser.android.ui.settings.SettingsScreen
import app.divbrowser.android.ui.splash.SplashScreen
import app.divbrowser.android.ui.theme.DivTheme

/**
 * Phase 28 — Deep links: div:// · divs:// · open.divbrowser.app/d/{slug}
 */
class MainActivity : ComponentActivity() {

    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLink = extractDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            val prefs = remember { DivPrefs(this@MainActivity) }
            val dark by prefs.darkMode.collectAsState(initial = true)
            DivTheme(darkTheme = dark) {
                DivNav(
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink = extractDeepLink(intent)
    }

    private fun extractDeepLink(intent: Intent?): String? {
        val data = intent?.data ?: return null
        val scheme = data.scheme?.lowercase().orEmpty()
        val host = data.host.orEmpty()
        val path = data.path.orEmpty()
        val raw = data.toString()

        return when {
            scheme == "div" || scheme == "divs" -> DivUrl.compile(raw).display
            // https://open.divbrowser.app/d/shop.ahmed
            (scheme == "https" || scheme == "http") &&
                host.contains("divbrowser") &&
                path.startsWith("/d/") -> {
                val slug = path.removePrefix("/d/").trim('/')
                if (slug.isBlank()) null else DivUrl.compile("div://$slug").display
            }
            else -> null
        }
    }
}

@Composable
private fun DivNav(
    pendingDeepLink: String?,
    onDeepLinkConsumed: () -> Unit,
) {
    val nav = rememberNavController()
    var pastSplash by remember { mutableStateOf(false) }

    // Apply deep link only after splash so user isn't stuck on splash stack
    LaunchedEffect(pendingDeepLink, pastSplash) {
        val link = pendingDeepLink ?: return@LaunchedEffect
        if (!pastSplash) return@LaunchedEffect
        nav.navigate(Routes.browser(link)) {
            launchSingleTop = true
        }
        onDeepLinkConsumed()
    }

    NavHost(
        navController = nav,
        startDestination = Routes.Splash,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onDone = {
                    pastSplash = true
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                onOpenUrl = { url -> nav.navigate(Routes.browser(url)) },
                onOpenSettings = { nav.navigate(Routes.Settings) },
                onOpenPublish = { nav.navigate(Routes.Publish) },
                onOpenMulti = { c -> nav.navigate(Routes.multi(c)) },
            )
        }
        composable(
            route = "browser?url={url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
            ),
        ) { entry ->
            val url = entry.arguments?.getString("url").orEmpty()
            BrowserScreen(
                initialUrl = url,
                onHome = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
                onSettings = { nav.navigate(Routes.Settings) },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onPublish = { nav.navigate(Routes.Publish) },
                onOpenDivs = { divs -> nav.navigate(Routes.browser(divs)) },
            )
        }
        composable(Routes.Dual) {
            DualPaneScreen(
                onHome = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Publish) {
            PublishScreen(
                onBack = { nav.popBackStack() },
                onOpenDiv = { div -> nav.navigate(Routes.browser(div)) },
            )
        }
        composable(
            route = "multi?count={count}",
            arguments = listOf(
                navArgument("count") {
                    type = NavType.IntType
                    defaultValue = 2
                },
            ),
        ) { entry ->
            val count = entry.arguments?.getInt("count") ?: 2
            MultiPaneScreen(
                initialCount = count,
                onHome = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
            )
        }
    }
}
