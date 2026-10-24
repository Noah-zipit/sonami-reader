package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): the hidden extension manager.
// Normal Browse never shows Sources / Extensions / Migrate. This screen is
// only reachable after the hidden unlock (7 taps on the Sonami moon mark in
// More, or 7 taps on the version row in About Us). It reuses Mihon's own
// extensions tab, including install, update, uninstall and extension stores,
// so no extension-install code was rewritten.

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionsViewModel
import eu.kanade.tachiyomi.ui.browse.extension.extensionsTab
import tachiyomi.i18n.MR

class SonamiHiddenExtensionsScreen : Screen() {

    @Composable
    override fun Content() {
        val extensionsViewModel = metroViewModel<ExtensionsViewModel>()
        val extensionsSearchQuery by extensionsViewModel.searchQuery.collectAsStateWithLifecycle()
        val tabs = listOf(extensionsTab(extensionsViewModel))
        val state = rememberPagerState { tabs.size }
        TabbedScreen(
            titleRes = MR.strings.label_extensions,
            tabs = tabs,
            state = state,
            searchQuery = extensionsSearchQuery,
            onChangeSearchQuery = extensionsViewModel::search,
        )
    }
}
