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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 40 — exactly 2 panes: Left | Right
 */
@Composable
fun DualPaneScreen(
    leftUrl: String = "https://example.com",
    rightUrl: String = "https://wikipedia.org",
    onHome: () -> Unit,
) {
    val p = LocalDivPalette.current
    var left by remember { mutableStateOf(PaneState().withUrl(leftUrl)) }
    var right by remember { mutableStateOf(PaneState().withUrl(rightUrl)) }
    var active by remember { mutableIntStateOf(0) }
    var editLeft by remember { mutableStateOf(false) }
    var editRight by remember { mutableStateOf(false) }

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
                .padding(horizontal = DivDimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Rounded.Home, contentDescription = "Home", tint = p.text)
            }
            Text(
                "2 panes",
                color = p.textDim,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    val a = left
                    left = right
                    right = a
                    active = 1 - active
                },
            ) {
                Icon(Icons.Rounded.SwapHoriz, contentDescription = "Swap", tint = p.primary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(DivDimens.Space1),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { active = 0 },
            ) {
                PaneFrame(
                    title = left.title.ifBlank { left.url },
                    mode = left.mode,
                    active = active == 0,
                    online = left.online,
                    onTitleClick = { editLeft = true },
                    onClose = null,
                ) {
                    PaneWebSlot(
                        url = left.url,
                        onTitle = { t -> left = left.copy(title = t.take(48)) },
                    )
                }
            }
            Spacer(modifier = Modifier.width(DivDimens.Space1))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { active = 1 },
            ) {
                PaneFrame(
                    title = right.title.ifBlank { right.url },
                    mode = right.mode,
                    active = active == 1,
                    online = right.online,
                    onTitleClick = { editRight = true },
                    onClose = null,
                ) {
                    PaneWebSlot(
                        url = right.url,
                        onTitle = { t -> right = right.copy(title = t.take(48)) },
                    )
                }
            }
        }
    }

    if (editLeft) {
        PaneUrlDialog(
            initial = left.url,
            onDismiss = { editLeft = false },
            onGo = {
                left = left.withUrl(it)
                editLeft = false
                active = 0
            },
        )
    }
    if (editRight) {
        PaneUrlDialog(
            initial = right.url,
            onDismiss = { editRight = false },
            onGo = {
                right = right.withUrl(it)
                editRight = false
                active = 1
            },
        )
    }
}
