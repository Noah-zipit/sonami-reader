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
// MangaDex, MangaFire, MangaPark, ManhuaPlus. Swaps from the original
// "famous 5" list: Comick has no main Keiyoushi extension (only an
// "Unoriginal" mirror) so MangaFire takes its place, and MangaKakalot has
// no Keiyoushi extension at all so MangaPark takes its place.

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.util.system.isPackageInstalled
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
    SonamiBundledExtension("MangaPark", "eu.kanade.tachiyomi.extension.en.mangaparktv", "1.6.55", "mangapark.apk"),
    SonamiBundledExtension("ManhuaPlus", "eu.kanade.tachiyomi.extension.en.manhuaplus", "1.6.63", "manhuaplus.apk"),
)

class SonamiSetupScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        var refreshTick by remember { mutableStateOf(0) }

        Scaffold(
            topBar = {
                AppBar(
                    title = "Famous 5 sources",
                    navigateUp = navigator::pop,
                )
            },
        ) { paddingValues ->
            ScrollbarLazyColumn(contentPadding = paddingValues) {
                item {
                    Text(
                        text = "Install each source once. Android asks you to confirm " +
                            "every install, Sonami cannot install them silently. " +
                            "After installing, go back to Browse and the sources appear there.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                    )
                }
                items(SONAMI_BUNDLED_EXTENSIONS.size) { index ->
                    val ext = SONAMI_BUNDLED_EXTENSIONS[index]
                    // refreshTick forces a re-check after returning from the installer.
                    @Suppress("UNUSED_EXPRESSION")
                    refreshTick
                    val installed = context.isPackageInstalled(ext.pkg)
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
                                            refreshTick += 1
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Install failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
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
