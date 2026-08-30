package app.divbrowser.android.ui.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 35 — WEB cyan / DIV violet status orb
 */
@Composable
fun ModeOrb(
    mode: PaneMode,
    modifier: Modifier = Modifier,
    online: Boolean = true,
) {
    val p = LocalDivPalette.current
    val color = when (mode) {
        PaneMode.WEB -> p.webOrb
        PaneMode.DIV -> p.divOrb
    }
    Box(
        modifier = modifier
            .size(DivDimens.OrbSize)
            .clip(CircleShape)
            .background(if (online) color else color.copy(alpha = 0.35f)),
    )
}
