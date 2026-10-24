package mihon.feature.support

// SONAMI (fork of Mihon, Apache 2.0): replaces Mihon's Support Us screen.
// This is the "About Us" screen: information about the person who built
// Sonami, plus Sonami's own links. Mihon donation/Discord links are gone.
// Tapping the version row 7 times reveals the hidden Extensions row.

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.LogoHeader
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.ui.browse.SonamiHiddenExtensionsScreen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.simpleicons.Github
import mihon.icons.simpleicons.SimpleIcons
import mihon.icons.simpleicons.X
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

class SonamiAboutScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        var versionTaps by remember { mutableIntStateOf(0) }
        var extensionsUnlocked by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(MR.strings.label_about_us),
                    navigateUp = navigator::pop,
                )
            },
        ) { paddingValues ->
            ScrollbarLazyColumn(contentPadding = paddingValues) {
                item {
                    LogoHeader(iconPadding = PaddingValues(vertical = 24.dp))
                }
                item {
                    Text(
                        text = "Sonami is a manga, manhwa and manhua reader built by Ashar Qaisar, " +
                            "Full-Stack Developer and AI Integration Engineer from Gujranwala, Pakistan. " +
                            "Founder of Elevate Mavens. Contract AI Engineer at Drytis.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = MaterialTheme.padding.small,
                        ),
                    )
                }
                item {
                    Text(
                        text = "Sonami is built on Mihon, open source under Apache 2.0.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = MaterialTheme.padding.small,
                        ),
                    )
                }
                item {
                    Card(modifier = Modifier.padding(MaterialTheme.padding.medium)) {
                        Column {
                            TextPreferenceWidget(
                                title = "Portfolio",
                                subtitle = "asharxyz.vercel.app",
                                icon = MaterialSymbols.Rounded.Public,
                                onPreferenceClick = { uriHandler.openUri("https://asharxyz.vercel.app") },
                            )
                            TextPreferenceWidget(
                                title = "GitHub",
                                subtitle = "github.com/noah-zipit",
                                icon = SimpleIcons.Github,
                                onPreferenceClick = { uriHandler.openUri("https://github.com/noah-zipit") },
                            )
                            TextPreferenceWidget(
                                title = "X",
                                subtitle = "@Noahextt",
                                icon = SimpleIcons.X,
                                onPreferenceClick = { uriHandler.openUri("https://x.com/Noahextt") },
                            )
                            TextPreferenceWidget(
                                title = stringResource(MR.strings.version),
                                subtitle = "Sonami ${BuildConfig.VERSION_NAME}",
                                icon = MaterialSymbols.Rounded.Info,
                                onPreferenceClick = {
                                    versionTaps += 1
                                    if (versionTaps >= 7) extensionsUnlocked = true
                                },
                            )
                            if (extensionsUnlocked) {
                                TextPreferenceWidget(
                                    title = stringResource(MR.strings.label_extensions),
                                    subtitle = "Hidden Sonami extension manager",
                                    icon = MaterialSymbols.Rounded.Book,
                                    onPreferenceClick = { navigator.push(SonamiHiddenExtensionsScreen()) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
