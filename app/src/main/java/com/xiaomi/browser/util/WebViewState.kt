package com.xiaomi.browser.util

data class WebViewState(
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false
)