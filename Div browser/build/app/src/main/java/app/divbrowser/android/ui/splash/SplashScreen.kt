package app.divbrowser.android.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.divbrowser.android.data.api.DivApi
import app.divbrowser.android.data.prefs.DivPrefs
import app.divbrowser.android.ui.theme.DivColors
import app.divbrowser.android.ui.theme.LocalDivPalette
import kotlinx.coroutines.delay

/**
 * Phase 8 — Splash UI (void gradient · mark · soft entrance)
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val p = LocalDivPalette.current
    val scale = remember { Animatable(0.82f) }
    val alpha = remember { Animatable(0f) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Phase 53 — soft device register (ignore failures)
        try {
            val id = DivPrefs(context).deviceId()
            DivApi.registerDevice(id)
        } catch (_: Exception) { }
        alpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        delay(850)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        p.bg,
                        DivColors.SurfaceDark,
                        p.bg,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value),
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
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
                    fontSize = 34.sp,
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                "Div Browser",
                color = p.text,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Web + Div",
                color = p.textDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
