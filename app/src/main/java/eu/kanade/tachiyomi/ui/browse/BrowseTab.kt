package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): unified Browse tab.
// Mihon's Browse had three tabs — Sources, Extensions, Migrate — all removed
// from normal use. This tab is a single Sonami screen: a compact search
// across ALL installed sources, source chips, and the selected source's
// Popular titles grid. Extension and migration management live only behind
// the hidden unlock.

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.browse.SonamiBrowseScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.presentation.util.rememberAnimatedVectorPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object BrowseTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val enter = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_enter)
            val exit = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_exit)
            return TabOptions(
                index = 3u,
                title = stringResource(MR.strings.browse),
                icon = rememberAnimatedVectorPainter(enter, exit, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(GlobalSearchScreen())
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        // SONAMI: all loaded extension sources (no enabled-language filter),
        // Famous 5 first. See SonamiBrowseViewModel.
        val browseViewModel = metroViewModel<SonamiBrowseViewModel>()
        val installedSources by browseViewModel.sources.collectAsStateWithLifecycle()
        var selectedSourceId by remember { mutableStateOf<Long?>(null) }
        // Falls back to the first source (Asura, when installed) whenever
        // the chosen source is not loaded, e.g. just after install.
        val selectedSource = installedSources.find { it.id == selectedSourceId }
            ?: installedSources.firstOrNull()

        SonamiBrowseScreen(
            installedSources = installedSources,
            selectedSource = selectedSource,
            onSelectSource = { selectedSourceId = it.id },
            // SONAMI: searchAllSources = true, so the search is not limited
            // to pinned sources (the Famous 5 are not pinned).
            onSearch = { query -> navigator.push(GlobalSearchScreen(query, searchAllSources = true)) },
            onClickSetup = { navigator.push(SonamiSetupScreen()) },
        ) { source, snackbarHostState ->
            SonamiSourceGrid(
                sourceId = source.id,
                snackbarHostState = snackbarHostState,
                onMangaClick = { navigator.push(MangaScreen(it.id, true)) },
            )
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
