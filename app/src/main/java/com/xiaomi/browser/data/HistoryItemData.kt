package com.xiaomi.browser.data

data class HistoryItemData(
    val id: Long,       // 唯一ID（可用时间戳）
    val url: String,    // 网址
    val title: String
)