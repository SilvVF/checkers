package io.silv.checkers.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


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
fun DragDropTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography.copy(
            displayLarge = Typography.displayLarge.copy(
                fontFamily = varela
            ),
            displayMedium = Typography.displayMedium.copy(
                fontFamily = varela
            ),
            displaySmall = Typography.displaySmall.copy(
                fontFamily = varela
            ),
            headlineLarge = Typography.headlineLarge.copy(
                fontFamily = varela
            ),
            headlineMedium = Typography.headlineMedium.copy(
                fontFamily = varela
            ),
            headlineSmall = Typography.headlineSmall.copy(
                fontFamily = varela
            ),
            titleLarge = Typography.titleLarge.copy(
                fontFamily = varela
            ),
            titleMedium = Typography.titleMedium.copy(
                fontFamily = varela
            ),
            titleSmall = Typography.titleSmall.copy(
                fontFamily = varela
            ),
            bodyLarge = Typography.bodyLarge.copy(
                fontFamily = varela
            ),
            bodyMedium = Typography.bodyMedium.copy(
                fontFamily = varela
            ),
            bodySmall = Typography.bodySmall.copy(
                fontFamily = varela
            ),
            labelLarge = Typography.labelLarge.copy(
                fontFamily = varela
            ),
            labelMedium = Typography.labelMedium.copy(
                fontFamily = varela
            ),
            labelSmall = Typography.labelSmall.copy(
                fontFamily = varela
            )
        ),
        content = content
    )
}