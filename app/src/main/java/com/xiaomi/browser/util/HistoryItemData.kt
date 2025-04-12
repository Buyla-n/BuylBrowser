package com.xiaomi.browser.util

import android.graphics.Bitmap

data class HistoryItemData(
    val id: Long,       // 唯一ID（可用时间戳）
    val url: String,    // 网址
    val title: String
)
