# Sonami

Sonami is a manga, manhwa and manhua reader for Android 8.0 and higher.

It is built for one thing: open the app, find something to read, and read
it. Browse shows the Popular titles of each installed source as a cover
grid, search runs across all installed sources at once, and any title
you open can be added to your library, where new chapters show up under
Ch Update.

## Install

1. Download the Sonami APK from the latest release on this repository.
2. Open the APK. Android will ask you to allow installs from Sonami;
   allow it when asked, then install.
3. Open Sonami. The first screen is Setup, which installs the Famous 5
   sources (below) one by one. Android asks you to confirm every
   install, and Sonami cannot install them silently, so confirm each
   one as it appears.
4. Once the sources are installed, go to Browse. Each source's Popular
   titles appear as a grid; tap a source name at the top to switch
   sources, tap a title to open it, and add it to your library from
   its detail page.

## The Famous 5 sources

These are the sources Sonami ships with, from the Keiyoushi extension
collection:

- Asura Scans
- MangaDex
- MangaFire
- MangaKakalot
- ManhuaPlus

More sources can be added later through the hidden Extensions screen:
in More, tap the Sonami moon mark 7 times, or in About Us, tap the
version row 7 times, to reveal it.

## Why extensions trust automatically

Mihon-style readers treat extensions as code that runs inside the app,
so an extension only loads if it is trusted. Sonami registers the
official Keiyoushi extension store the first time you open Setup, so
extensions signed with Keiyoushi's key trust automatically and load
without you tapping Trust on each one. If Setup cannot reach the
internet the first time it opens, the store is not registered; in that
case, open the hidden Extensions screen and tap Trust on each
extension yourself, as Mihon asks.

## What Sonami is not

Sonami hosts no manga, manhwa or manhua content itself. Every title
comes from a third-party source, and availability and legality vary by
source and region. Sonami does not control, endorse, or guarantee the
content any source serves.

## Privacy

This build includes no telemetry and no in-app update checker, because
it is built without a distribution flag (the `dist` flag is unset, so
telemetry and the updater are off). Sonami stores your library,
reading history and settings on your own device. When a new Sonami
release is published, download it from this repository yourself; the
app will not prompt you to update.

## About the author

Sonami is built by Ashar Qaisar, Full-Stack Developer and AI
Integration Engineer from Gujranwala, Pakistan. Founder of Elevate
Mavens. Contract AI Engineer at Drytis.

- Portfolio: https://asharxyz.vercel.app
- GitHub: https://github.com/noah-zipit
- X: https://x.com/Noahextt

## Licence and attribution

Sonami is a fork of Mihon v0.21.2, open source under the Apache
License 2.0 (Copyright 2015 Javier Tomás, 2024 Mihon Open Source
Project). The full licence is in the LICENSE file, and all Apache 2.0
licence and copyright notices are kept. Files changed for Sonami are
marked with a `SONAMI` comment at the top or at the changed block.

Sonami is not Mihon, is not affiliated with Mihon, and is not endorsed
by Mihon. See SONAMI.md for exactly what changed from Mihon.
