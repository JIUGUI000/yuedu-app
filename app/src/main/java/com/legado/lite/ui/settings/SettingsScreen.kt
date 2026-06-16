package com.legado.lite.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legado.lite.BuildConfig
import com.legado.lite.R
import com.legado.lite.data.preferences.ReaderPreferences
import com.legado.lite.tts.TtsController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onOpenSources: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val reader by vm.reader.collectAsState(initial = ReaderPreferences())
    val ttsState by TtsController.state.collectAsState()
    val ttsInfo by TtsController.modelInfo.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var exportDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Section(title = "书源管理") {
                ListItem(
                    icon = Icons.Outlined.Book,
                    title = stringResource(R.string.book_source_management),
                    subtitle = "查看 / 导入 / 导出书源",
                    onClick = onOpenSources
                )
            }
            Section(title = "阅读偏好") {
                ListItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.setting_bg_color),
                    subtitle = "当前：${bgName(reader.bgColor)}"
                ) { /* 改背景色占位 */ }
                ListItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.setting_page_mode),
                    subtitle = "当前：${pageModeName(reader.pageMode)}"
                )
                ListItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.setting_font_size),
                    subtitle = "字号 ${reader.fontSize} · 行距 ${"%.2f".format(reader.lineSpacing)}"
                )
            }
            Section(title = "听书 / TTS") {
                ListItem(
                    icon = Icons.Outlined.RecordVoiceOver,
                    title = stringResource(R.string.setting_tts_model_status),
                    subtitle = "$ttsInfo (${stateName(ttsState)})"
                )
            }
            Section(title = "数据") {
                ListItem(
                    icon = Icons.Outlined.ContentCopy,
                    title = "导出书源到剪贴板",
                    subtitle = "复制所有书源 JSON",
                    onClick = {
                        scope.launch {
                            val json = vm.exportSources()
                            copyToClipboard(context, "LegadoLite-BookSources", json)
                            snackbar.showSnackbar(context.getString(R.string.msg_export_success))
                            exportDialog = json
                        }
                    }
                )
            }
            Section(title = stringResource(R.string.about)) {
                ListItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.version),
                    subtitle = "v${BuildConfig.VERSION_NAME}"
                )
            }
        }
    }

    if (exportDialog != null) {
        AlertDialog(
            onDismissRequest = { exportDialog = null },
            title = { Text("书源 JSON（已复制到剪贴板）") },
            text = { Text(exportDialog!!.take(2000) + if (exportDialog!!.length > 2000) "…" else "") },
            confirmButton = { TextButton(onClick = { exportDialog = null }) { Text("关闭") } }
        )
    }
}

private fun bgName(key: String) = when (key) {
    "green" -> "护眼绿"
    "black" -> "夜间黑"
    "yellow" -> "羊皮纸"
    else -> "白底"
}

private fun pageModeName(key: String) = when (key) {
    "cover" -> "覆盖"
    "simulation" -> "仿真"
    else -> "滑动"
}

private fun stateName(s: TtsController.State) = when (s) {
    TtsController.State.UNINITIALIZED -> "未初始化"
    TtsController.State.LOADING -> "加载中"
    TtsController.State.READY -> "就绪"
    TtsController.State.ERROR -> "错误"
    TtsController.State.PLAYING -> "播放中"
    TtsController.State.PAUSED -> "已暂停"
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    content()
}

@Composable
private fun ListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(0.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing?.invoke()
        }
    }
}
