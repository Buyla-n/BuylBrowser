package com.xiaomi.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xiaomi.browser.ui.HomeUI.BrowserHome
import com.xiaomi.browser.ui.theme.BrowserTheme
import com.xiaomi.browser.util.BrowserViewModel
import com.xiaomi.browser.util.PreferenceHelper
import java.io.File
import java.io.FileOutputStream

class BrowserActivity : ComponentActivity() {
    private var intentUrl by mutableStateOf<String?>(null)
    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        handleIntent(intent)

        viewModel.darkMode = PreferenceHelper(context = this).darkMode
        viewModel.NonPicture = PreferenceHelper(this).NonPicture

        setContent {
            BrowserTheme {
                LaunchedEffect(intentUrl) {
                    if (intentUrl != null) {
                        viewModel.browserUrl = intentUrl as String
                        viewModel.browserMode = 1
                    }
                }
                BrowserHome(this)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intentUrl = when {
            intent?.action == Intent.ACTION_VIEW -> {
                when {
                    // 处理HTTP/HTTPS URL
                    intent.data?.scheme?.let { it == "http" || it == "https" } == true -> {
                        intent.data?.toString()
                    }
                    // 处理本地HTML文件
                    intent.type?.equals("text/html") == true -> {
                        handleHtmlFileIntent(intent)
                    }
                    // 处理文件URI
                    intent.data?.scheme == "file" -> {
                        intent.data?.path
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun handleHtmlFileIntent(intent: Intent): String? {
        return try {
            // 从内容URI读取HTML文件
            val inputStream = contentResolver.openInputStream(intent.data!!)
            val file = File(cacheDir, "temp.html")
            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }
            "file://${file.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}