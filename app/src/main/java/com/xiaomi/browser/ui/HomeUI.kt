package com.xiaomi.browser.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xiaomi.browser.R
import com.xiaomi.browser.ui.WebUI.BrowserWeb
import com.xiaomi.browser.util.BrowserViewModel
import com.xiaomi.browser.util.PreferenceHelper
import com.xiaomi.browser.util.QuickLinkData
import com.xiaomi.browser.util.WebViewState

object HomeUI {
    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BrowserHome(context: Context) {

        val viewModel : BrowserViewModel = viewModel()
        val prefs by lazy { PreferenceHelper(context) }
        var showBottomMenu by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }
        var showShortcutDialog by remember { mutableStateOf(false) }
        var showCustom by remember { mutableStateOf(false) }
        var webViewRef by remember { mutableStateOf<WebView?>(null) }
        var webViewState by remember { mutableStateOf(WebViewState()) }
        var enabledQuickLink by remember { mutableStateOf(prefs.enabledQuickLink) }

        val quickLinkItems = remember {
            mutableStateListOf<QuickLinkData>().apply {
                addAll(prefs.getQuickLinks())
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
            floatingActionButton = {
                if (viewModel.fullscreenMode) {
                    FloatingActionButton(
                        onClick = { viewModel.barVisible = !viewModel.barVisible },
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        shape = RoundedCornerShape(36.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "显示菜单"
                        )
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = viewModel.barVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    ) {
                        Row {
                            ActionItem(
                                modifier = Modifier.weight(1f),
                                painter = painterResource(R.drawable.action_backward),
                                onClick = { webViewRef?.goBack() },
                                enabled = webViewState.canGoBack && viewModel.browserMode == 1
                            )
                            ActionItem(
                                modifier = Modifier.weight(1f),
                                painter = painterResource(R.drawable.action_forward),
                                onClick = { webViewRef?.goForward() },
                                enabled = webViewState.canGoForward && viewModel.browserMode == 1
                            )
                            ActionItem(
                                modifier = Modifier.weight(1f),
                                painter = painterResource(R.drawable.action_tabs)
                            )
                            ActionItem(
                                modifier = Modifier.weight(1f),
                                painter = painterResource(R.drawable.action_menu),
                                onClick = { showBottomMenu = true }
                            )
                            if (viewModel.browserMode == 0) {
                                ActionItem(
                                    modifier = Modifier.weight(1f),
                                    painter = painterResource(R.drawable.action_bookmark),
                                    onClick = {
                                        viewModel.quickSetState = 2
                                        showBottomMenu = true
                                    }
                                )
                            } else {
                                ActionItem(
                                    modifier = Modifier.weight(1f),
                                    painter = painterResource(R.drawable.action_home),
                                    onClick = {
                                        viewModel.browserMode = 0
                                    }
                                )
                            }
                        }
                    }
                }


                if (showBottomMenu) {
                    ToolUI.ToolSheet(
                        onDismiss = {showBottomMenu = false},
                        webViewRef = webViewRef,
                        context = context,
                        showShortcutDialog = {showShortcutDialog = true}
                    )
                }

                if (showShortcutDialog) {
                    AddShortcutDialog(
                        onConfirmRequest = { title, link ->
                            val icon = Icon.createWithResource(context, R.drawable.action_bookmark)
                            val shortcutManager = context.getSystemService(
                                ShortcutManager::class.java)

                            val shortcut = ShortcutInfo.Builder(context, "web_shortcut_$link")
                                .setShortLabel(title)
                                .setLongLabel(title)
                                .setIcon(icon)
                                .setIntent(Intent(Intent.ACTION_VIEW, link.toUri()).setPackage(context.packageName))
                                .build()

                            shortcutManager.requestPinShortcut(shortcut, null)
                        },
                        onDismissRequest = {showShortcutDialog = false},
                        url = webViewRef?.url ?: ""
                    )

                }

                if (showAddDialog) {
                    AddShortcutDialog(
                        onConfirmRequest = { title, link ->
                            val quickData = QuickLinkData(
                                id = (quickLinkItems.size + 1).toLong(),
                                title = title,
                                link = link,
                                iconRes = R.drawable.action_bookmark
                            )
                            quickLinkItems.add(quickData)
                            prefs.addQuickLink(quickData)
                        },
                        onDismissRequest = {showAddDialog = false}
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState  = viewModel.browserMode
            ) { target ->
                if (target == 0) {
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(16.dp)
                                    .size(29.dp),
                                onClick = {
                                    showCustom = !showCustom
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.action_custom),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }

                            AnimatedVisibility(
                                visible = showCustom,
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            MaterialTheme.shapes.extraLarge
                                        )
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "快速链接",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Switch(
                                            checked = enabledQuickLink,
                                            onCheckedChange = { isChecked ->
                                                enabledQuickLink = isChecked
                                                prefs.enabledQuickLink = isChecked
                                            }
                                        )
                                    }
                                }
                            }

                            Icon(
                                painter = painterResource(R.drawable.explore),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //主页搜索框
                            Row(
                                modifier = Modifier
                                    .height(54.dp)
                                    .fillMaxWidth(0.85f)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 16.dp)
                                        .size(25.5.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_search),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(3.6299744.dp)
                                            .align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.surface
                                    )
                                }
                                var searchText by remember { mutableStateOf(" ") }
                                BasicTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it.ifEmpty { " " } },
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .fillMaxWidth(0.9f)
                                        .padding(start = 8.dp)
                                        .background(Color.Transparent),
                                    textStyle = TextStyle.Default.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = TextStyle.Default.lineHeight
                                    ),
                                    singleLine = true,
                                    keyboardActions = KeyboardActions(
                                        onDone = { viewModel.searchWithEngine(searchText) }
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        if (enabledQuickLink) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(quickLinkItems) { item ->  // Replace with your actual data list
                                    QuickLinkItem(
                                        painter = painterResource(item.iconRes),  // Example icon
                                        text = item.title,
                                        link = item.link,
                                        onClick = { link ->
                                            viewModel.browserUrl = link
                                            viewModel.browserMode = 1
                                        },
                                        showBadge = showCustom,
                                        onDelete = {
                                            val data = QuickLinkData(
                                                id = item.id,
                                                title = item.title,
                                                link = item.link,
                                                iconRes = R.drawable.action_bookmark
                                            )
                                            quickLinkItems.remove(data)
                                            prefs.deleteQuickLink(data)
                                        }
                                    )
                                }
                                item {
                                    QuickLinkItem(
                                        painter = painterResource(R.drawable.action_add),  // Example icon
                                        text = "添加",
                                        size = 36.dp,
                                        onClick = {
                                            showAddDialog = true
                                        },
                                        showBadge = false,
                                        onDelete = {}
                                    )
                                }
                            }
                        }
                    }
                } else {
                    BrowserWeb(
                        modifier = Modifier.padding(
                            bottom = if (viewModel.barVisible) 52.dp else 0.dp,
                            top = if (viewModel.barVisible) 0.dp else 0.dp
                        ),
                        onWebViewCreated = { webView -> webViewRef = webView },
                        onStateChange = { newState -> webViewState = newState }
                    )
                }
            }
        }
    }

    @Composable
    private fun AddShortcutDialog(
        onConfirmRequest: (String, String) -> Unit,
        onDismissRequest: () -> Unit,
        url: String = ""
    ) {
        var title by remember { mutableStateOf("网站") }
        var link by remember { mutableStateOf(url) }
        AlertDialog(
            title = {
                Text(text = "添加")
            },
            text = {
                Column {

                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        label = { Text("标题") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    TextField(
                        value = link,
                        onValueChange = { link = it },
                        modifier = Modifier.padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        label = { Text("网址") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && link.isNotEmpty()) {
                            onConfirmRequest(title, link)
                            onDismissRequest()
                        }
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("退出")
                }
            }
        )
    }

    @Composable
    fun QuickSetItem(
        painter: Painter,
        onClick: (Int) -> Unit = {},
        text: String,
        size: Dp = 26.5.dp,
        id: Int = 0,
        enabled: Boolean = true,
        switchMode: Boolean = false,
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = { if (enabled || switchMode) onClick(id) })
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)  // 调整背景大小
                    .background(
                        if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = 0.5f
                        ),
                        RoundedCornerShape(45.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(size),
                    tint = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                overflow = TextOverflow.Visible
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun QuickLinkItem(
        painter: Painter,
        onClick: (String) -> Unit = {},
        text: String,
        size: Dp = 26.5.dp,
        link: String = "",
        showBadge: Boolean,
        onDelete: () -> Unit
    ) {
        var showRemove by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .combinedClickable(
                    indication = null,
                    interactionSource = null,
                    onClick = {
                        showRemove = false
                        onClick(link)
                    },
                    onLongClick = {
                        showRemove = !showRemove
                    }
                )
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BadgedBox(
                badge = {
                    if(showRemove || showBadge) {
                        Badge(
                            modifier = Modifier.clickable {
                                showRemove= false
                                onDelete()
                            }
                        ){
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Email"
                            )
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)  // 调整背景大小
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(size),
                        tint = MaterialTheme.colorScheme.surface// 图标大小保持不变,
                    )
                }
            }
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                overflow = TextOverflow.Visible
            )
        }
    }

    @Composable
    fun ActionItem(
        modifier: Modifier,
        painter: Painter,
        enabled: Boolean = true,
        onClick: () -> Unit = {}
    ) {
        IconButton(
            modifier = modifier.then(Modifier.fillMaxHeight()),
            onClick = { onClick() },
            enabled = enabled,
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(26.5.dp)
            )
        }
    }

    @Composable
    fun CommonItem(
        title : String = "None",
        body : String = "null",
        onClick: () -> Unit
    ){
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { onClick() }
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                maxLines = 1,
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.typography.titleSmall.color
            )
            Text(
                text = body,
                maxLines = 1,
                textAlign = TextAlign.Start,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.typography.bodySmall.color
            )
        }
    }

    @Composable
    fun CommonToolBar(
        onDismissRequest: () -> Unit,
        onSwitch : () -> Unit,
        onClear : () -> Unit
    ){
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onDismissRequest() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { onSwitch() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null
                )
            }
            IconButton(
                onClick = { onClear() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null
                )
            }
        }
    }
}