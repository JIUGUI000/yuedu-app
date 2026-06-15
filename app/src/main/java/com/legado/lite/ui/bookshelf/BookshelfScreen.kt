package com.legado.lite.ui.bookshelf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.legado.lite.R
import com.legado.lite.data.entity.BookEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    onOpenBook: (Long) -> Unit,
    onOpenSearch: () -> Unit
) {
    val vm: BookshelfViewModel = viewModel()
    val books by vm.books.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_bookshelf)) },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.action_search))
                    }
                }
            )
        }
    ) { padding ->
        if (books.isEmpty()) {
            EmptyShelf(padding = padding)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    ShelfItem(
                        book = book,
                        onClick = { onOpenBook(book.id) },
                        onTogglePin = { vm.togglePin(book.id, !book.pinned) },
                        onRemove = { vm.removeFromShelf(book.id) },
                        onDelete = { vm.delete(book.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyShelf(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.hint_no_book_in_shelf), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ShelfItem(
    book: BookEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRemove: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!book.cover.isNullOrBlank()) {
                AsyncImage(
                    model = book.cover,
                    contentDescription = book.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(36.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            if (book.pinned) {
                Icon(
                    Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                        .padding(3.dp)
                )
            }
            if (book.latestReadProgress > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(book.latestReadProgress.coerceIn(0f, 1f))
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(2.dp))
            Box {
                Text("⋮", modifier = Modifier.clickable { menuOpen = true }.padding(horizontal = 4.dp))
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(if (book.pinned) "取消置顶" else "置顶") },
                        onClick = { menuOpen = false; onTogglePin() },
                        leadingIcon = { Icon(Icons.Outlined.PushPin, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_remove_from_shelf)) },
                        onClick = { menuOpen = false; onRemove() },
                        leadingIcon = { Icon(Icons.Outlined.History, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete)) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
        if (!book.author.isNullOrBlank()) {
            Text(
                book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
