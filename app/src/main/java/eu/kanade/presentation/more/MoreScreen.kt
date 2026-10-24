package eu.kanade.presentation.more

// SONAMI (fork of Mihon, Apache 2.0): More screen redesigned so it is not a
// 1:1 Mihon copy. The flat Mihon list is replaced by grouped cards
// (Reading / Library / App), the header is the Sonami moon-and-dragon mark
// with a Sonami title, Support Us is replaced by About Us, and Help is
// removed. Tapping the moon mark 7 times reveals the hidden Extensions row.

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.CloudOff
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Eyeglasses2
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.Storage
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickSupport: () -> Unit,
    onClickAbout: () -> Unit,
    onClickExtensions: (() -> Unit)? = null,
) {
    var markTaps by remember { mutableIntStateOf(0) }
    var extensionsUnlocked by remember { mutableStateOf(false) }

    Scaffold { contentPadding ->
        ScrollbarLazyColumn(contentPadding = contentPadding) {
            item {
                LogoHeader(
                    iconPadding = PaddingValues(vertical = 24.dp),
                    onClick = {
                        markTaps += 1
                        if (markTaps >= 7) extensionsUnlocked = true
                    },
                )
            }
            item {
                Text(
                    text = "Sonami",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = MaterialTheme.padding.small,
                        ),
                )
            }

            item {
                SectionTitle("Reading")
            }
            item {
                Card(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium)) {
                    Column {
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.label_downloaded_only),
                            subtitle = stringResource(MR.strings.downloaded_only_summary),
                            icon = MaterialSymbols.Rounded.CloudOff,
                            checked = downloadedOnly,
                            onCheckedChanged = onDownloadedOnlyChange,
                        )
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.pref_incognito_mode),
                            subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                            icon = MaterialSymbols.Rounded.Eyeglasses2,
                            checked = incognitoMode,
                            onCheckedChanged = onIncognitoModeChange,
                        )
                        val downloadQueueState = downloadQueueStateProvider()
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_download_queue),
                            subtitle = when (downloadQueueState) {
                                DownloadQueueState.Stopped -> null
                                is DownloadQueueState.Paused -> {
                                    val pending = downloadQueueState.pending
                                    if (pending == 0) {
                                        stringResource(MR.strings.paused)
                                    } else {
                                        "${stringResource(MR.strings.paused)} • ${
                                            pluralStringResource(
                                                MR.plurals.download_queue_summary,
                                                count = pending,
                                                pending,
                                            )
                                        }"
                                    }
                                }
                                is DownloadQueueState.Downloading -> {
                                    val pending = downloadQueueState.pending
                                    pluralStringResource(
                                        MR.plurals.download_queue_summary,
                                        count = pending,
                                        pending,
                                    )
                                }
                            },
                            icon = MaterialSymbols.Rounded.Download,
                            onPreferenceClick = onClickDownloadQueue,
                        )
                    }
                }
            }

            item {
                SectionTitle("Library")
            }
            item {
                Card(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium)) {
                    Column {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.categories),
                            icon = MaterialSymbols.AutoMirroredRounded.Label,
                            onPreferenceClick = onClickCategories,
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_stats),
                            icon = MaterialSymbols.Rounded.QueryStats,
                            onPreferenceClick = onClickStats,
                        )
                    }
                }
            }

            item {
                SectionTitle("App")
            }
            item {
                Card(modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium)) {
                    Column {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_data_storage),
                            icon = MaterialSymbols.Rounded.Storage,
                            onPreferenceClick = onClickDataAndStorage,
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_settings),
                            icon = MaterialSymbols.Rounded.Settings,
                            onPreferenceClick = onClickSettings,
                        )
                        // SONAMI: Support Us is replaced by About Us; Help is removed.
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_about_us),
                            icon = MaterialSymbols.Rounded.Info,
                            onPreferenceClick = onClickSupport,
                        )
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.pref_category_about),
                            icon = MaterialSymbols.Rounded.Info,
                            onPreferenceClick = onClickAbout,
                        )
                        if (extensionsUnlocked && onClickExtensions != null) {
                            TextPreferenceWidget(
                                title = stringResource(MR.strings.label_extensions),
                                subtitle = "Hidden Sonami extension manager",
                                icon = MaterialSymbols.Rounded.Book,
                                onPreferenceClick = onClickExtensions,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
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
