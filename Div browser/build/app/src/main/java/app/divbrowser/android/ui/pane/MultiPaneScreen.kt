package app.divbrowser.android.ui.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import app.divbrowser.android.core.url.DivUrl
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.data.prefs.DivPrefs
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MultiPaneScreen(
    initialCount: Int = 2,
    seedUrls: List<String> = listOf(
        "https://example.com",
        "https://wikipedia.org",
        "https://news.ycombinator.com",
        "divs://browser.com/privacy_policy",
    ),
    onHome: () -> Unit,
) {
    val p = LocalDivPalette.current
    val context = LocalContext.current
    val prefs = remember { DivPrefs(context) }

    var panes by remember {
        mutableStateOf(
            List(initialCount.coerceIn(1, 4)) { i ->
                PaneState().withUrl(seedUrls.getOrElse(i) { "https://example.com" })
            },
        )
    }
    var active by remember { mutableIntStateOf(0) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var focusMode by remember { mutableStateOf(false) }
    var sessionLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val saved = prefs.loadPaneSession()
        if (!saved.isNullOrEmpty()) {
            panes = saved.map { PaneState().withUrl(it) }
        }
        sessionLoaded = true
    }
    LaunchedEffect(panes.map { it.url }) {
        if (sessionLoaded) {
            prefs.savePaneSession(panes.map { it.url })
        }
    }
    LaunchedEffect(panes.map { it.url to it.mode }) {
        while (isActive) {
            val key = try { prefs.savedApiKey() } catch (_: Exception) { null }
            panes = panes.map { pane ->
                if (pane.mode != PaneMode.DIV) return@map pane.copy(online = true)
                val domain = DivUrl.compile(pane.url).domain
                if (domain.isNullOrBlank()) return@map pane.copy(online = false)
                val st = try {
                    DivApi.tunnelStatus(domain, key)
                } catch (_: Exception) {
                    null
                }
                val online = st?.optBoolean("online") == true ||
                    st?.optBoolean("ok") == true ||
                    st?.optString("status") == "online"
                pane.copy(online = online)
            }
            delay(12_000)
        }
    }

    fun setCount(n: Int) {
        val target = n.coerceIn(1, 4)
        panes = when {
            target > panes.size -> panes + List(target - panes.size) { i ->
                val idx = panes.size + i
                PaneState().withUrl(seedUrls.getOrElse(idx) { "https://example.com" })
            }
            target < panes.size -> panes.take(target)
            else -> panes
        }
        if (active >= panes.size) active = panes.lastIndex.coerceAtLeast(0)
    }

    fun updateTitle(index: Int, title: String) {
        panes = panes.mapIndexed { i, s -> if (i == index) s.copy(title = title.take(48)) else s }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DivDimens.TopBarH)
                .background(p.surface)
                .padding(horizontal = DivDimens.Space1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Rounded.Home, contentDescription = "Home", tint = p.text)
            }
            Text(
                if (focusMode) "Focus" else "${panes.size} pane${if (panes.size > 1) "s" else ""}",
                color = p.textDim,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    if (panes.size >= 2) {
                        val i = active.coerceIn(0, panes.lastIndex)
                        val j = if (i == 0) 1 else 0
                        val list = panes.toMutableList()
                        val tmp = list[i]
                        list[i] = list[j]
                        list[j] = tmp
                        panes = list
                        active = j
                    }
                },
                enabled = panes.size >= 2 && !focusMode,
            ) {
                Icon(Icons.Rounded.SwapHoriz, contentDescription = "Swap", tint = p.primary)
            }
            IconButton(onClick = { focusMode = !focusMode }, enabled = panes.isNotEmpty()) {
                Icon(
                    if (focusMode) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                    contentDescription = "Focus mode",
                    tint = if (focusMode) p.primary else p.textDim,
                )
            }
            IconButton(onClick = { setCount(panes.size - 1) }, enabled = panes.size > 1 && !focusMode) {
                Icon(Icons.Rounded.Remove, contentDescription = "Fewer", tint = p.textDim)
            }
            IconButton(onClick = { setCount(panes.size + 1) }, enabled = panes.size < 4 && !focusMode) {
                Icon(Icons.Rounded.Add, contentDescription = "More", tint = p.primary)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(DivDimens.Space1),
        ) {
            if (focusMode) {
                val idx = active.coerceIn(0, panes.lastIndex)
                PaneCell(
                    state = panes[idx],
                    active = true,
                    onFocus = {},
                    onTitleClick = { editIndex = idx },
                    onClose = { focusMode = false },
                    onTitle = { updateTitle(idx, it) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (panes.size == 1) {
                PaneCell(
                    state = panes[0],
                    active = true,
                    onFocus = { active = 0 },
                    onTitleClick = { editIndex = 0 },
                    onClose = null,
                    onTitle = { updateTitle(0, it) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (panes.size == 2) {
                Row(modifier = Modifier.fillMaxSize()) {
                    PaneCell(
                        state = panes[0], active = active == 0, onFocus = { active = 0 },
                        onTitleClick = { editIndex = 0 }, onClose = { setCount(1) },
                        onTitle = { updateTitle(0, it) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    Spacer(modifier = Modifier.width(DivDimens.Space1))
                    PaneCell(
                        state = panes[1], active = active == 1, onFocus = { active = 1 },
                        onTitleClick = { editIndex = 1 }, onClose = { setCount(1) },
                        onTitle = { updateTitle(1, it) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            } else if (panes.size == 3) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        PaneCell(
                            state = panes[0], active = active == 0, onFocus = { active = 0 },
                            onTitleClick = { editIndex = 0 }, onClose = { setCount(2) },
                            onTitle = { updateTitle(0, it) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                        Spacer(modifier = Modifier.width(DivDimens.Space1))
                        PaneCell(
                            state = panes[1], active = active == 1, onFocus = { active = 1 },
                            onTitleClick = { editIndex = 1 }, onClose = { setCount(2) },
                            onTitle = { updateTitle(1, it) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    Spacer(modifier = Modifier.height(DivDimens.Space1))
                    PaneCell(
                        state = panes[2], active = active == 2, onFocus = { active = 2 },
                        onTitleClick = { editIndex = 2 }, onClose = { setCount(2) },
                        onTitle = { updateTitle(2, it) },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        PaneCell(
                            state = panes[0], active = active == 0, onFocus = { active = 0 },
                            onTitleClick = { editIndex = 0 }, onClose = { setCount(3) },
                            onTitle = { updateTitle(0, it) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                        Spacer(modifier = Modifier.width(DivDimens.Space1))
                        PaneCell(
                            state = panes[1], active = active == 1, onFocus = { active = 1 },
                            onTitleClick = { editIndex = 1 }, onClose = { setCount(3) },
                            onTitle = { updateTitle(1, it) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    Spacer(modifier = Modifier.height(DivDimens.Space1))
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        PaneCell(
                            state = panes[2], active = active == 2, onFocus = { active = 2 },
                            onTitleClick = { editIndex = 2 }, onClose = { setCount(3) },
                            onTitle = { updateTitle(2, it) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                        Spacer(modifier = Modifier.width(DivDimens.Space1))
                        PaneCell(
                            state = panes[3], active = active == 3, onFocus = { active = 3 },
                            onTitleClick = { editIndex = 3 }, onClose = { setCount(3) },
                            onTitle = { updateTitle(3, it) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }

    editIndex?.let { idx ->
        if (idx in panes.indices) {
            PaneUrlDialog(
                initial = panes[idx].url,
                onDismiss = { editIndex = null },
                onGo = { u ->
                    panes = panes.mapIndexed { i, s -> if (i == idx) s.withUrl(u) else s }
                    active = idx
                    editIndex = null
                },
            )
        }
    }
}

@Composable
private fun PaneCell(
    state: PaneState,
    active: Boolean,
    onFocus: () -> Unit,
    onTitleClick: () -> Unit,
    onClose: (() -> Unit)?,
    onTitle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onFocus)) {
        PaneFrame(
            title = state.title.ifBlank { state.url },
            mode = state.mode,
            active = active,
            online = state.online,
            onTitleClick = onTitleClick,
            onClose = onClose,
        ) {
            PaneWebSlot(url = state.url, onTitle = onTitle)
        }
    }
}
