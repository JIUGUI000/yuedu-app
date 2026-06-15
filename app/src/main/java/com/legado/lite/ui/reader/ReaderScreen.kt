package com.legado.lite.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legado.lite.R
import com.legado.lite.tts.TtsController
import com.legado.lite.tts.TtsPlaybackService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    startIndex: Int,
    onBack: () -> Unit
) {
    val vm: ReaderViewModel = viewModel()
    val book by vm.book.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val index by vm.currentIndex.collectAsState()
    val content by vm.content.collectAsState()
    val prefs by vm.prefs.collectAsState()
    val chromeVisible by vm.chromeVisible.collectAsState()
    val loading by vm.loading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(bookId, startIndex) { vm.load(bookId, startIndex) }

    val bg = when (prefs.bgColor) {
        "green" -> Color(0xFFC7E0C7)
        "black" -> Color(0xFF1B1B1B)
        "yellow" -> Color(0xFFF5EFD6)
        else -> Color(0xFFFFFFFF)
    }
    val fg = when (prefs.bgColor) {
        "black" -> Color(0xFFC7C7C7)
        "green" -> Color(0xFF2A3B2A)
        "yellow" -> Color(0xFF5C4B2A)
        else -> Color(0xFF1B1B1B)
    }
    val ttsState by TtsController.state.collectAsState()
    val isPlaying = ttsState == TtsController.State.PLAYING

    Scaffold(
        containerColor = bg,
        topBar = {
            if (chromeVisible) {
                TopAppBar(
                    title = { Text(book?.name ?: "", color = fg) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = fg)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val ch = chapters.getOrNull(index) ?: return@IconButton
                            if (isPlaying) {
                                TtsController.stop()
                                TtsPlaybackService.stop(context)
                            } else {
                                TtsController.speakChapter(
                                    context = context,
                                    chapterTitle = ch.title,
                                    text = content
                                ) {
                                    vm.next()
                                }
                                TtsPlaybackService.startPlay(context, ch.title)
                            }
                        }) {
                            Icon(if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null, tint = fg)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, titleContentColor = fg)
                )
            }
        },
        bottomBar = {
            if (chromeVisible) {
                ReaderBottomBar(
                    progress = if (chapters.isEmpty()) 0f else (index + 1f) / chapters.size,
                    chapterTitle = chapters.getOrNull(index)?.title ?: "",
                    fg = fg,
                    onPrev = vm::prev,
                    onNext = vm::next
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bg)
                .pointerInput(prefs.pageMode) {
                    detectTapGestures(
                        onTap = { offset ->
                            val w = size.width
                            when {
                                offset.x < w * 0.33f -> vm.prev()
                                offset.x > w * 0.66f -> vm.next()
                                else -> vm.toggleChrome()
                            }
                        }
                    )
                }
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = fg)
                }
            } else {
                when (prefs.pageMode) {
                    "slide" -> SlidePage(
                        chapterTitle = chapters.getOrNull(index)?.title ?: "",
                        text = content,
                        textColor = fg,
                        fontSize = prefs.fontSize,
                        lineSpacing = prefs.lineSpacing,
                        paragraphSpacing = prefs.paragraphSpacing
                    )
                    else -> CoverPage(
                        chapterTitle = chapters.getOrNull(index)?.title ?: "",
                        text = content,
                        textColor = fg,
                        fontSize = prefs.fontSize,
                        lineSpacing = prefs.lineSpacing,
                        paragraphSpacing = prefs.paragraphSpacing
                    )
                }
            }
        }
    }
}

@Composable
private fun SlidePage(
    chapterTitle: String,
    text: String,
    textColor: Color,
    fontSize: Int,
    lineSpacing: Float,
    paragraphSpacing: Int
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        item {
            Text(
                chapterTitle,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        item {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * lineSpacing).sp
            )
            Spacer(Modifier.height(paragraphSpacing.dp))
        }
    }
}

@Composable
private fun CoverPage(
    chapterTitle: String,
    text: String,
    textColor: Color,
    fontSize: Int,
    lineSpacing: Float,
    paragraphSpacing: Int
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text(
            chapterTitle,
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * lineSpacing).sp,
            modifier = Modifier.padding(bottom = paragraphSpacing.dp)
        )
    }
}

@Composable
private fun ReaderBottomBar(
    progress: Float,
    chapterTitle: String,
    fg: Color,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(color = fg.copy(alpha = 0.05f)) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Slider(value = progress.coerceIn(0f, 1f), onValueChange = { /* 跳转 */ })
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrev) { Icon(Icons.Outlined.SkipPrevious, contentDescription = null, tint = fg) }
                Text(chapterTitle, color = fg, maxLines = 1)
                IconButton(onClick = onNext) { Icon(Icons.Outlined.SkipNext, contentDescription = null, tint = fg) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(onClick = { /* TODO: 字号设置面板 */ }) { Icon(Icons.Outlined.FormatSize, contentDescription = null, tint = fg) }
                IconButton(onClick = { /* TODO: 亮度 */ }) { Icon(Icons.Outlined.Brightness6, contentDescription = null, tint = fg) }
            }
        }
    }
}
