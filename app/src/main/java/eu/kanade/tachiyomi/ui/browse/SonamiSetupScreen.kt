package eu.kanade.tachiyomi.ui.browse

// SONAMI (fork of Mihon, Apache 2.0): Famous 5 first-run setup.
// MECHANISM, stated honestly: Mihon extensions are separate APKs and Android
// does not allow an app to silently install other APKs. Sonami therefore
// bundles the five Keiyoushi extension APKs in its own assets
// (assets/sonami_extensions/) and this screen installs each one through the
// normal system installer, which asks the user to confirm every install.
// Once installed, the extensions behave exactly like Mihon extensions and
// appear in unified Browse. Installed state is re-checked every time this
// screen is opened.
// Shipped set (Keiyoushi index checked 2026-10-24): Asura Scans,
// MangaDex, MangaFire, MangaKakalot, ManhuaPlus. Swap from the original
// "famous 5" list: Comick has no main Keiyoushi extension (only an
// "Unoriginal" mirror) so MangaFire takes its place. MangaKakalot was
// verified in the live Keiyoushi index (v1.6.26); MangaPark was dropped
// as precarious in community notes.
//
// TRUST (Fix A): opening this screen seeds the Keiyoushi extension store
// via SonamiSetupViewModel, so the bundled extensions are trusted by their
// store signature instead of landing in "Not loaded" / "Untrusted".
// INSTALL PERMISSION (Fix B): if the system "install unknown apps" grant
// for Sonami is off, a card at the top opens the system setting; the
// grant itself is the user's choice in Settings, Sonami cannot take it.
// FLOW (Fix C): installed state is re-checked on resume (returning from
// the installer), the next not-installed extension is offered as the
// next step (never auto-launched, so a declined install cannot loop),
// and when all five are installed a "go to Browse" action appears.

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.rememberRequestPackageInstallsPermissionState
import eu.kanade.tachiyomi.util.system.isPackageInstalled
import eu.kanade.tachiyomi.util.system.launchRequestPackageInstallsPermission
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Download
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import java.io.File

data class SonamiBundledExtension(
    val name: String,
    val pkg: String,
    val version: String,
    val assetFile: String,
)

val SONAMI_BUNDLED_EXTENSIONS = listOf(
    SonamiBundledExtension("Asura Scans", "eu.kanade.tachiyomi.extension.en.asurascans", "1.6.69", "asurascans.apk"),
    SonamiBundledExtension("MangaDex", "eu.kanade.tachiyomi.extension.all.mangadex", "1.6.0", "mangadex.apk"),
    SonamiBundledExtension("MangaFire", "eu.kanade.tachiyomi.extension.all.mangafire", "1.6.34", "mangafire.apk"),
    SonamiBundledExtension("MangaKakalot", "eu.kanade.tachiyomi.extension.en.mangakakalot", "1.6.26", "mangakakalot.apk"),
    SonamiBundledExtension("ManhuaPlus", "eu.kanade.tachiyomi.extension.en.manhuaplus", "1.6.63", "manhuaplus.apk"),
)

class SonamiSetupScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val setupViewModel = metroViewModel<SonamiSetupViewModel>()
        val storeState by setupViewModel.storeState.collectAsStateWithLifecycle()

        // SONAMI (Fix A): seed the Keiyoushi store once; the ViewModel
        // makes this idempotent, so reopening Setup never re-adds it.
        LaunchedEffect(Unit) {
            setupViewModel.ensureKeiyoushiStore()
        }

        // SONAMI (Fix B): re-checked on resume by the helper itself.
        // initialValue = true avoids flashing the card before the first
        // resume check, matching Mihon's Extensions screen.
        val installGranted = rememberRequestPackageInstallsPermissionState(initialValue = true)

        // SONAMI (Fix C): re-check installed state on resume, i.e. when
        // returning from the system installer. The old refreshTick only
        // fired at tap time, before the install had completed.
        val lifecycleOwner = LocalLifecycleOwner.current
        var resumeTick by remember { mutableStateOf(0) }
        DisposableEffect(lifecycleOwner) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    resumeTick += 1
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        @Suppress("UNUSED_EXPRESSION")
        resumeTick

        val installedByPkg = SONAMI_BUNDLED_EXTENSIONS.associate { ext ->
            ext.pkg to context.isPackageInstalled(ext.pkg)
        }
        val installedCount = installedByPkg.values.count { it }
        val allInstalled = installedCount == SONAMI_BUNDLED_EXTENSIONS.size
        val nextToInstall = SONAMI_BUNDLED_EXTENSIONS.firstOrNull { installedByPkg[it.pkg] != true }

        // SONAMI (Fix D): pin any loaded Famous 5 source, in this flow
        // only. Unloaded sources are skipped inside the ViewModel.
        LaunchedEffect(installedCount) {
            if (installedCount > 0) {
                setupViewModel.pinFamous5()
            }
        }

        fun install(ext: SonamiBundledExtension) {
            try {
                val outDir = File(context.cacheDir, "sonami_extensions").apply { mkdirs() }
                val outFile = File(outDir, ext.assetFile)
                context.assets.open("sonami_extensions/${ext.assetFile}").use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    outFile,
                )
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Install failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        Scaffold(
            topBar = {
                AppBar(
                    title = "Famous 5 sources",
                    navigateUp = navigator::pop,
                )
            },
        ) { paddingValues ->
            ScrollbarLazyColumn(contentPadding = paddingValues) {
                // SONAMI (Fix B): install permission card, only when off.
                if (!installGranted) {
                    item {
                        Card(
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.padding.medium,
                                vertical = MaterialTheme.padding.small,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(MaterialTheme.padding.medium)) {
                                Text(
                                    text = "Sonami needs permission to install extensions",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "Turn on \"Allow from this source\" for Sonami " +
                                        "in the system setting that opens, then come back here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = MaterialTheme.padding.small),
                                )
                                Button(
                                    onClick = { context.launchRequestPackageInstallsPermission() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = MaterialTheme.padding.small),
                                ) {
                                    Text("Open system setting")
                                }
                            }
                        }
                    }
                }

                // SONAMI (Fix A): store-seeding state. Silent when added,
                // which is the normal state after the first open.
                when (storeState) {
                    SonamiSetupViewModel.StoreState.Checking,
                    SonamiSetupViewModel.StoreState.Adding -> {
                        item {
                            Text(
                                text = "Adding Keiyoushi store so the Famous 5 " +
                                    "trust automatically...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(MaterialTheme.padding.medium),
                            )
                        }
                    }
                    SonamiSetupViewModel.StoreState.Failed -> {
                        item {
                            Card(
                                modifier = Modifier.padding(
                                    horizontal = MaterialTheme.padding.medium,
                                    vertical = MaterialTheme.padding.small,
                                ),
                            ) {
                                Column(modifier = Modifier.padding(MaterialTheme.padding.medium)) {
                                    Text(
                                        text = "Could not add the Keiyoushi store. " +
                                            "Extensions may show as Untrusted until it is added.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Button(
                                        onClick = { setupViewModel.ensureKeiyoushiStore() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = MaterialTheme.padding.small),
                                    ) {
                                        Text("Retry adding store")
                                    }
                                    Text(
                                        text = "If this fails, open hidden Extensions " +
                                            "and tap Trust on each extension.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = MaterialTheme.padding.small),
                                    )
                                }
                            }
                        }
                    }
                    SonamiSetupViewModel.StoreState.Added -> {
                        // Normal state: nothing to say.
                    }
                }

                item {
                    Text(
                        text = "Install each source once. Android asks you to confirm " +
                            "every install, Sonami cannot install them silently. " +
                            "After installing, go back to Browse and the sources appear there.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                    )
                }

                // SONAMI (Fix C): sequential next step, never auto-launched.
                if (!allInstalled && nextToInstall != null) {
                    item {
                        Button(
                            onClick = { install(nextToInstall) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.padding.medium),
                        ) {
                            Text("Next: install ${nextToInstall.name}")
                        }
                    }
                }

                // SONAMI (Fix C): all-installed state.
                if (allInstalled) {
                    item {
                        Button(
                            onClick = { navigator.pop() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.padding.medium),
                        ) {
                            Text("All 5 installed — go to Browse")
                        }
                    }
                }

                items(SONAMI_BUNDLED_EXTENSIONS.size) { index ->
                    val ext = SONAMI_BUNDLED_EXTENSIONS[index]
                    val installed = installedByPkg[ext.pkg] == true
                    Card(
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = 2.dp,
                        ),
                    ) {
                        Column {
                            TextPreferenceWidget(
                                title = ext.name,
                                subtitle = if (installed) {
                                    "Installed (${ext.version})"
                                } else {
                                    "Not installed (${ext.version}) — tap to install"
                                },
                                icon = MaterialSymbols.Rounded.Download,
                                onPreferenceClick = {
                                    if (installed) {
                                        Toast.makeText(context, "${ext.name} is already installed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        install(ext)
                                    }
                                },
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "Tip: if an install is declined, just tap it again. " +
                            "New sources beyond these five go through the hidden " +
                            "Extensions manager (More header, 7 taps on the moon mark).",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                    )
                }
            }
        }
    }
}
