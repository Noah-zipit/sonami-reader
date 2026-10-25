package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): the Browse tab's popular-titles grid.
// This is the grid from Mihon's BrowseSourceScreen, embedded in the tab:
// Popular/Latest/Filter chips above a cover grid for ONE source, tapping a
// title opens its detail page so it can be added to the library, long-press
// adds it directly unless it is already in the library (then its detail
// page opens, where it can be removed or migrated).
//
// SOURCE SWITCHING: BrowseSourceViewModel is created once per assisted
// sourceId and never reloads for a different source, so switching sources
// must give it a fresh ViewModel. This composable therefore provides a
// dedicated ViewModelStoreOwner per sourceId (cleared on dispose) and keys
// the subtree on sourceId. If the assisted-ViewModel helper ever stops
// honouring LocalViewModelStoreOwner, switching would silently keep showing
// the previous source's titles — that is the one behaviour a device build
// must check (see report).

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import eu.kanade.presentation.browse.BrowseSourceContent
import tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceViewModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceViewModel.Listing
import eu.kanade.tachiyomi.ui.browse.source.browse.SourceFilterDialog
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.webview.WebViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.Verified
import mihon.icons.materialsymbols.roundedfilled.Favorite
import mihon.presentation.core.util.collectAsLazyPagingItems
import tachiyomi.core.common.Constants
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.interactor.GetRemoteManga
import tachiyomi.domain.source.model.StubSource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen

@Composable
fun SonamiSourceGrid(
    sourceId: Long,
    snackbarHostState: SnackbarHostState,
    onMangaClick: (Manga) -> Unit,
) {
    val storeOwner = remember(sourceId) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(storeOwner) {
        onDispose { storeOwner.viewModelStore.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
        key(sourceId) {
            SonamiSourceGridContent(
                sourceId = sourceId,
                snackbarHostState = snackbarHostState,
                onMangaClick = onMangaClick,
            )
        }
    }
}

@Composable
private fun SonamiSourceGridContent(
    sourceId: Long,
    snackbarHostState: SnackbarHostState,
    onMangaClick: (Manga) -> Unit,
) {
    val viewModel =
        assistedMetroViewModel<BrowseSourceViewModel, BrowseSourceViewModel.Factory> {
            create(sourceId = sourceId, listingQuery = GetRemoteManga.QUERY_POPULAR)
        }
    val state by viewModel.state.collectAsState()

    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current

    val source = state.source
    if (source == null) {
        LoadingScreen()
        return
    }
    if (source is StubSource) {
        EmptyScreen(
            message = stringResource(MR.strings.source_not_installed, source.toString()),
        )
        return
    }

    val onWebViewClick = {
        val httpSource = source as? HttpSource
        if (httpSource != null) {
            navigator.push(
                WebViewScreen(
                    url = httpSource.getHomeUrl(),
                    initialTitle = httpSource.name,
                    sourceId = httpSource.id,
                ),
            )
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.padding.small),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
        ) {
            FilterChip(
                selected = state.listing == Listing.Popular,
                onClick = {
                    viewModel.resetFilters()
                    viewModel.setListing(Listing.Popular)
                },
                leadingIcon = {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                label = { Text(text = stringResource(MR.strings.popular)) },
            )
            if (source.supportsLatest) {
                FilterChip(
                    selected = state.listing == Listing.Latest,
                    onClick = {
                        viewModel.resetFilters()
                        viewModel.setListing(Listing.Latest)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                    label = { Text(text = stringResource(MR.strings.latest)) },
                )
            }
            if (state.filters.isNotEmpty()) {
                FilterChip(
                    selected = state.listing is Listing.Search,
                    onClick = viewModel::openFilterSheet,
                    leadingIcon = {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                    label = { Text(text = stringResource(MR.strings.action_filter)) },
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            BrowseSourceContent(
                source = source,
                mangaList = viewModel.mangaPagerFlowFlow.collectAsLazyPagingItems(),
                columns = viewModel.getColumnsPreference(LocalConfiguration.current.orientation),
                displayMode = viewModel.displayMode,
                snackbarHostState = snackbarHostState,
                contentPadding = PaddingValues(),
                onWebViewClick = onWebViewClick,
                onHelpClick = { uriHandler.openUri(Constants.URL_HELP) },
                onLocalSourceHelpClick = { uriHandler.openUri(LocalSource.HELP_URL) },
                onMangaClick = onMangaClick,
                onMangaLongClick = { manga ->
                    scope.launch {
                        val duplicates = withContext(Dispatchers.IO) {
                            viewModel.getDuplicateLibraryManga(manga)
                        }
                        if (manga.favorite || duplicates.isNotEmpty()) {
                            // Already in the library: open its detail page,
                            // where it can be removed or migrated. The tab
                            // grid does not render those dialogs.
                            navigator.push(MangaScreen(manga.id, true))
                        } else {
                            viewModel.addFavorite(manga)
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
            )
        }
    }

    if (state.dialog is BrowseSourceViewModel.Dialog.Filter) {
        SourceFilterDialog(
            onDismissRequest = { viewModel.setDialog(null) },
            filters = state.filters,
            onReset = { viewModel.resetFilters() },
            onFilter = { viewModel.search(filters = state.filters) },
            onUpdate = viewModel::setFilters,
        )
    }
}
