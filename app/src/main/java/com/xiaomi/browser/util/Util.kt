package com.xiaomi.browser.util

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Util {

    const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36"
    const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 15; 24094RAD4C Build/AP3A.240617.008) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.86 Mobile Safari/537.36"

    private fun getOriginalFileName(url: String): String? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()
            connection.getHeaderField("Content-Disposition")?.let { disposition ->
                Regex("filename=\"?(.+?)\"?;?").find(disposition)?.groupValues?.get(1)
            } ?: url.substringAfterLast('/').takeIf { it.contains('.') }
        } catch (e: Exception) {
            null
        }
    }

    fun startDownload(context: Context, url: String) {
        val fileName = getOriginalFileName(url) ?: URLUtil.guessFileName(url, null,  null)

        val request = DownloadManager.Request(url.toUri()).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(URLUtil.guessFileName(url, null, "zip"))
            setDescription(url)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setAllowedOverMetered(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openDownload(context: Context) {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.android.providers.downloads.ui",
                    "com.android.providers.downloads.ui.DownloadList"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val fallbackIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                } else {
                    Toast.makeText(context, "无法找到下载管理器", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "无权限访问系统下载管理器", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "打开下载管理器失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareUrl(context: Context, url: String, title: String? = "分享链接") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title)

        try {
            context.startActivity(shareIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "没有找到可用的分享应用", Toast.LENGTH_SHORT).show()
        }
    }

}