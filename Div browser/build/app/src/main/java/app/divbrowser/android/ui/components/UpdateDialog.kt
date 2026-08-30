package app.divbrowser.android.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.DivDimens
import app.divbrowser.android.ui.theme.LocalDivPalette

@Composable
fun UpdateDialog(
    info: DivApi.UpdateInfo,
    onDismiss: () -> Unit,
) {
    val p = LocalDivPalette.current
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surface, RoundedCornerShape(DivDimens.RadiusMd))
                .padding(20.dp),
        ) {
            Text("Update available", color = p.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                info.versionName.ifBlank { "New version" },
                color = p.primary,
                fontSize = 14.sp,
            )
            if (info.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(info.notes, color = p.textDim, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)))
                    } catch (_: Exception) {
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(DivDimens.ButtonH),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DivColors.Primary,
                    contentColor = DivColors.BgDark,
                ),
            ) {
                Text("Download", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Later", color = p.textDim)
            }
        }
    }
}
