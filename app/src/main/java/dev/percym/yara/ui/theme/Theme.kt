package dev.percym.yara.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val YaRAColors = darkColorScheme(
    primary              = GoldPrimary,
    onPrimary            = PurpleDark,
    primaryContainer     = GoldContainer,
    onPrimaryContainer   = GoldLight,
    secondary            = PurpleCardLight,
    onSecondary          = TextPrimary,
    secondaryContainer   = PurpleCard,
    onSecondaryContainer = TextSubtle,
    tertiary             = TextMuted,
    onTertiary           = TextPrimary,
    background           = PurpleDark,
    onBackground         = TextPrimary,
    surface              = PurpleCard,
    onSurface            = TextPrimary,
    surfaceVariant       = PurpleMid,
    onSurfaceVariant     = TextSubtle,
    error                = ErrorRed,
    onError              = TextPrimary,
    outline              = CheckboxBorder,
    outlineVariant       = DividerPurple,
)

@Composable
fun YaRATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YaRAColors,
        typography  = Typography,
        content     = content
    )
}
