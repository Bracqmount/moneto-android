package com.fay.currencywidget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonetoDarkColors = darkColorScheme(
    primary           = Color(0xFFF59E0B),
    onPrimary         = Color(0xFF18181B),
    primaryContainer  = Color(0xFF1A1F2E),
    onPrimaryContainer= Color(0xFFF59E0B),
    secondary         = Color(0xFFF59E0B),
    onSecondary       = Color(0xFF18181B),
    background        = Color(0xFF0F1117),
    onBackground      = Color(0xFFFFFFFF),
    surface           = Color(0xFF1A1F2E),
    onSurface         = Color(0xFFFFFFFF),
    surfaceVariant    = Color(0xFF13161F),
    onSurfaceVariant  = Color(0xFF9CA3AF),
    outline           = Color(0xFF1F2937),
    error             = Color(0xFFEF4444),
    onError           = Color(0xFFFFFFFF),
    tertiary          = Color(0xFF10B981),
    onTertiary        = Color(0xFF18181B),
)

@Composable
fun CurrencyWidgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonetoDarkColors,
        typography = Typography,
        content = content
    )
}