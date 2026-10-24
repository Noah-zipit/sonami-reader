package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): unified Browse tab.
// Mihon's Browse had three tabs — Sources, Extensions, Migrate — all removed
// from normal use. This tab is a single Sonami screen: one search across all
// installed sources, plus the installed sources' popular listings. Extension
// and migration management live only behind the hidden unlock.

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.browse.SonamiBrowseScreen
import eu.kanade.presentation.browse.SourceUiModel
import eu.kanade.presentation.util.Tab
import eu.kanade.presentation.util.rememberAnimatedVectorPainter
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.SourcesViewModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import tachiyomi.domain.source.interactor.GetRemoteManga
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
        val sourcesViewModel = metroViewModel<SourcesViewModel>()
        val sourcesState by sourcesViewModel.state.collectAsStateWithLifecycle()
        val installedSources = sourcesState.items
            .filterIsInstance<SourceUiModel.Item>()
            .map { it.source }

        SonamiBrowseScreen(
            installedSources = installedSources,
            onSearch = { query -> navigator.push(GlobalSearchScreen(query)) },
            onClickSource = { source ->
                navigator.push(BrowseSourceScreen(source.id, GetRemoteManga.QUERY_POPULAR))
            },
            onClickSetup = { navigator.push(SonamiSetupScreen()) },
        )

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
