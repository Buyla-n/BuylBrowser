package com.xiaomi.browser.util

data class QuickSetData(
    val id: Int,
    val icon: Int,
    val title: String,
    val enabled: Boolean = true,
    val switchMode: Boolean = false
)