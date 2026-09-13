package com.lqanh.todoandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF004AC6)
val PrimaryContainer = Color(0xFF2563EB)
val OnPrimary = Color(0xFFFFFFFF)
val Surface = Color(0xFFF8F9FF)
val OnSurface = Color(0xFF0B1C30)
val OnSurfaceVariant = Color(0xFF434655)
val SurfaceContainerLow = Color(0xFFEFF4FF)
val SurfaceContainer = Color(0xFFE5EEFF)
val SurfaceContainerHigh = Color(0xFFDCE9FF)
val SurfaceContainerHighest = Color(0xFFD3E4FE)
val PrimaryFixed = Color(0xFFDBE1FF)
val Outline = Color(0xFF737686)
val OutlineVariant = Color(0xFFC3C6D7)
val Error = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)
val Tertiary = Color(0xFF006242)
val TertiaryContainer = Color(0xFF007D55)
val TertiaryFixed = Color(0xFF6FFBBE)
val Secondary = Color(0xFF6B38D4)
val SecondaryContainer = Color(0xFF8455EF)
val SecondaryFixed = Color(0xFFE9DDFF)
val OnSecondaryFixed = Color(0xFF23005C)
val Amber = Color(0xFFF59E0B)
val InverseSurface = Color(0xFF213145)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Color(0xFFEEEFFF),
    secondary = Secondary,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnPrimary,
    tertiaryContainer = TertiaryContainer,
    error = Error,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

@Composable
fun NhipSongTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
