package app.divbrowser.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divbrowser.android.core.url.DivUrl
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.ui.components.UpdateDialog
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Home — clean search + minimal chrome (split lives in Settings)
 */
@Composable
fun HomeScreen(
    onOpenUrl: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPublish: () -> Unit = {},
    onOpenMulti: (Int) -> Unit = {},
) {
    val p = LocalDivPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var serverOk by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var updateInfo by remember { mutableStateOf<DivApi.UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val h = try { DivApi.health() } catch (_: Exception) { null }
        serverOk = DivApi.isHealthy(h)
        val u = try { DivApi.checkUpdate() } catch (_: Exception) { null }
        if (u?.available == true) updateInfo = u
    }

    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    fun submit(raw: String = query) {
        val q = raw.trim()
        if (q.isEmpty()) return
        val compiled = DivUrl.compile(q)
        focusManager.clearFocus()
        keyboard?.hide()
        onOpenUrl(compiled.display)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DivColors.Secondary.copy(alpha = if (p.isDark) 0.14f else 0.07f),
                        p.bg,
                        DivColors.Primary.copy(alpha = if (p.isDark) 0.05f else 0.03f),
                    ),
                ),
            )
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(top = 12.dp, bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(DivColors.Primary, DivColors.Secondary),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "D",
                            color = DivColors.BgDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Text("Div", color = p.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = p.textDim)
                }
            }

            Spacer(modifier = Modifier.weight(0.35f))

            Text(
                "Search",
                color = p.text,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(p.surface.copy(alpha = 0.94f))
                    .border(1.dp, p.border, RoundedCornerShape(28.dp))
                    .padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = p.text, fontSize = 15.sp),
                    cursorBrush = SolidColor(DivColors.Primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { submit() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("url · div:// · search", color = p.textDim, fontSize = 15.sp)
                        }
                        inner()
                    },
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.Close, "Clear", tint = p.textDim, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(
                    onClick = { submit() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(DivColors.Primary, DivColors.Secondary)),
                        ),
                ) {
                    Icon(Icons.Rounded.ArrowForward, "Go", tint = DivColors.BgDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SoftChip("Publish") { onOpenPublish() }
                SoftChip("div://") {
                    if (query.isBlank()) query = "div://" else submit()
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                when (serverOk) {
                    true -> "Cloud online"
                    false -> "Cloud offline"
                    null -> "Checking…"
                },
                color = p.textDim.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            updateInfo?.let { info ->
                UpdateDialog(info = info, onDismiss = { updateInfo = null })
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SoftChip(label: String, onClick: () -> Unit) {
    val p = LocalDivPalette.current
    Text(
        label,
        color = p.text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(p.surface)
            .border(1.dp, p.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}
