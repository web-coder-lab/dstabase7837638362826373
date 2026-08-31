package app.divbrowser.android.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divbrowser.android.BuildConfig
import app.divbrowser.android.core.url.DivLegal
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.data.prefs.DivPrefs
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette
import kotlinx.coroutines.launch

/**
 * Settings — real persisted toggles + split control + legal
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPublish: () -> Unit = {},
    onOpenDivs: (String) -> Unit = {},
    onOpenMulti: (Int) -> Unit = {},
) {
    val p = LocalDivPalette.current
    val context = LocalContext.current
    val prefs = remember { DivPrefs(context) }
    val dark by prefs.darkMode.collectAsState(initial = true)
    val jsEnabled by prefs.jsEnabled.collectAsState(initial = true)
    val blockPopups by prefs.blockPopups.collectAsState(initial = true)
    val clearOnExit by prefs.clearOnExit.collectAsState(initial = false)
    val defaultPanes by prefs.defaultPanes.collectAsState(initial = 1)
    val scope = rememberCoroutineScope()

    var domain by remember { mutableStateOf<String?>(null) }
    var hasKey by remember { mutableStateOf(false) }
    var cloud by remember { mutableStateOf("Checking…") }

    LaunchedEffect(Unit) {
        domain = prefs.savedDomain()
        hasKey = !prefs.savedApiKey().isNullOrBlank()
        val h = try { DivApi.health() } catch (_: Exception) { null }
        cloud = if (DivApi.isHealthy(h)) "Online" else "Offline"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg)
            .statusBarsPadding()
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
            Text("Settings", color = p.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        SectionLabel("Appearance")
        SettingsCard {
            ToggleRow(
                title = "Dark mode",
                subtitle = "Saved on this device",
                checked = dark,
                onChange = { v -> scope.launch { prefs.setDarkMode(v) } },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Browser")
        SettingsCard {
            ToggleRow(
                title = "JavaScript",
                subtitle = "Applied to pages you open",
                checked = jsEnabled,
                onChange = { v -> scope.launch { prefs.setJsEnabled(v) } },
            )
            ToggleRow(
                title = "Block pop-ups",
                subtitle = "Blocks window.open targets",
                checked = blockPopups,
                onChange = { v -> scope.launch { prefs.setBlockPopups(v) } },
            )
            ToggleRow(
                title = "Clear session on exit",
                subtitle = "Drop multi-pane URLs when app closes",
                checked = clearOnExit,
                onChange = { v -> scope.launch { prefs.setClearOnExit(v) } },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Split screen")
        SettingsCard {
            Text(
                "Default layout",
                color = p.textDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                listOf(1, 2, 3, 4).forEach { n ->
                    val selected = defaultPanes == n
                    Text(
                        text = if (n == 1) "1" else "$n",
                        color = if (selected) DivColors.BgDark else p.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) DivColors.Primary else p.surface2)
                            .clickable {
                                scope.launch { prefs.setDefaultPanes(n) }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
            NavRow(
                title = "Open split now",
                subtitle = when (defaultPanes) {
                    1 -> "Single browser"
                    else -> "$defaultPanes panes"
                },
            ) {
                if (defaultPanes <= 1) onBack() else onOpenMulti(defaultPanes)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Publish & cloud")
        SettingsCard {
            NavRow(
                title = "Publish domain",
                subtitle = if (domain.isNullOrBlank()) "Claim div:// and get API key"
                else "div://$domain · ${if (hasKey) "key saved" else "no key"}",
            ) { onPublish() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cloud status", color = p.text, fontWeight = FontWeight.SemiBold)
                    Text(
                        cloud,
                        color = if (cloud == "Online") DivColors.Success else DivColors.Danger,
                        fontSize = 12.sp,
                    )
                }
            }
            if (hasKey || !domain.isNullOrBlank()) {
                NavRow(
                    title = "Clear publish data",
                    subtitle = "Remove domain + API key from this phone",
                ) {
                    scope.launch {
                        prefs.clearPublish()
                        domain = null
                        hasKey = false
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Legal")
        SettingsCard {
            NavRow("Privacy policy", "Opens in browser") { onOpenDivs(DivLegal.Privacy) }
            NavRow("Terms of service", "Opens in browser") { onOpenDivs(DivLegal.Terms) }
            NavRow("About", "Opens in browser") { onOpenDivs(DivLegal.About) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("About")
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Div Browser", color = p.text, fontWeight = FontWeight.SemiBold)
                Text(
                    "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    color = p.textDim,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val p = LocalDivPalette.current
    Text(
        text,
        color = p.textDim,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val p = LocalDivPalette.current
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(DivDimens.RadiusMd))
            .background(p.surface),
    ) { content() }
}

@Composable
private fun NavRow(title: String, subtitle: String, onClick: () -> Unit) {
    val p = LocalDivPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = p.text, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = p.textDim, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = p.textDim)
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val p = LocalDivPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = p.text, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = p.textDim, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = DivColors.Primary,
                checkedThumbColor = DivColors.BgDark,
            ),
        )
    }
}
