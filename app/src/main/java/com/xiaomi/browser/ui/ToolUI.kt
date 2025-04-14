package com.xiaomi.browser.ui

import android.content.Context
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.xiaomi.browser.R
import com.xiaomi.browser.ui.HomeUI.CommonItem
import com.xiaomi.browser.ui.HomeUI.CommonToolBar
import com.xiaomi.browser.ui.HomeUI.QuickSetItem
import com.xiaomi.browser.util.BookmarkData
import com.xiaomi.browser.util.BrowserViewModel
import com.xiaomi.browser.util.HistoryItemData
import com.xiaomi.browser.util.PreferenceHelper
import com.xiaomi.browser.util.QuickSetData
import com.xiaomi.browser.util.Util.DESKTOP_USER_AGENT
import com.xiaomi.browser.util.Util.MOBILE_USER_AGENT
import com.xiaomi.browser.util.Util.openDownload
import com.xiaomi.browser.util.Util.shareUrl
import kotlinx.coroutines.launch
import org.json.JSONArray

object ToolUI {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ToolSheet(
        onDismiss : () -> Unit,
        webViewRef : WebView?,
        context : Context,
        showShortcutDialog : () -> Unit
    ){

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false,
        )

        val viewModel : BrowserViewModel = viewModel()

        val scope = rememberCoroutineScope()
        val prefs by lazy { PreferenceHelper(context) }

        val darkModeTitle =
            when (viewModel.darkMode){
                0 -> "跟随系统"
                1 -> "日间模式"
                else -> "夜间模式"
            }

        val nonPictureTitle =
            when (viewModel.NonPicture){
                0 -> "有图"
                1 -> "节流"
                else -> "无图"
            }

        val quickSetItems =
            listOf(
                QuickSetData(id = 1, icon = R.drawable.action_download, title = "已下载"),
                QuickSetData(id = 2, icon = R.drawable.action_share, title = "分享", enabled = viewModel.browserMode == 1),
                QuickSetData(id = 3, icon = R.drawable.action_agent, title = "桌面端", enabled = viewModel.accessMode == 1, switchMode = true),
                QuickSetData(id = 4, icon = R.drawable.action_history, title = "历史"),
                QuickSetData(id = 5, icon = R.drawable.actiom_night, title = darkModeTitle),
                QuickSetData(id = 6, icon = R.drawable.action_shortcut, title = "加到桌面", enabled = viewModel.browserMode == 1),
                QuickSetData(id = 7, icon = R.drawable.action_add_bookmark, title = "添加书签", enabled = viewModel.browserMode == 1),
                QuickSetData(id = 8, icon = R.drawable.action_fullscreen, title = "全屏", enabled = viewModel.fullscreenMode, switchMode = true),
                QuickSetData(id = 9, icon = R.drawable.action_bookmark, title = "书签"),
                QuickSetData(id = 10, icon = R.drawable.action_nopicture, title = nonPictureTitle.toString()),
                QuickSetData(id = 11, icon = R.drawable.action_diff, title = "资源")
            )

        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight().offset(y = 54.dp),
            sheetState = sheetState,
            onDismissRequest = {
                onDismiss()
                viewModel.quickSetState = 0
            }
        ) {
            BackHandler {
                if (viewModel.quickSetState == 0) {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        onDismiss()
                    }
                } else {
                    viewModel.quickSetState = 0
                }
            }
            AnimatedContent(
                targetState = viewModel.quickSetState,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    // 判断是前进（下一页）还是后退（上一页）
                    val isForward = targetState > initialState
                    val slideDirection = if (isForward) {
                        AnimatedContentTransitionScope.SlideDirection.Left  // 前进：从右滑入
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right // 后退：从左滑入
                    }

                    // 应用动态方向
                    (slideIntoContainer(towards = slideDirection, animationSpec = tween(300)) + fadeIn())
                        .togetherWith(
                            slideOutOfContainer(
                                towards = slideDirection,  // 旧内容往相同方向滑出
                                animationSpec = tween(600)
                            ) + fadeOut()
                        )
                }
            ) { state ->
                Column(Modifier.fillMaxSize()) {
                    when(state) {
                        0 -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(quickSetItems) { item ->  // Replace with your actual data list
                                    QuickSetItem(
                                        painter = painterResource(item.icon),  // Example icon
                                        text = item.title,
                                        id = item.id,
                                        onClick = { id ->
                                            when (id) {
                                                1 -> openDownload(context)

                                                2 -> {
                                                    shareUrl(context, webViewRef?.url ?: "")
                                                    onDismiss()
                                                }

                                                3 -> {
                                                    viewModel.accessMode = if (viewModel.accessMode == 0) 1 else 0
                                                    webViewRef?.settings?.userAgentString = if (viewModel.accessMode == 0) MOBILE_USER_AGENT else DESKTOP_USER_AGENT
                                                    webViewRef?.reload()
                                                }

                                                4 -> { viewModel.quickSetState = 1 }

                                                5 -> { viewModel.darkMode = ((viewModel.darkMode + 1) % 3).also { prefs.darkMode = it } }

                                                6 -> { showShortcutDialog() }

                                                7 -> {
                                                    prefs.addBookmark(BookmarkData(id = 0, url = webViewRef?.url ?: "未知", title = webViewRef?.title ?: "未知"))
                                                    Toast.makeText(context, "添加完成", Toast.LENGTH_SHORT).show()
                                                }

                                                8 -> { viewModel.fullscreenMode = !viewModel.fullscreenMode }

                                                9 -> { viewModel.quickSetState = 2 }

                                                10 -> {
                                                    viewModel.NonPicture = ((viewModel.NonPicture + 1) % 3).also { prefs.NonPicture = it
                                                        webViewRef?.reload()
                                                    }
                                                }

                                                11 -> { viewModel.quickSetState = 3 }
                                            }
                                        },
                                        enabled = item.enabled,
                                        switchMode = item.switchMode
                                    )
                                }
                            }
                        }

                        1 -> {
                            var showHistoryType by remember { mutableIntStateOf(0) }
                            val historyItems = remember {
                                mutableStateListOf<HistoryItemData>().apply {
                                    addAll(prefs.getHistory())
                                }
                            }
                            CommonToolBar(
                                onDismissRequest = { viewModel.quickSetState = 0 },
                                onSwitch = { showHistoryType = if (showHistoryType == 0) 1 else 0 },
                                onClear = { prefs.clearHistory().also { historyItems.clear() } }
                            )
                            if (historyItems.isNotEmpty()) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(if (showHistoryType == 0) 2 else 1),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 28.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    items(historyItems) { item ->  // Replace with your actual data list
                                        CommonItem(
                                            title = item.title,
                                            body = item.url
                                        ) {
                                            viewModel.browserUrl = item.url
                                            viewModel.browserMode = 1
                                            viewModel.quickSetState = 0
                                            onDismiss()
                                        }
                                    }
                                }
                            } else {
                                Text("没有历史哦~", Modifier.fillMaxWidth().weight(1f),
                                    textAlign = TextAlign.Center)
                            }
                        }

                        2 -> {
                            var showBookmarkType by remember { mutableIntStateOf(0) }
                            val bookmarkItems = remember {
                                mutableStateListOf<BookmarkData>().apply { addAll(prefs.getBookmark()) }
                            }

                            CommonToolBar(
                                onDismissRequest = { viewModel.quickSetState = 0 },
                                onClear = { prefs.clearBookmark().also { bookmarkItems.clear() } },
                                onSwitch = { showBookmarkType = if (showBookmarkType == 0) 1 else 0 }
                            )

                            if (bookmarkItems.isNotEmpty()) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(if (showBookmarkType == 0) 2 else 1),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 28.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    items(bookmarkItems) { item ->  // Replace with your actual data list
                                        CommonItem(
                                            title = item.title,
                                            body = item.url
                                        ) {
                                            viewModel.browserUrl = item.url
                                            viewModel.browserMode = 1
                                            viewModel.quickSetState = 0
                                            onDismiss()
                                        }
                                    }
                                }
                            } else {
                                Text("没有书签哦~", Modifier.fillMaxWidth().weight(1f),textAlign = TextAlign.Center)
                            }
                        }

                        //diff
                        3 -> {
                            val resItems = remember {
                                mutableStateListOf<String>()
                            }

                            LaunchedEffect(webViewRef) { // 使用 LaunchedEffect 确保只在特定条件下调用
                                webViewRef?.evaluateJavascript(
                                    "javascript:Array.from(document.getElementsByTagName('img')).map(img => img.src)",
                                    object : ValueCallback<String?> {
                                        override fun onReceiveValue(value: String?) {
                                            value?.let {
                                                try {
                                                    val imageUrlArray = JSONArray(it)
                                                    for (i in 0 until imageUrlArray.length()) {
                                                        val imageUrl = imageUrlArray.getString(i)
                                                        Log.d("WebView", "Image URL: $imageUrl")
                                                        if (!resItems.contains(imageUrl)) { // 避免重复添加
                                                            resItems.add(imageUrl)
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("WebView", "Error parsing JSON: ${e.message}")
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = { viewModel.quickSetState = 0 },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            }
                            if (resItems.isNotEmpty()) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed( 1),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 28.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    items(resItems) { item ->  // Replace with your actual data list

                                        Column(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.medium
                                                )
                                                .clickable {
                                                    viewModel.browserUrl = item
                                                    viewModel.browserMode = 1
                                                    viewModel.quickSetState = 0
                                                    onDismiss()
                                                }
                                                .padding(4.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            val imageUrl = item
                                            if (imageUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = imageUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .padding(8.dp)
                                                )
                                            } else {
                                                Text(text = "图片加载失败", modifier = Modifier.padding(8.dp))
                                            }
                                            Text(
                                                text = item,
                                                maxLines = 1,
                                                textAlign = TextAlign.Start,
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.typography.bodySmall.color
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text("没有资源哦~", Modifier.fillMaxWidth().weight(1f),
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}