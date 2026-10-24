package eu.kanade.presentation.browse

// SONAMI (fork of Mihon, Apache 2.0): unified Browse.
// Replaces Mihon's Browse tabs (Sources / Extensions / Migrate) for normal
// users. There is one screen:
//  - a search box that searches ALL installed sources at once (Mihon's
//    global search) and lets any result be added to the library from its
//    detail page, and
//  - a single list of installed sources (the Famous 5 once installed) whose
//    popular listings open directly, with no per-source management UI.
// Extension management lives only behind the hidden unlock.

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.domain.source.model.Source
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding

@Composable
fun SonamiBrowseScreen(
    installedSources: List<Source>,
    onSearch: (String) -> Unit,
    onClickSource: (Source) -> Unit,
    onClickSetup: () -> Unit,
) {
    var query by remember { mutableStateOf("") }

    Scaffold { contentPadding ->
        ScrollbarLazyColumn(contentPadding = contentPadding) {
            item {
                Card(modifier = Modifier.padding(MaterialTheme.padding.medium)) {
                    Column(modifier = Modifier.padding(MaterialTheme.padding.medium)) {
                        Text(
                            text = "Search every source at once",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "One search across all installed sources. " +
                                "Open any title to add it to your library.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = MaterialTheme.padding.small),
                        )
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Search manhwa, manga, manhua") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = MaterialTheme.padding.medium),
                        )
                        Button(
                            onClick = { if (query.isNotBlank()) onSearch(query) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = MaterialTheme.padding.small),
                        ) {
                            Text("Search")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium)) {
                    Column {
                        TextPreferenceWidget(
                            title = "Famous 5 sources",
                            subtitle = "Asura Scans, MangaDex, MangaFire, MangaPark, ManhuaPlus",
                            icon = MaterialSymbols.Rounded.Book,
                            onPreferenceClick = onClickSetup,
                        )
                    }
                }
            }

            if (installedSources.isEmpty()) {
                item {
                    Text(
                        text = "No sources installed yet. Open Famous 5 sources above " +
                            "and install them once, then they appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                    )
                }
            } else {
                item {
                    Text(
                        text = "All titles",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            start = MaterialTheme.padding.medium,
                            end = MaterialTheme.padding.medium,
                            top = MaterialTheme.padding.medium,
                            bottom = MaterialTheme.padding.small,
                        ),
                    )
                }
                items(installedSources.size) { index ->
                    val source = installedSources[index]
                    Card(
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = 2.dp,
                        ),
                    ) {
                        TextPreferenceWidget(
                            title = source.visualName,
                            subtitle = "Popular titles",
                            icon = MaterialSymbols.Rounded.Search,
                            onPreferenceClick = { onClickSource(source) },
                        )
                    }
                }
            }
        }
    }
}
