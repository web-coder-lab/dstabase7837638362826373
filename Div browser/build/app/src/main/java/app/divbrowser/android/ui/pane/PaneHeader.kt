package app.divbrowser.android.ui.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.DivUrlStyle
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 36 — compact pane header (orb · title · close)
 */
@Composable
fun PaneHeader(
    title: String,
    mode: PaneMode,
    active: Boolean,
    onTitleClick: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
    online: Boolean = true,
) {
    val p = LocalDivPalette.current
    val titleColor = if (active) p.text else p.textDim

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DivDimens.PaneHeaderH)
            .background(p.surface.copy(alpha = if (active) 0.95f else 0.75f))
            .padding(horizontal = DivDimens.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        ModeOrb(mode = mode, online = online)
        Spacer(modifier = Modifier.width(DivDimens.Space2))
        Text(
            text = title.ifBlank {
                when (mode) {
                    PaneMode.WEB -> "Web"
                    PaneMode.DIV -> "Div"
                }
            },
            style = DivUrlStyle.copy(color = titleColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTitleClick),
        )
        if (onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(DivDimens.IconMd + DivDimens.Space1),
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close pane",
                    tint = p.textDim,
                    modifier = Modifier.size(DivDimens.IconSm),
                )
            }
        }
    }
}
