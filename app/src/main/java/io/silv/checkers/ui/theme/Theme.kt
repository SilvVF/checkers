package io.silv.checkers.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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