package app.divbrowser.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class DivPalette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val glass: Color,
    val text: Color,
    val textDim: Color,
    val border: Color,
    val primary: Color,
    val secondary: Color,
    val webOrb: Color,
    val divOrb: Color,
    val danger: Color,
    val isDark: Boolean,
)

val LocalDivPalette = staticCompositionLocalOf {
    DivPalette(
        bg = DivColors.BgDark,
        surface = DivColors.SurfaceDark,
        surface2 = DivColors.Surface2Dark,
        glass = DivColors.GlassDark,
        text = DivColors.TextDark,
        textDim = DivColors.TextDimDark,
        border = DivColors.Border,
        primary = DivColors.Primary,
        secondary = DivColors.Secondary,
        webOrb = DivColors.WebOrb,
        divOrb = DivColors.DivOrb,
        danger = DivColors.Danger,
        isDark = true,
    )
}

@Composable
fun DivTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) {
        DivPalette(
            bg = DivColors.BgDark,
            surface = DivColors.SurfaceDark,
            surface2 = DivColors.Surface2Dark,
            glass = DivColors.GlassDark,
            text = DivColors.TextDark,
            textDim = DivColors.TextDimDark,
            border = DivColors.Border,
            primary = DivColors.Primary,
            secondary = DivColors.Secondary,
            webOrb = DivColors.WebOrb,
            divOrb = DivColors.DivOrb,
            danger = DivColors.Danger,
            isDark = true,
        )
    } else {
        DivPalette(
            bg = DivColors.BgLight,
            surface = DivColors.SurfaceLight,
            surface2 = DivColors.Surface2Light,
            glass = DivColors.GlassLight,
            text = DivColors.TextLight,
            textDim = DivColors.TextDimLight,
            border = DivColors.Border,
            primary = DivColors.Primary,
            secondary = DivColors.Secondary,
            webOrb = DivColors.WebOrb,
            divOrb = DivColors.DivOrb,
            danger = DivColors.Danger,
            isDark = false,
        )
    }

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = DivColors.Primary,
            secondary = DivColors.Secondary,
            background = DivColors.BgDark,
            surface = DivColors.SurfaceDark,
            onPrimary = DivColors.BgDark,
            onBackground = DivColors.TextDark,
            onSurface = DivColors.TextDark,
            error = DivColors.Danger,
        )
    } else {
        lightColorScheme(
            primary = DivColors.Primary,
            secondary = DivColors.Secondary,
            background = DivColors.BgLight,
            surface = DivColors.SurfaceLight,
            onPrimary = Color.White,
            onBackground = DivColors.TextLight,
            onSurface = DivColors.TextLight,
            error = DivColors.Danger,
        )
    }

    CompositionLocalProvider(LocalDivPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = DivTypography, content = content)
    }
}
