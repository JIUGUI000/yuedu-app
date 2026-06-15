package com.legado.lite.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.legado.lite.R
import com.legado.lite.ui.bookshelf.BookshelfScreen
import com.legado.lite.ui.detail.BookDetailScreen
import com.legado.lite.ui.explore.ExploreScreen
import com.legado.lite.ui.reader.ReaderScreen
import com.legado.lite.ui.search.SearchScreen
import com.legado.lite.ui.settings.SettingsScreen
import com.legado.lite.ui.settings.SourcesScreen

object Routes {
    const val SHELF = "shelf"
    const val EXPLORE = "explore"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val SOURCES = "sources"
    const val DETAIL = "detail/{bookId}"
    const val READER = "reader/{bookId}?index={index}"

    fun detail(bookId: Long) = "detail/$bookId"
    fun reader(bookId: Long, index: Int = -1) = "reader/$bookId?index=$index"
}

private data class TabItem(val route: String, val label: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun LegadoAppRoot() {
    val nav = rememberNavController()
    val tabs = listOf(
        TabItem(Routes.SHELF, R.string.tab_bookshelf, Icons.Outlined.LibraryBooks),
        TabItem(Routes.EXPLORE, R.string.tab_explore, Icons.Outlined.AutoStories),
        TabItem(Routes.SETTINGS, R.string.tab_settings, Icons.Outlined.Settings)
    )
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            // 只在主 tab 显示底部导航
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    nav.navigate(tab.route) {
                                        popUpTo(Routes.SHELF) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = nav, startDestination = Routes.SHELF) {
                composable(Routes.SHELF) {
                    BookshelfScreen(
                        onOpenBook = { id -> nav.navigate(Routes.detail(id)) },
                        onOpenSearch = { nav.navigate(Routes.SEARCH) }
                    )
                }
                composable(Routes.EXPLORE) {
                    ExploreScreen(
                        onOpenSearch = { nav.navigate(Routes.SEARCH) },
                        onOpenBook = { id -> nav.navigate(Routes.detail(id)) }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onOpenSources = { nav.navigate(Routes.SOURCES) }
                    )
                }
                composable(Routes.SOURCES) {
                    SourcesScreen(onBack = { nav.popBackStack() })
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        onBack = { nav.popBackStack() },
                        onOpenBook = { id -> nav.navigate(Routes.detail(id)) }
                    )
                }
                composable(Routes.DETAIL) { backEntry ->
                    val bookId = backEntry.arguments?.getString("bookId")?.toLongOrNull() ?: 0L
                    BookDetailScreen(
                        bookId = bookId,
                        onBack = { nav.popBackStack() },
                        onRead = { idx -> nav.navigate(Routes.reader(bookId, idx)) }
                    )
                }
                composable(Routes.READER) { backEntry ->
                    val bookId = backEntry.arguments?.getString("bookId")?.toLongOrNull() ?: 0L
                    val index = backEntry.arguments?.getString("index")?.toIntOrNull() ?: -1
                    ReaderScreen(
                        bookId = bookId,
                        startIndex = index,
                        onBack = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
