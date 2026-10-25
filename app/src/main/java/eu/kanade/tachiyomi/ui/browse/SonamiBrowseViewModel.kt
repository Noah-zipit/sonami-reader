package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): source list for the unified Browse tab.
// Unlike SourcesViewModel (GetEnabledSources), this does NOT apply the
// enabled-languages or disabled-sources filters: Sonami's Famous 5 must show
// in Browse as soon as their extension APKs are loaded, regardless of those
// preferences. SourceManager.sources has no language filter — it is every
// loaded extension source plus Local, which is excluded here.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import eu.kanade.tachiyomi.source.Source
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.source.local.isLocal
import kotlin.time.Duration.Companion.seconds

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class SonamiBrowseViewModel(
    sourceManager: SourceManager,
) : ViewModel() {

    val sources: StateFlow<List<Source>> = sourceManager.sources
        .map { loaded ->
            loaded
                .filterNot { it.isLocal() }
                .sortedWith(
                    compareBy<Source> { FAMOUS_5_ORDER.indexOf(it.name).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
                        .thenBy { it.name.lowercase() },
                )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    companion object {
        // SONAMI: fixed display order for the bundled sources; anything else
        // installed via the hidden extensions manager sorts after them.
        val FAMOUS_5_ORDER = listOf(
            "Asura Scans",
            "MangaDex",
            "MangaFire",
            "MangaKakalot",
            "ManhuaPlus",
        )
    }
}
