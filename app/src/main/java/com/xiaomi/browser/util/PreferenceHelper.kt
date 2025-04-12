package com.xiaomi.browser.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xiaomi.browser.R

class PreferenceHelper(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 快速链接开关状态
    var enabledQuickLink: Boolean
        get() = sharedPref.getBoolean("KEY_ENABLED_QUICK_LINK", true)
        set(value) = sharedPref.edit { putBoolean("KEY_ENABLED_QUICK_LINK", value) }

    var darkMode: Int
        get() = sharedPref.getInt("KEY_DARKMODE", 0)
        set(value) = sharedPref.edit { putInt("KEY_DARKMODE", value) }

    fun addQuickLink(item: QuickLinkData): Boolean {
        val currentList = getQuickLinks().toMutableList()
        currentList.add(item)
        sharedPref.edit {
            putString("KEY_QUICK_LINKS", gson.toJson(currentList))
        }
        return true
    }

    fun deleteQuickLink(item: QuickLinkData): Boolean {
        val currentList = getQuickLinks().toMutableList()
        currentList.remove(item)
        sharedPref.edit {
            putString("KEY_QUICK_LINKS", gson.toJson(currentList))
        }
        return true
    }

    // 读取书签列表（带默认值）
    fun getQuickLinks(): List<QuickLinkData> {
        val json = sharedPref.getString("KEY_QUICK_LINKS", null)
        return if (json.isNullOrEmpty()) {
            // 默认书签数据
            listOf(
                QuickLinkData(1, "必应", "https://www.bing.com/", R.drawable.action_bookmark),
                QuickLinkData(2, "小米", "https://www.mi.com/", R.drawable.action_bookmark),
                QuickLinkData(3, "Github", "https://github.com/", R.drawable.action_bookmark),
                QuickLinkData(4, "Dick", "https://deepseek.com/", R.drawable.action_bookmark),
                QuickLinkData(5, "太极", "https://kernelsu.org/", R.drawable.action_bookmark),
                QuickLinkData(6, "酷安", "https://www.coolapk.com/", R.drawable.action_bookmark)
            )
        } else {
            val type = object : TypeToken<List<QuickLinkData>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    fun addHistory(item: HistoryItemData): Boolean {
        val currentList = getHistory().toMutableList()
        currentList.add(item)
        sharedPref.edit {
            putString("KEY_HISTORY", gson.toJson(currentList))
        }
        return true
    }

    fun getHistory(): List<HistoryItemData> {
        val json = sharedPref.getString("KEY_HISTORY", null)
        return if (json.isNullOrEmpty()) {
            listOf(
                HistoryItemData(id = 0, url = "示例网址", title = "示例记录")
            )
        } else {
            val type = object : TypeToken<List<HistoryItemData>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    fun clearHistory() {
        val currentList = emptyList<HistoryItemData>().toMutableList()
        sharedPref.edit {
            putString("KEY_HISTORY", gson.toJson(currentList))
        }
    }

    //book
    fun addBookmark(item: BookmarkData): Boolean {
        val currentList = getBookmark().toMutableList()
        currentList.add(item)
        sharedPref.edit {
            putString("KEY_BOOKMARK", gson.toJson(currentList))
        }
        return true
    }

    fun getBookmark(): List<BookmarkData> {
        val json = sharedPref.getString("KEY_BOOKMARK", null)
        return if (json.isNullOrEmpty()) {
            listOf(
                BookmarkData(id = 0, url = "示例网址", title = "示例书签")
            )
        } else {
            val type = object : TypeToken<List<BookmarkData>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    fun clearBookmark() {
        val currentList = emptyList<BookmarkData>().toMutableList()
        sharedPref.edit {
            putString("KEY_BOOKMARK", gson.toJson(currentList))
        }
    }
}