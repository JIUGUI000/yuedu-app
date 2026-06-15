package com.legado.lite.ui.search

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.legado.lite.R
import com.legado.lite.domain.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenBook: (Long) -> Unit
) {
    val vm: SearchViewModel = viewModel()
    val keyword by vm.keyword.collectAsState()
    val results by vm.results.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val history by vm.history.collectAsState()
    val errors by vm.errors.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_search)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = vm::setKeyword,
                    placeholder = { Text(stringResource(R.string.hint_search_book)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { vm.search() }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            if (history.isNotEmpty() && results.isEmpty() && !loading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    history.take(8).forEach { h ->
                        AssistChip(
                            onClick = { vm.setKeyword(h.keyword); vm.search() },
                            label = { Text(h.keyword) },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            if (results.isEmpty() && !loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (errors.isNotEmpty()) {
                        Text("部分书源失败：${errors.joinToString("；")}", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(stringResource(R.string.hint_no_search_result), color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results, key = { it.key }) { r ->
                        SearchResultItem(r) {
                            vm.addToShelfAndOpen(r) { bookId -> onOpenBook(bookId) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(r: SearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 76.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!r.cover.isNullOrBlank()) {
                    AsyncImage(model = r.cover, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.align(Alignment.Center))
                }
            }
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(r.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${r.author.ifEmpty { "佚名" }} · ${r.sourceName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (!r.lastChapter.isNullOrBlank()) {
                    Text("最新：${r.lastChapter}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                if (!r.intro.isNullOrBlank()) {
                    Text(r.intro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
