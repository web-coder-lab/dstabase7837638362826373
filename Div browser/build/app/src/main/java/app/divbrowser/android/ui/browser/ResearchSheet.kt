package app.divbrowser.android.ui.browser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 23 — Research sheet: edit URL, copy, go
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchSheet(
    initialUrl: String,
    onDismiss: () -> Unit,
    onGo: (String) -> Unit,
) {
    val p = LocalDivPalette.current
    val context = LocalContext.current
    var text by remember { mutableStateOf(initialUrl) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
        ) {
            Text("Research", color = p.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Edit or copy the current address", color = p.textDim, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(14.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = TextStyle(color = p.text, fontSize = 14.sp),
                cursorBrush = SolidColor(DivColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surface2, RoundedCornerShape(DivDimens.RadiusSm))
                    .padding(14.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
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
            TextButton(
                onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("url", text.trim()))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Copy address", color = p.primary)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close", color = p.textDim)
            }
        }
    }
}
