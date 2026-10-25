package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): first-run setup support.
// Two jobs, both idempotent so reopening Setup is safe:
//  - Seed the Keiyoushi extension store (Fix A). Mihon trusts an extension
//    only if its signature matches a registered store's signing key (or the
//    user manually trusted that exact package). Sonami never registered a
//    store, so sideloaded Keiyoushi APKs landed in "Not loaded" /
//    "Untrusted" and never appeared in Browse or search. Registering the
//    store once makes the Famous 5 auto-trust: the signing key comes from
//    the fetched index itself, never hardcoded here.
//  - Pin the Famous 5 sources (Fix D), so Mihon's default pinned-only
//    global search also covers them. Only sources actually loaded are
//    pinned; anything still unloaded (e.g. untrusted before Fix A) is
//    skipped silently, never invented.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import eu.kanade.domain.source.service.SourcePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.domain.extension.interactor.AddExtensionStore
import mihon.domain.extension.interactor.GetExtensionStores
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.domain.source.service.SourceManager

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class SonamiSetupViewModel(
    private val getExtensionStores: GetExtensionStores,
    private val addExtensionStore: AddExtensionStore,
    private val extensionManager: eu.kanade.tachiyomi.extension.ExtensionManager,
    private val preferences: SourcePreferences,
    private val sourceManager: SourceManager,
) : ViewModel() {

    private val _storeState = MutableStateFlow<StoreState>(StoreState.Checking)
    val storeState: StateFlow<StoreState> = _storeState.asStateFlow()

    /**
     * Registers the Keiyoushi store exactly once: if a store with this
     * index URL already exists, this is a no-op that reports Added.
     * Safe to call on every Setup open and on retry after a failure.
     */
    fun ensureKeiyoushiStore() {
        if (_storeState.value == StoreState.Adding) return
        viewModelScope.launch(Dispatchers.IO) {
            _storeState.update { StoreState.Adding }
            try {
                val alreadyThere = getExtensionStores.get()
                    .any { it.indexUrl == KEIYOUSHI_INDEX_URL }
                if (alreadyThere) {
                    _storeState.update { StoreState.Added }
                    return@launch
                }
                addExtensionStore(KEIYOUSHI_INDEX_URL)
                    .onSuccess {
                        // Refresh the available-extension listing so the
                        // newly trusted extensions load without a restart.
                        extensionManager.findAvailableExtensions()
                        _storeState.update { StoreState.Added }
                    }
                    .onFailure {
                        _storeState.update { StoreState.Failed }
                    }
            } catch (e: Exception) {
                _storeState.update { StoreState.Failed }
            }
        }
    }

    /**
     * Pins every currently loaded Famous 5 source that is not already
     * pinned. Called from the Setup flow only, never from Browse, so a
     * user unpin stays unpinned until they return to Setup.
     */
    fun pinFamous5() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loadedIds = sourceManager.getAll()
                    .filter { it.name in SonamiBrowseViewModel.FAMOUS_5_ORDER }
                    .map { "${it.id}" }
                if (loadedIds.isEmpty()) return@launch
                preferences.pinnedSources.getAndSet { pinned -> pinned + loadedIds }
            } catch (e: Exception) {
                // Pinning is a convenience, never a setup blocker.
            }
        }
    }

    sealed interface StoreState {
        data object Checking : StoreState
        data object Adding : StoreState
        data object Added : StoreState
        data object Failed : StoreState
    }

    companion object {
        const val KEIYOUSHI_INDEX_URL =
            "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.json"
    }
}
