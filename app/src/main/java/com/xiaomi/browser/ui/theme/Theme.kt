package com.xiaomi.browser.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaomi.browser.util.BrowserViewModel

@Composable
fun BrowserTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val viewModel : BrowserViewModel = viewModel()

    val colorScheme =
        if (viewModel.darkMode == 0) if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context) else if (viewModel.darkMode == 1) dynamicLightColorScheme(context) else dynamicDarkColorScheme(context)

    val view = LocalView.current
    val isDarkMode = viewModel.darkMode == 2 || isSystemInDarkTheme()

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            // 状态栏图标深色（浅色模式）
            isAppearanceLightStatusBars = !isDarkMode
            // 导航栏图标深色（可选）
            isAppearanceLightNavigationBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}