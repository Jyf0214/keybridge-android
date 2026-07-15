package com.keybridge.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ===== 颜色定义 =====
val Primary = Color(0xFF1565C0)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD1E4FF)
val OnPrimaryContainer = Color(0xFF001D36)

val Secondary = Color(0xFF535F70)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFD7E3F7)
val OnSecondaryContainer = Color(0xFF101C2B)

val Tertiary = Color(0xFF6B5778)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFF2DAFF)
val OnTertiaryContainer = Color(0xFF251431)

val Surface = Color(0xFFFDFBFF)
val OnSurface = Color(0xFF1A1C1E)
val SurfaceVariant = Color(0xFFE0E3EB)
val OnSurfaceVariant = Color(0xFF44474E)

val Error = Color(0xFFBA1A1A)
val Background = Color(0xFFFDFBFF)

// 暗色主题
val DarkPrimary = Color(0xFF9ECAFF)
val DarkOnPrimary = Color(0xFF003258)
val DarkPrimaryContainer = Color(0xFF004A7C)
val DarkOnPrimaryContainer = Color(0xFFD1E4FF)

val DarkSecondary = Color(0xFFBBC7DB)
val DarkOnSecondary = Color(0xFF253140)
val DarkSecondaryContainer = Color(0xFF3B4858)
val DarkOnSecondaryContainer = Color(0xFFD7E3F7)

val DarkSurface = Color(0xFF1A1C1E)
val DarkOnSurface = Color(0xFFE3E2E6)
val DarkSurfaceVariant = Color(0xFF44474E)
val DarkOnSurfaceVariant = Color(0xFFC4C6D0)

val DarkBackground = Color(0xFF1A1C1E)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    background = Background
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = Error,
    background = DarkBackground
)

@Composable
fun KeyBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12+ 支持动态取色（Material You）
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
