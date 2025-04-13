package com.xiaomi.browser.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaomi.browser.util.BrowserViewModel
import com.xiaomi.browser.util.HistoryItemData
import com.xiaomi.browser.util.PreferenceHelper
import com.xiaomi.browser.util.Util.DESKTOP_USER_AGENT
import com.xiaomi.browser.util.Util.MOBILE_USER_AGENT
import com.xiaomi.browser.util.Util.startDownload
import com.xiaomi.browser.util.WebViewState


object WebUI {
    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BrowserWeb(
        modifier: Modifier = Modifier,
        onWebViewCreated: (WebView) -> Unit = {},
        onStateChange: (WebViewState) -> Unit = {}
    ) {
        val viewModel : BrowserViewModel = viewModel()
        var loading by remember { mutableStateOf(false) }
        var targetProgress by remember { mutableIntStateOf(0) }
        val animatedProgress by animateFloatAsState(
            targetValue = targetProgress / 100f,
            animationSpec = tween(durationMillis = 300)  // 可以调整动画持续时间
        )
        val context = LocalContext.current
        var searchUrlText by remember { mutableStateOf(viewModel.browserUrl) }
        var showDownloadDialog by remember { mutableStateOf(false) }
        var downloadUrl by remember { mutableStateOf("") }

        val pref = PreferenceHelper(context)
        var showInfoSheet by remember { mutableStateOf(false) }

        var webView = remember {
            WebView(context).apply {
                webViewClient = WebViewClient()

                with(settings) {
                    javaScriptEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    domStorageEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    loadsImagesAutomatically = true
                    allowContentAccess = true
                    allowFileAccess = true
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    defaultTextEncodingName = "utf-8"
                    userAgentString = if (viewModel.accessMode == 0) MOBILE_USER_AGENT else DESKTOP_USER_AGENT
                    builtInZoomControls = true
                    displayZoomControls = false // 隐藏原生缩放控件（可选）
                    setSupportZoom(true)
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        loading = false
                        val shouldAddNew = when {
                            pref.getHistory().isEmpty() -> true
                            else -> pref.getHistory().last().url != url
                        }
                        if (shouldAddNew) {
                            pref.addHistory(
                                HistoryItemData(
                                    id = System.currentTimeMillis(),
                                    url = view?.url ?: "未知链接",
                                    title = view?.title?.ifEmpty { "无标题" } ?: "无标题"
                                )
                            )
                        }
                        onStateChange(
                            WebViewState(
                                canGoBack = canGoBack(),
                                canGoForward = canGoForward(),
                                isLoading = false
                            )
                        )
                    }
                    override fun shouldOverrideUrlLoading(
                        webView: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        return when (request.url.scheme) {
                            "http", "https" -> super.shouldOverrideUrlLoading(webView, request)
                            else -> true
                        }
                    }
                }

                setDownloadListener { url, _, _, _, _ ->
                    downloadUrl = url
                    showDownloadDialog = true
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        targetProgress = newProgress
                    }
                }
            }.also { webView ->
                onWebViewCreated(webView) // 创建后把 WebView 传回给父组件
            }
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        CookieManager.getInstance().setAcceptCookie(true)
        WebView.setWebContentsDebuggingEnabled(true) // 允许 Chrome 远程调试
        Scaffold(
            contentWindowInsets = WindowInsets(bottom = 0),
            topBar = {
//                if (showInfoSheet){
//                    ModalBottomSheet(
//                        onDismissRequest = { showInfoSheet = false },
//                        modifier = Modifier.graphicsLayer(
//                            rotationX = 180f
//                        ).height(256.dp),
//                        sheetState = rememberModalBottomSheetState(
//                            skipPartiallyExpanded = true, // 跳过中间状态
//                            confirmValueChange = { target ->
//                                // 限制只能展开到 Expanded 或 Collapsed
//                                target == SheetValue.Expanded || target == SheetValue.Hidden
//                            }
//                        )
//                    ) {
//                        Box(
//                            modifier = Modifier.graphicsLayer(
//                                rotationX = 180f
//                            ).fillMaxSize().padding(top = 16.dp),
//                            contentAlignment = Alignment.TopStart
//                        ) {
//                            Row {
//                                Text(viewModel.browserUrl)
//                                IconButton(
//                                    onClick = { showInfoSheet = false },
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Rounded.Close,
//                                        contentDescription = null
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
                AnimatedVisibility(
                    visible = viewModel.barVisible,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier
                                    .height(54.dp)
                                    .fillMaxWidth()
                                    .padding(end = 16.dp, bottom = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LaunchedEffect(viewModel.browserUrl) {
                                    searchUrlText = viewModel.browserUrl
                                }
                                BasicTextField(
                                    value = searchUrlText,
                                    onValueChange = { searchUrlText = it.ifEmpty { " " } },
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .fillMaxWidth(0.8f)
                                        .padding(start = 8.dp)
                                        .background(Color.Transparent),
                                    textStyle = TextStyle.Default.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = TextStyle.Default.lineHeight
                                    ),
                                    singleLine = true,
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            val finalUrl =
                                                if (!searchUrlText.startsWith("http://") && !searchUrlText.startsWith(
                                                        "https://"
                                                    )
                                                ) {
                                                    "https://$searchUrlText"
                                                } else {
                                                    searchUrlText
                                                }
                                            viewModel.browserUrl = finalUrl
                                        }
                                    ),
                                )
                                IconButton(
                                    onClick = {
                                        webView.reload()
                                    }//,
//                                    Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
//                                IconButton(
//                                    onClick = {
//                                        showInfoSheet = true
//                                    },
//                                    Modifier.weight(1f)
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Rounded.Build,
//                                        contentDescription = null,
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(18.dp)
//                                    )
//                                }
                            }
                        },
                        modifier = Modifier.height(88.dp)
                    )
                }
            },
            modifier = modifier
        ) { innerPadding ->



            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (viewModel.barVisible) innerPadding.calculateTopPadding() else 0.dp)
            ) {

                // 加载进度条
                AnimatedVisibility(
                    visible = loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                    )
                }
                BackHandler {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else if (viewModel.browserMode == 1) {
                        viewModel.browserMode = 0
                    } else {
                        val activity = context as ComponentActivity
                        activity.onBackPressedDispatcher.onBackPressed()
                    }
                }


                // WebView 显示区域
                AndroidView(
                    factory = { webView },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    update = {
                        if (it.url != viewModel.browserUrl) {
                            it.loadUrl(viewModel.browserUrl)
                        }
                    }
                )
                if (showDownloadDialog && downloadUrl.isNotEmpty()) {
                    DownloadDialog(downloadUrl) { showDownloadDialog = false }
                }
            }
        }
    }

    @Composable
    fun DownloadDialog(url: String, onDismissRequest: () -> Unit) {
        val context = LocalContext.current
        AlertDialog(
            icon = {
                Icon(Icons.Rounded.Warning, contentDescription = "Example Icon")
            },
            title = {
                Text(text = "发起下载请求")
            },
            text = {
                Text(text = "是否下载 $url")
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDownload(context, url)
                        onDismissRequest()
                    }
                ) {
                    Text("下载")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }


}