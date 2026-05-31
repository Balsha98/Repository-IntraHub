package com.bazovic.balsa.intrahub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─── SECTION: Color Scheme ─── //
private val IntraHubColorScheme = lightColorScheme(
    primary = OrangeRIT,
    onPrimary = Surface,
    primaryContainer = OrangeTint,
    onPrimaryContainer = OrangeDark,
    secondary = Ink3,
    onSecondary = Surface,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    outline = Line,
    error = Loss,
    onError = Surface,
)//IntraHubColorScheme

// ─── SECTION: Theme ─── //
@Composable
fun IntraHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IntraHubColorScheme,
        typography = Typography,
        content = content,
    )//MaterialTheme
}//IntraHubTheme
