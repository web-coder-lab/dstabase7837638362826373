package app.divbrowser.android.ui.pane

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 36 — pane chrome: focus ring + header + content
 */
@Composable
fun PaneFrame(
    title: String,
    mode: PaneMode,
    active: Boolean,
    onTitleClick: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
    online: Boolean = true,
    content: @Composable () -> Unit,
) {
    val p = LocalDivPalette.current
    val shape = RoundedCornerShape(DivDimens.RadiusSm)
    val borderColor = when {
        active && mode == PaneMode.DIV -> p.divOrb.copy(alpha = 0.85f)
        active -> p.webOrb.copy(alpha = 0.85f)
        else -> p.border
    }
    val borderWidth = if (active) DivDimens.FocusStroke else DivDimens.SplitStroke

    Column(
        modifier = modifier
            .fillMaxSize()
            .alpha(if (active) 1f else 0.88f)
            .clip(shape)
            .border(borderWidth, borderColor, shape),
    ) {
        PaneHeader(
            title = title,
            mode = mode,
            active = active,
            online = online,
            onTitleClick = onTitleClick,
            onClose = onClose,
        )
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
