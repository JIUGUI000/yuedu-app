package com.legado.lite.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.legado.lite.R
import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.ChapterEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    onBack: () -> Unit,
    onRead: (Int) -> Unit
) {
    val vm: BookDetailViewModel = viewModel()
    val book by vm.book.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val inShelf by vm.inShelf.collectAsState()

    LaunchedEffect(bookId) { vm.load(bookId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.name ?: stringResource(R.string.tab_explore), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        if (loading && book == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val b = book
        if (b == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(error ?: "加载失败")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Header(b, inShelf, onShelfAdd = vm::addToShelf, onShelfRemove = vm::removeFromShelf, onRead = { onRead(b.latestReadChapterIndex) }) }
            item {
                Text("目录（${chapters.size}）",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            items(chapters, key = { it.id }) { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        c.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { onRead(c.index) }) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    }
                }
            }
            if (chapters.isEmpty() && !loading) {
                item {
                    Text(
                        "目录为空或正在加载…",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    b: BookEntity,
    inShelf: Boolean,
    onShelfAdd: () -> Unit,
    onShelfRemove: () -> Unit,
    onRead: () -> Unit
) {
    Row(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(width = 96.dp, height = 130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!b.cover.isNullOrBlank()) {
                AsyncImage(model = b.cover, contentDescription = b.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(40.dp))
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(b.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            if (!b.author.isNullOrBlank()) {
                Text("作者：${b.author}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!b.kind.isNullOrBlank()) {
                Text("类型：${b.kind}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!b.lastChapter.isNullOrBlank()) {
                Text("最新：${b.lastChapter}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            Text("书源：${b.originName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
    if (!b.intro.isNullOrBlank()) {
        Text(b.intro,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onRead, modifier = Modifier.weight(1f)) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
            Spacer(Modifier.size(4.dp))
            Text("开始阅读")
        }
        if (inShelf) {
            OutlinedButton(onClick = onShelfRemove, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_remove_from_shelf))
            }
        } else {
            OutlinedButton(onClick = onShelfAdd, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_add_to_shelf))
            }
        }
    }
}
