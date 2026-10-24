# Sonami

Sonami is a fork of Mihon v0.21.2 (Apache License 2.0, Copyright 2015
Javier Tomás, 2024 Mihon Open Source Project). All Apache 2.0 licence and
copyright notices are kept; files changed for Sonami are marked with a
`SONAMI` comment at the top or at the changed block.

## What changed from Mihon

- Package: `app.mihon` -> `com.sonamidex`. App name: Mihon -> Sonami.
  The internal Kotlin package `eu.kanade.tachiyomi` is unchanged (renaming
  it would break the extension ABI and every internal reference).
- Launcher icon: the moon-and-dragon icon (pure graphic, no letters), in
  `res/mipmap-*/ic_launcher.png`. The in-app mark is the white
  moon-and-dragon symbol in `res/drawable-nodpi/ic_sonami_mark.png`.
  The old Mihon glyph vector (`ic_mihon.xml`) now points at that mark.
- Updates tab -> "Ch Update" (`label_recent_updates` string).
- More screen: redesigned as grouped cards (Reading / Library / App),
  Sonami moon-mark header, indigo default theme (see
  `TachiyomiColorScheme.kt`). Support Us is replaced by About Us
  (`SonamiAboutScreen.kt`); Help is removed from More. The Settings
  "About" page links were also re-pointed to the author's portfolio,
  GitHub and X, and Mihon donation/Discord links were removed.
- Browse: Mihon's Sources / Extensions / Migrate tabs are removed from
  normal use. `BrowseTab` is now one unified screen
  (`SonamiBrowseScreen.kt`): one search across all installed sources
  (Mihon's global search) plus the installed sources' popular listings.
  A title opened from there can be added to the library as usual.
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

Requires JDK 21, Android SDK platform 37 (compileSdk 37.2), build-tools
36, NDK 29.0.14206865 (see `gradle/mihon.versions.toml`). Build with
`./gradlew assembleRelease -Pdist=ci` (Mihon CI, build.yml) or
`./gradlew assembleFoss -Pdist=foss`. There is NO
`assembleStandardRelease` task: the app defines no product flavors,
only build types debug/release/foss/nightly/benchmark, so
`assembleStandardRelease` fails with "task not found" (proven on
Kaggle 2026-10-24).
The build is unsigned debug-style unless a keystore is supplied; the
APK in `app/build/outputs/apk/` is a development build, not a release
signed for distribution.

## Kaggle build failure, 2026-10-24 23:48 PKT (verified)
Notebook (private, unpublished, Kaggle user Noah Ext) ran with
Internet ON, CPU. Two separate failures, no APK produced:
1. Android SDK platform 37 UNAVAILABLE via sdkmanager, stable and
   channel 3 (canary) both: "Failed to find package
   'platforms;android-37'", no platforms/ directory created.
   Platform 37 is Android 17, "Cinnamon Bun Preview" in Android
   Studio's SDK Manager, not a stable platform package.
2. Gradle task name wrong: `./gradlew :app:assembleStandardRelease`
   failed "task 'assembleStandardRelease' not found in project ':app'"
   (Gradle 9.8.1, 2m 44s). Correct task is assembleRelease (see above).
Next attempt must fix the task name first, then solve platform 37
(newer cmdline-tools / preview package, or a compileSdk 36 test build
- a test only, not a silent downgrade of the shipped fork).

## Build blocker, 2026-10-25 (verified, environment, not code)

No APK could be produced in this subagent context. Root cause, proven
by a raw-socket test: the sandbox intercepts Java TCP connections and
answers with a block message ("Other TCP connections is turned off for
this assistant"). Consequences, all reproduced:
- Gradle wrapper cannot download Gradle 9.8.1 (Java HttpURLConnection
  tunnelling fails with NoSuchElementException on the empty response).
- Gradle cannot start at all — even `--no-daemon` forks a single-use
  daemon and fails with "Could not dispatch a message to the daemon",
  because daemon IPC is a localhost TCP socket.
- sdkmanager cannot fetch any repository (same Java tunnelling failure).
- Gradle dependency downloads would fail the same way.
Independently, this VM also lacks the exact toolchain: Android platform
37 is absent from Google's stable repository XML (max is android-36);
AGP 9.4.1 and Gradle 9.8.1 are in no local cache (the local mirror
`~/workspace/android-tools/localrepo` tops out at AGP 8.11.1, cached
Gradle tops out at 9.3.1).
What unblocks it, in order: (1) run the Gradle build from the
parent/main-agent context or any machine with normal Java networking,
with platform 37 / build-tools 36 / NDK 29 installed (or the
preview/canary repository URL that actually hosts platform 37); (2)
only then fix any compile or Spotless errors in the new Kotlin files
(they have NOT been compile-checked) and run the APK verification
tests. Downgrading to the cached SDK 35 / AGP 8.11.1 / Gradle 9.3.1 was
NOT done — it would be a large port (AGP 9 DSL, e.g. minorApiLevel,
does not exist in AGP 8) and would no longer be the v0.21.2 fork.
