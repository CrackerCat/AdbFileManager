package view.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import view.theme.ThemeState
import viewmodel.FileManagerViewModel
import java.awt.Desktop
import java.net.URI
import utils.UpdateInfo
import utils.VersionChecker

/**
 * 创建目录对话框
 */
@Composable
fun CreateDirectoryDialog(
    visible: Boolean,
    onDismiss: () -> Unit = {},
    onDismissRequest: () -> Unit = onDismiss,
    onConfirm: (dirName: String) -> Unit
) {
    if (!visible) return
    
    var dirName by remember { mutableStateOf("") }
    val isInputValid = dirName.isNotEmpty() && !dirName.contains("/") && !dirName.contains("\\")
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 自动聚焦到输入框
    LaunchedEffect(visible) {
        delay(100) // 短暂延迟以确保UI完全渲染
        focusRequester.requestFocus()
    }
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题和图标
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "创建新文件夹",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 输入框
                OutlinedTextField(
                    value = dirName,
                    onValueChange = { dirName = it },
                    label = { 
                        Text("文件夹名称") 
                    },
                    placeholder = {
                        Text(
                            "输入新文件夹名称",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    isError = dirName.isNotEmpty() && !isInputValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onKeyEvent {
                            if (it.key == Key.Enter && isInputValid) {
                                onConfirm(dirName)
                                true
                            } else {
                                false
                            }
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isInputValid) {
                                focusManager.clearFocus()
                                onConfirm(dirName)
                            }
                        }
                    ),
                    supportingText = {
                        if (dirName.isNotEmpty() && !isInputValid) {
                            Text(
                                "文件夹名称不能包含特殊字符如: / 或 \\",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Create,
                            contentDescription = null
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if (isInputValid) {
                                onConfirm(dirName)
                                dirName = ""
                            }
                        },
                        enabled = isInputValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("创建")
                    }
                }
            }
        }
    }
}

/**
 * 文件编辑对话框
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileEditDialog(
    visible: Boolean,
    fileName: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onSave: (content: String) -> Unit,
    fileEncoding: String,
    onEncodingChange: (String) -> Unit
) {
    if (!visible) return
    
    var content by remember(initialContent) { mutableStateOf(initialContent) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = androidx.compose.foundation.rememberScrollState()
    val scope = rememberCoroutineScope()
    val hasChanges = remember(initialContent) { 
        mutableStateOf(false)
    }
    
    // 检测当前是否为暗色模式
    val isDarkMode = ThemeState.isDark()
    
    // 检测内容变化
    LaunchedEffect(content) {
        hasChanges.value = content != initialContent
    }
    
    // 窗口状态，用于控制窗口位置
    val windowState = rememberWindowState(width = 900.dp, height = 700.dp)
    
    // 常用编码列表
    val commonEncodings = listOf(
        "UTF-8",
        "GBK",
        "GB18030",
        "GB2312",
        "UTF-16",
        "UTF-32",
        "ISO-8859-1",
        "BIG5",
        "Shift-JIS",
        "EUC-JP",
        "EUC-KR"
    )
    
    // 编码选择下拉菜单状态
    var showEncodingMenu by remember { mutableStateOf(false) }
    
    Window(
        state = windowState,
        title = "编辑 - $fileName",
        onCloseRequest = onDismiss,
        undecorated = true // 移除默认窗口装饰
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 自定义标题栏 - 可拖动
                Surface(
                    color = if (isDarkMode) 
                               MaterialTheme.colorScheme.surfaceVariant 
                           else 
                               MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 可拖动区域
                    MoveableWindowArea { dragModifier ->
                        Row(
                            modifier = dragModifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 文件名称
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // 窗口控制按钮
                            IconButton(
                                onClick = onDismiss
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 内容区域
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 状态指示
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "编辑文件内容",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // 编码选择下拉菜单
                        Box {
                            OutlinedButton(
                                onClick = { showEncodingMenu = true },
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "编码: $fileEncoding",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showEncodingMenu,
                                onDismissRequest = { showEncodingMenu = false }
                            ) {
                                commonEncodings.forEach { encoding ->
                                    DropdownMenuItem(
                                        text = { Text(encoding) },
                                        onClick = {
                                            onEncodingChange(encoding)
                                            showEncodingMenu = false
                                        },
                                        leadingIcon = {
                                            if (encoding == fileEncoding) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        
                        // 状态指示
                        AnimatedVisibility(
                            visible = hasChanges.value,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "已修改",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // 编辑器
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        ) {
                            // 行号列
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(top = 8.dp, end = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                val lines = content.split("\n").size
                                for (i in 1..lines) {
                                    Text(
                                        text = "$i",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                            
                            // 编辑区域
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 40.dp)
                            ) {
                                BasicTextField(
                                    value = content,
                                    onValueChange = { content = it },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .verticalScroll(scrollState)
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                scope.launch {
                                                    bringIntoViewRequester.bringIntoView()
                                                }
                                            }
                                        },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 底部按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 文件信息
                        Text(
                            text = "${content.length} 字符, ${content.split("\n").size} 行",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 取消按钮
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("取消")
                        }
                        
                        // 保存按钮
                        Button(
                            onClick = { onSave(content) },
                            enabled = hasChanges.value,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 创建文件对话框
 */
@Composable
fun CreateFileDialog(
    visible: Boolean,
    onDismiss: () -> Unit = {},
    onDismissRequest: () -> Unit = onDismiss,
    onConfirm: (fileName: String, content: String) -> Unit
) {
    if (visible) {
        var fileName by remember { mutableStateOf("") }
        var fileContent by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        
        // 验证文件名
        val validateFileName = {
            when {
                fileName.isBlank() -> {
                    isError = true
                    errorMessage = "文件名不能为空"
                    false
                }
                fileName.contains("/") || fileName.contains("\\") -> {
                    isError = true
                    errorMessage = "文件名不能包含特殊字符"
                    false
                }
                else -> {
                    isError = false
                    errorMessage = ""
                    true
                }
            }
        }
        
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = "创建新文件",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // 文件名输入框
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { 
                            fileName = it
                            if (isError) validateFileName()
                        },
                        label = { Text("文件名") },
                        placeholder = { Text("输入文件名 (例如: note.txt)") },
                        singleLine = true,
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 文件内容输入框
                    OutlinedTextField(
                        value = fileContent,
                        onValueChange = { fileContent = it },
                        label = { Text("文件内容") },
                        placeholder = { Text("输入文件内容 (可选)") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (validateFileName()) {
                            onConfirm(fileName, fileContent)
                        }
                    },
                    enabled = fileName.isNotBlank()
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 关于对话框
 */
@Composable
fun AboutDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    // 检查更新状态
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 应用图标
                Icon(
                    imageVector = Icons.Filled.Android,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 应用名称
                Text(
                    text = "ADB 文件管理器",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 版本信息
                Text(
                    text = "版本 ${FileManagerViewModel.VERSION}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 描述
                Text(
                    text = "一个简单易用的 ADB 文件管理工具，支持文件传输、编辑和管理。",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮区域
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // GitHub 链接
                    Button(
                        onClick = {
                            try {
                                Desktop.getDesktop().browse(URI(FileManagerViewModel.GITHUB_URL))
                            } catch (e: Exception) {
                                // 处理打开链接失败的情况
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.width(200.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Code,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("在 GitHub 上查看")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 检查更新按钮
                    Button(
                        onClick = {
                            isCheckingUpdate = true
                            coroutineScope.launch {
                                val update = VersionChecker.checkForUpdates(forceCheck = true)
                                isCheckingUpdate = false
                                if (update != null) {
                                    updateAvailable = update
                                    showUpdateDialog = true
                                } else {
                                    // 显示已是最新版本的提示
                                    // 可以使用 SnackBar 或者其他方式提示
                                }
                            }
                        },
                        enabled = !isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.width(200.dp)
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("检查中...")
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Update,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("检查更新")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 关闭按钮
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("关闭")
                }
            }
        }
    }
    
    // 显示更新对话框
    updateAvailable?.let { info ->
        UpdateDialog(
            visible = showUpdateDialog,
            updateInfo = info,
            onDismiss = { showUpdateDialog = false }
        )
    }
}

/**
 * 更新提示对话框
 */
@Composable
fun UpdateDialog(
    visible: Boolean,
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var neverShowUpdates by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 更新图标
                Icon(
                    imageVector = Icons.Filled.Update,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 标题
                Text(
                    text = "发现新版本",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 版本信息
                Text(
                    text = "新版本 ${updateInfo.version} 已发布",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 更新说明
                Text(
                    text = "更新内容：",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 更新说明内容
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, false)
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = updateInfo.releaseNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 永不提示选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = neverShowUpdates,
                        onCheckedChange = { neverShowUpdates = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "不再提示版本更新",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    // 稍后再说按钮
                    TextButton(
                        onClick = {
                            // 如果选择了永不提示，保存配置
                            if (neverShowUpdates) {
                                VersionChecker.saveUpdateConfig(neverShowUpdates = true)
                            } else {
                                // 否则只忽略当前版本
                                VersionChecker.saveUpdateConfig(ignoredVersion = updateInfo.version)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("稍后再说")
                    }

                    // 立即更新按钮
                    Button(
                        onClick = {
                            try {
                                Desktop.getDesktop().browse(URI(updateInfo.downloadUrl))
                                // 如果选择了永不提示，保存配置
                                if (neverShowUpdates) {
                                    VersionChecker.saveUpdateConfig(neverShowUpdates = true)
                                }
                            } catch (e: Exception) {
                                // 处理打开链接失败的情况
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("立即更新")
                    }
                }
            }
        }
    }
} 