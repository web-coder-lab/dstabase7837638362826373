package app.divbrowser.android.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.LocalDivPalette

/**
 * Phase 24 — Site Shield: per-site permission toggles (UI state)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldSheet(
    siteLabel: String,
    onDismiss: () -> Unit,
) {
    val p = LocalDivPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val toggles = remember {
        mutableStateMapOf(
            "Camera" to false,
            "Microphone" to false,
            "Location" to false,
            "Notifications" to false,
            "Clipboard" to true,
            "Cookies (session)" to true,
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = p.primary)
                Spacer(modifier = Modifier.padding(6.dp))
                Text("Site Shield", color = p.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                siteLabel,
                color = p.textDim,
                fontSize = 13.sp,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Permissions for this surface",
                color = p.textDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            toggles.keys.sorted().forEach { key ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(p.surface2, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(key, color = p.text, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(
                        checked = toggles[key] == true,
                        onCheckedChange = { toggles[key] = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = DivColors.Primary,
                            checkedThumbColor = DivColors.BgDark,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Done", color = p.primary)
            }
        }
    }
}
