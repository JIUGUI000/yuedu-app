package com.legado.lite.ui.explore

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
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legado.lite.R
import com.legado.lite.data.entity.ReadHistoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onOpenSearch: () -> Unit,
    onOpenBook: (Long) -> Unit
) {
    val vm: ExploreViewModel = viewModel()
    val history by vm.history.collectAsState()
    val recent by vm.recent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_explore)) },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    onClick = onOpenSearch,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("搜索想看的书", style = MaterialTheme.typography.titleSmall)
                            Text("跨书源聚合，结果更全", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                Text("最近阅读", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            }
            if (recent.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("暂无最近阅读", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(recent, key = { it.id }) { h ->
                    HistoryRow(h) { onOpenBook(h.bookId) }
                }
            }
            item {
                Text("阅读历史", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            }
            if (history.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("暂无历史", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(history.take(50), key = { it.id }) { h ->
                    HistoryRow(h) { onOpenBook(h.bookId) }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(h: ReadHistoryEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (h.cover.isNullOrBlank()) Icons.Outlined.MenuBook else Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(h.bookName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${h.author ?: "佚名"} · ${h.chapterTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
