package com.xiaomi.browser.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BrowserViewModel() : ViewModel() {
    var browserUrl by mutableStateOf(value = "")
    var browserMode by mutableIntStateOf(value = 0)
    var darkMode by mutableIntStateOf(value = 0)
    var accessMode by mutableIntStateOf(value = 0)
    var barVisible by mutableStateOf(value = true)
    var NonPicture by mutableIntStateOf(value = 0)
    // 0 quick set | 1 history | 2 bookmark
    var quickSetState by mutableIntStateOf(value = 0)
    var fullscreenMode by mutableStateOf(value = false)
    var incognitoMode by mutableStateOf(value = false)
    fun searchWithEngine(url: String){
        browserUrl = "https://cn.bing.com/search?q=$url&form=QBLH&sp=-1&lq=0&pq=$url&sc=11-4&qs=n&sk="
        browserMode = 1
    }
}