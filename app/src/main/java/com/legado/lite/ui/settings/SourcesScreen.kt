package com.legado.lite.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.legado.lite.R
import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.ui.LegadoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SourcesViewModel(app: android.app.Application) : LegadoViewModel(app) {

    val sources: StateFlow<List<BookSourceEntity>> = container.bookSourceRepository.observeAll()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(source: BookSourceEntity) {
        scope.launch { container.bookSourceRepository.update(source.copy(enabled = !source.enabled)) }
    }

    fun delete(source: BookSourceEntity) {
        scope.launch { container.bookSourceRepository.delete(source) }
    }

    fun import(text: String): Int {
        var n = 0
        kotlinx.coroutines.runBlocking { n = container.bookSourceRepository.importFromJson(text) }
        return n
    }

    fun addNew(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) return
        scope.launch {
            container.bookSourceRepository.upsert(BookSourceEntity(name = name, url = url))
        }
    }

    fun update(source: BookSourceEntity) {
        scope.launch { container.bookSourceRepository.update(source) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(onBack: () -> Unit) {
    val vm: SourcesViewModel = viewModel()
    val list by vm.sources.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var importDialog by remember { mutableStateOf(false) }
    var editSource by remember { mutableStateOf<BookSourceEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.book_source_management)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { importDialog = true }) {
                        Icon(Icons.Outlined.FileOpen, contentDescription = stringResource(R.string.action_import))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editSource = BookSourceEntity(name = "", url = "") }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_add_source))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (list.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.hint_no_book_source))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(list, key = { it.id }) { s ->
                    SourceRow(
                        source = s,
                        onToggle = { vm.toggle(s) },
                        onEdit = { editSource = s },
                        onDelete = { vm.delete(s) }
                    )
                }
            }
        }
    }

    if (importDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { importDialog = false },
            title = { Text("导入书源") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("粘贴 Legado 风格 JSON。可粘贴单条、数组、或者多行 JSON。", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("{...}") },
                        minLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = cm.primaryClip
                        val pasted = clip?.getItemAt(0)?.text?.toString()
                        if (!pasted.isNullOrBlank()) text = pasted
                    }) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("  从剪贴板读取")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val n = vm.import(text)
                    importDialog = false
                    scope.launch { snackbar.showSnackbar("导入成功 $n 条") }
                }) { Text(stringResource(R.string.action_import)) }
            },
            dismissButton = { TextButton(onClick = { importDialog = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }

    editSource?.let { s ->
        EditSourceDialog(
            source = s,
            onSave = { updated ->
                if (s.id == 0L) vm.addNew(updated.name, updated.url)
                else vm.update(updated)
                editSource = null
            },
            onDismiss = { editSource = null }
        )
    }
}

@Composable
private fun SourceRow(
    source: BookSourceEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(source.name, style = MaterialTheme.typography.titleSmall)
                Text(source.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Switch(checked = source.enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = null) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = null) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSourceDialog(
    source: BookSourceEntity,
    onSave: (BookSourceEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(source.name) }
    var url by remember { mutableStateOf(source.url) }
    var searchUrl by remember { mutableStateOf(source.searchUrl ?: source.ruleSearch ?: "") }
    var searchList by remember { mutableStateOf(source.searchListRule ?: "") }
    var searchName by remember { mutableStateOf(source.searchNameRule ?: "") }
    var searchAuthor by remember { mutableStateOf(source.searchAuthorRule ?: "") }
    var searchCover by remember { mutableStateOf(source.searchCoverRule ?: "") }
    var searchResultUrl by remember { mutableStateOf(source.searchUrlRule ?: "") }
    var bookName by remember { mutableStateOf(source.ruleBookName ?: "") }
    var bookAuthor by remember { mutableStateOf(source.ruleBookAuthor ?: "") }
    var bookIntro by remember { mutableStateOf(source.ruleBookIntro ?: "") }
    var bookCover by remember { mutableStateOf(source.ruleCoverUrl ?: "") }
    var chapterList by remember { mutableStateOf(source.ruleChapterList ?: "") }
    var chapterName by remember { mutableStateOf(source.ruleChapterName ?: "text") }
    var chapterUrl by remember { mutableStateOf(source.ruleChapterUrl ?: "href") }
    var content by remember { mutableStateOf(source.ruleContent ?: "") }
    var header by remember { mutableStateOf(source.header ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (source.id == 0L) "新增书源" else "编辑书源") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("书源 URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = searchUrl, onValueChange = { searchUrl = it }, label = { Text("搜索 URL 模板 (含 {{key}})") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = searchList, onValueChange = { searchList = it }, label = { Text("搜索结果列表规则") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = searchName, onValueChange = { searchName = it }, label = { Text("书名规则 @text") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = searchAuthor, onValueChange = { searchAuthor = it }, label = { Text("作者规则 @text") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = searchCover, onValueChange = { searchCover = it }, label = { Text("封面规则 @src") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = searchResultUrl, onValueChange = { searchResultUrl = it }, label = { Text("书页 URL 规则 @href") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = bookName, onValueChange = { bookName = it }, label = { Text("详情-书名") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = bookAuthor, onValueChange = { bookAuthor = it }, label = { Text("详情-作者") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = bookIntro, onValueChange = { bookIntro = it }, label = { Text("详情-简介") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = bookCover, onValueChange = { bookCover = it }, label = { Text("详情-封面") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = chapterList, onValueChange = { chapterList = it }, label = { Text("目录列表规则") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = chapterName, onValueChange = { chapterName = it }, label = { Text("目录-章节名规则") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = chapterUrl, onValueChange = { chapterUrl = it }, label = { Text("目录-章节URL规则") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("正文规则 (建议带 @text)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = header, onValueChange = { header = it }, label = { Text("自定义 header JSON (可选)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(source.copy(
                    name = name,
                    url = url,
                    searchUrl = searchUrl,
                    ruleSearch = searchUrl,
                    searchListRule = searchList,
                    searchNameRule = searchName,
                    searchAuthorRule = searchAuthor,
                    searchCoverRule = searchCover,
                    searchUrlRule = searchResultUrl,
                    ruleBookName = bookName,
                    ruleBookAuthor = bookAuthor,
                    ruleBookIntro = bookIntro,
                    ruleCoverUrl = bookCover,
                    ruleChapterList = chapterList,
                    ruleChapterName = chapterName,
                    ruleChapterUrl = chapterUrl,
                    ruleContent = content,
                    header = header.ifBlank { null }
                ))
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
