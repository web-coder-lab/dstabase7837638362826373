package app.divbrowser.android.ui.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 39 — URL edit dialog for a single pane
 */
@Composable
fun PaneUrlDialog(
    initial: String,
    onDismiss: () -> Unit,
    onGo: (String) -> Unit,
) {
    val p = LocalDivPalette.current
    var text by remember { mutableStateOf(initial) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surface, RoundedCornerShape(DivDimens.RadiusMd))
                .padding(DivDimens.Space4),
        ) {
            Text("Open URL", color = p.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(DivDimens.Space3))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = TextStyle(color = p.text, fontSize = 14.sp),
                cursorBrush = SolidColor(p.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surface2, RoundedCornerShape(DivDimens.RadiusSm))
                    .padding(DivDimens.Space3),
            )
            Spacer(modifier = Modifier.height(DivDimens.Space4))
            Button(
                onClick = { onGo(text.trim()) },
                modifier = Modifier.fillMaxWidth().height(DivDimens.ButtonH),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DivColors.Primary,
                    contentColor = DivColors.BgDark,
                ),
                shape = RoundedCornerShape(DivDimens.RadiusMd),
            ) {
                Text("Go", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = p.textDim)
            }
        }
    }
}
