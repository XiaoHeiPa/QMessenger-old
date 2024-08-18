package org.cubewhy.chat.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import org.cubewhy.playground.ui.theme.Pink40
import org.cubewhy.playground.ui.theme.Pink80
import org.cubewhy.playground.ui.theme.Purple40
import org.cubewhy.playground.ui.theme.Purple80
import org.cubewhy.playground.ui.theme.PurpleGrey40
import org.cubewhy.playground.ui.theme.PurpleGrey80

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun QMessengerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && dynamicColorScheme(darkTheme) != null -> {
            dynamicColorScheme(darkTheme)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }!!

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?