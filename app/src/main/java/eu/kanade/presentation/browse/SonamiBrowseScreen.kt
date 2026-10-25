package eu.kanade.presentation.browse

// SONAMI (fork of Mihon, Apache 2.0): unified Browse tab.
// Replaces Mihon's Browse tabs (Sources / Extensions / Migrate) for normal
// users. The tab is, top to bottom:
//  - a compact search field that searches ALL installed sources at once
//    (Mihon's global search with the All filter; any result opens its detail
//    page, where it can be added to the library),
//  - a "Famous 5 sources" row that opens the one-time setup screen,
//  - a horizontally scrollable row of source chips, and
//  - the selected source's Popular titles grid (the grid slot, supplied by
//    the Browse tab, which owns the per-source ViewModel).
// The source list comes from all loaded extension sources, not the
// enabled-only list, so installed sources cannot vanish from Browse.
// Extension management lives only behind the hidden unlock.

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.source.Source
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding

@Composable
fun SonamiBrowseScreen(
    installedSources: List<Source>,
    selectedSource: Source?,
    onSelectSource: (Source) -> Unit,
    onSearch: (String) -> Unit,
    onClickSetup: () -> Unit,
    grid: @Composable (source: Source, snackbarHostState: SnackbarHostState) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.padding.medium,
                        vertical = MaterialTheme.padding.small,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search manhwa, manga, manhua") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { if (query.isNotBlank()) onSearch(query) },
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Search,
                        contentDescription = "Search",
                    )
                }
            }

            Card(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.padding.medium,
                    vertical = MaterialTheme.padding.small,
                ),
            ) {
                TextPreferenceWidget(
                    title = "Famous 5 sources",
                    subtitle = "Asura Scans, MangaDex, MangaFire, MangaKakalot, ManhuaPlus",
                    icon = MaterialSymbols.Rounded.Book,
                    onPreferenceClick = onClickSetup,
                )
            }

            if (installedSources.isEmpty()) {
                Text(
                    text = "No sources installed yet. Open Famous 5 sources above " +
                        "and install them once, then they appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = MaterialTheme.padding.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    installedSources.forEach { source ->
                        FilterChip(
                            selected = source.id == selectedSource?.id,
                            onClick = { onSelectSource(source) },
                            label = { Text(source.name) },
                        )
                    }
                }

                val current = selectedSource ?: installedSources.first()
                Box(modifier = Modifier.weight(1f)) {
                    grid(current, snackbarHostState)
                }
            }
        }
    }
}
