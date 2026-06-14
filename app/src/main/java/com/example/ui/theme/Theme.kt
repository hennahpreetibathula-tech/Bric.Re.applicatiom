package com.example.ui.theme

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

private val DarkColorScheme =
  lightColorScheme(
    primary = Color(0xFF000000),
    secondary = Color(0xFF004D40),
    tertiary = Color(0xFF111111),
    background = Color(0xFFBDFCC9),
    surface = Color(0xFFE8FFF0),
    surfaceVariant = Color(0xFFA6EBCF),
    onPrimary = Color(0xFFBDFCC9),
    onSecondary = Color(0xFFBDFCC9),
    onTertiary = Color(0xFFBDFCC9),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF111111)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF000000),
    secondary = Color(0xFF004D40),
    tertiary = Color(0xFF111111),
    background = Color(0xFFBDFCC9),
    surface = Color(0xFFE8FFF0),
    surfaceVariant = Color(0xFFA6EBCF),
    onPrimary = Color(0xFFBDFCC9),
    onSecondary = Color(0xFFBDFCC9),
    onTertiary = Color(0xFFBDFCC9),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF111111)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is false by default to preserve the Forest Green & Gold brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
