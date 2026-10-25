# Sonami

Sonami is a fork of Mihon v0.21.2 (Apache License 2.0, Copyright 2015
Javier Tomás, 2024 Mihon Open Source Project). All Apache 2.0 licence and
copyright notices are kept; files changed for Sonami are marked with a
`SONAMI` comment at the top or at the changed block.

Sonami's own version is 1.0.0 (versionCode 39). The "v0.21.2" above is
the Mihon version it was forked from, not Sonami's version.

## What changed from Mihon

- Package: `app.mihon` -> `com.sonamidex`. App name: Mihon -> Sonami.
  The internal Kotlin package `eu.kanade.tachiyomi` is unchanged (renaming
  it would break the extension ABI and every internal reference).
- Launcher icon: the moon-and-dragon icon (pure graphic, no letters),
  as a proper adaptive icon (indigo background, transparent moon-mark
  foreground) plus rounded legacy PNGs in `res/mipmap-*/ic_launcher.png`.
  The in-app mark is the white moon-and-dragon symbol in
  `res/drawable-nodpi/ic_sonami_mark.png`. The old Mihon glyph vector
  (`ic_mihon.xml`) now points at that mark.
- Updates tab -> "Ch Update" (`label_recent_updates` string).
- More screen: redesigned as grouped cards (Reading / Library / App),
  Sonami moon-mark header, indigo default theme (see
  `TachiyomiColorScheme.kt`). Support Us is replaced by About Us
  (`SonamiAboutScreen.kt`); Help is removed from More. The Settings
  "About" page links were also re-pointed to the author's portfolio,
  GitHub and X, and Mihon donation/Discord links were removed.
- Browse: Mihon's Sources / Extensions / Migrate tabs are removed from
  normal use. `BrowseTab` is now one unified screen
  (`SonamiBrowseScreen.kt` + `SonamiSourceGrid.kt`): a compact search
  row at the top, a row of source chips, and below them the selected
  source's Popular titles as a cover grid, with Popular / Latest /
  Filter chips exactly like a source's own browse page. Tapping a
  source chip switches the grid to that source; tapping a title opens
  its detail page so it can be added to the library. The source list
  comes from all loaded extension sources (`SourceManager.sources`),
  not from the enabled-languages list, so the Famous 5 show as soon as
  their extensions are loaded.
- Search: Sonami's search uses all installed sources
  (`GlobalSearchScreen` with `searchAllSources = true`), not Mihon's
  default pinned-only filter, because the Famous 5 are not pinned by
  default. Other Mihon search entry points keep the pinned-only
  default.
- Donation campaign removed: Mihon's in-app donation banner in
  `MainActivity` (`ShowDonationCampaign`) is deleted, along with its
  donation/Support Us strings in `strings.xml` and the orphaned
  adaptive-icon vectors that still contained the Mihon glyph. Base
  strings now contain zero "Mihon" occurrences. The dead
  `donationCampaignShown` preference key is left in place (no readers
  remain; removing a stored key is a migration risk for no benefit).
- Hidden extension mode: tapping the Sonami moon mark in More 7 times,
  or the version row in About Us 7 times, reveals a hidden Extensions
  row that opens `SonamiHiddenExtensionsScreen`, which reuses Mihon's
  own extensions tab (install / update / uninstall / stores). Normal
  users never see it.

## Pre-installed sources ("Famous 5") — mechanism, stated honestly

Mihon extensions are separate APKs and Android does not allow an app to
silently install other APKs. Sonami therefore bundles five Keiyoushi
extension APKs in `app/src/main/assets/sonami_extensions/` and
`SonamiSetupScreen` installs each one through the normal system
installer, which asks the user to confirm every install. After that one
time step, the sources behave like any Mihon extension and appear in
unified Browse. No extension code was rewritten.

Shipped set, verified against the live Keiyoushi index on 2026-10-24:

| Source | Package | Version |
| --- | --- | --- |
| Asura Scans | eu.kanade.tachiyomi.extension.en.asurascans | 1.6.69 |
| MangaDex | eu.kanade.tachiyomi.extension.all.mangadex | 1.6.0 |
| MangaFire | eu.kanade.tachiyomi.extension.all.mangafire | 1.6.34 |
| MangaKakalot | eu.kanade.tachiyomi.extension.en.mangakakalot | 1.6.26 |
| ManhuaPlus | eu.kanade.tachiyomi.extension.en.manhuaplus | 1.6.63 |

Swaps from the original "famous 5" list, per the build spec: Comick has
no main Keiyoushi extension (only a "Comick (Unoriginal)" mirror), so
MangaFire takes its place. MangaKakalot was verified in the live
Keiyoushi index (v1.6.26) on 2026-10-24, correcting an earlier wrong
claim that it had none; MangaPark was dropped as precarious in
community notes.

## Extension trust: Keiyoushi store seeding

Mihon only loads an extension if it is trusted: either its signature
matches a registered extension store's signing key, or the user tapped
Trust on it. Sideloaded Keiyoushi APKs therefore used to land in
"Not loaded" as Untrusted. Sonami fixes this in `SonamiSetupViewModel`:
the first time Setup opens, it registers the official Keiyoushi
extension store (index
`https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.json`)
if it is not already registered, and the fetched index supplies the
signing key — no signing key is hardcoded in Sonami code and no trust
preference is written directly. Once the store is registered, the
Famous 5 trust automatically and load without tapping Trust on each
one. If Setup cannot reach the internet on first open, the store is
not registered and the fallback is the manual route: open the hidden
Extensions screen and tap Trust on each extension.

## Setup flow (1.0.0)

- Install permission: if Android's "allow installs from Sonami" setting
  is off, Setup shows a card with a button that opens that system
  setting. The card disappears once the permission is granted.
- Installed state is re-checked when returning from the installer
  (on resume), not at tap time, so an install that just completed
  shows as installed.
- Sequential install: Setup offers the first not-yet-installed
  extension as "Next: install X". It never auto-launches the
  installer on return, so a declined install cannot loop.
- All-installed state: when all five show installed, Setup shows
  "All 5 installed — go to Browse".
- Pinning: when a Famous 5 source becomes installed, its source id is
  added to the pinned sources, so Mihon's pinned-only search paths
  also cover it. Pinning happens only in the Setup flow, never on
  Browse open, so a user unpin is not re-applied by browsing.

## Known remaining Mihon references (not user-facing branding)

- Deep help links inside Settings (storage FAQ, tracking guide),
  the WebView Cloudflare troubleshooting link, and the migration-guide
  link still point at Mihon documentation pages. They are help content,
  not the Mihon name, logo, donation or social links, and were left
  working rather than broken.
- The local-source help entry in the source-browse toolbar is a
  functional help item for local files, not the removed More > Help.
- Tracker, backup, reader and download code is unchanged from Mihon.

## Building

Requires JDK 21, Android SDK platform 37 (compileSdk 37), build-tools
36, NDK 29.0.14206865 (see `gradle/mihon.versions.toml`). Build with
`./gradlew assembleRelease` and NO `-Pdist` flag: without a
distribution flag, telemetry is off and the google-services plugin is
not applied, which matters because the shipped `google-services.json`
still names Mihon's packages — do NOT rename the package in that file,
as that would send Sonami crash data to Mihon's Firebase project.
There is NO `assembleStandardRelease` task: the app defines no product
flavors, only build types debug/release/foss/nightly/benchmark.

The release APK is arm64-only (`app-arm64-v8a-release.apk`, ~26 MB),
copied to the release as `sonami.apk`. Public releases must be signed
with a private release keystore: the test builds so far were signed
with Android's default debug key, which is public and must never be
used for a published release, because anyone holding it could sign a
fake Sonami update that users' phones would accept.
