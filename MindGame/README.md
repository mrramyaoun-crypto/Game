# MindGame Android — v0.4.0 home review

Native Android phone APK, Kotlin puzzle engines and native Android views, with the recovered Java Enigma v13 game. Android 8.0+. All gameplay and art are bundled. No ads, login, subscription, server or network permission.

## Home review build — awaiting approval

The user rejected v0.3.0's visual fidelity. This build changes **only the home presentation**, based directly on approved concept C, Explorer’s Desk. It replaces the separate image cards with one continuous illustrated walnut desk, seven physical game objects with their own native touch/accessibility targets, live labels, settings, bottom navigation, and a local saved-game resume tray. Fresh installation displays an honest first-puzzle invitation instead of fictitious progress.

The scene fits with one uniform scale; game objects are never stretched to fill boxes. Press feedback is a subtle native warm light. Small-screen target padding keeps settings and navigation at least 48dp. The largest supported test phone width is 412dp; tested heights include 640, 810 and 860dp. Native scene art is bundled and cached; no browser or network use.

**Other game screens remain exactly as v0.3.0 and are still awaiting faithful reconstruction.** Engine files, rules, original Enigma module and owl launcher pixels have been verified unchanged. Continue those screens only after the user reviews this home APK.

## Existing gameplay retained from v0.3.0

- **Launcher: mosaic owl.** Adaptive icon and matching in-app owl artwork.
- **Enigma:** full recovered mechanical code machine, clue papers, candidate list, scratch-outs, digit elimination, hint and Easy/Normal/Hard. Four distinct digits and three guesses. Help integrated into the original toolbar; phone Back returns to the library and candidate paper scrolls; original rules retained.
- **Mastermind:** wooden ten-row board, truly round colored pegs, six direct color choices, tap a slot to replace it. No color cycling. Black exact and ivory misplaced feedback; repeated colors allowed. Draft guesses save locally.
- **Relic Hunt:** sixteen detailed antique relic illustrations replace fruit. Hunts of 6, 8, 12 or 16 pairs; flip animations and matched markers.
- **Mosaic Restoration:** 12 named pictures across 10×10, 15×15 and 20×20. Each is fully solvable by line-constraint deduction and uniquely determined. Drag to fill/cross, completed clue dimming, pan, pinch and button zoom, stroke undo, picture reveal and a local collection. New selects a different picture of the same size.
- **Sudoku:** 80 validated puzzle bases across four technique-rated tiers, with unit-preserving digit/row/column/transposition variations for replay. Easy: naked/hidden singles. Medium: requires locked candidates. Hard: requires candidate pairs after singles/locked candidates. Expert: remains unsolved by those techniques and requires stronger deduction. Standard 9×9 grid, pencil notes, conflicts, related-cell highlights, erase, undo and a clearly labeled reveal hint.
- **Minesweeper:** Beginner 9×9/10, Intermediate 16×16/40, Expert 30×16/99 and Custom (8–30 columns/rows). Reveal/Flag controls, long-press flags, chording, larger-board pan/zoom and viewport overview. First opening and neighbors are safe. Classic boards may require guessing; no no-guess mode is claimed.
- **2048:** tactile ceramic tiles, native sliding animation, correct once-per-move merges, persisted last-move undo, score/best and milestones through 4096. Continue after reaching 2048.

## Rules

Every game has a compact, single-screen guide: Goal, Rules, Example, Play. Larger accessibility font sizes can scroll. Explanations are written in our own words, adjusted to the implemented single-player variants. Gameplay does not open a browser.

Primary rules/reference sources checked on 2026-10-14:
- Sudoku: https://easybrain.com/sudoku
- Picture logic: https://easybrain.com/nonogram
- Minesweeper developer listing: https://play.google.com/store/apps/details?id=com.jurajkusnier.minesweeper
- Original 2048: https://gabrielecirulli.github.io/2048/ and https://www.2048original.com/
- Mastermind publisher instruction scan: https://www.buffalolib.org/sites/default/files/gaming-unplugged/inst/Mastermind%20Instructions.pdf
- Matching-pair publisher rules: https://www.ravensburger.org/spielanleitungen/ecm/Spielanleitungen/20877%20anl%202155250.pdf
- Enigma-specific distinct-digit/three-guess rules: recovered v13 code and generation tests, rather than a generic Mastermind rulebook.

## Build

Use JDK 17, Android SDK 35 and the Gradle wrapper:

```
./gradlew testDebugUnitTest assembleDebug lintDebug
```

First development build downloads dependencies. Installation/gameplay require no internet.

Test APK: `app/build/outputs/apk/debug/app-debug.apk`. Same application ID and personal testing key as earlier APKs. Install over the existing app if desired. Source includes the conventional test key for reproducible personal updates; use a private production signing identity before store publishing.

## Modules and verification

`core/`: registry, rules, local saves/settings/statistics. `games/`: independently tested engines and validated catalogs. `ui/`: reusable materials, artwork, native gesture boards, guides and animations. `ui/games/`: screen module per game. `com/codebreaker/game/`: recovered Enigma.

Adding compiled native game code requires a new APK, but does not require rewriting existing game modules. No dynamic downloaded executable code.

Native Skia renders from Robolectric are generated in `app/build/qa`. They show real Android view code, not image-generation mockups. Tests cover engines, catalog uniqueness, larger levels, undo/save restoration, chording, direct palette input, guides fitting a normal phone screen, Enigma generation and scratch/keyboard interactions. Phone testing is still needed for hardware gesture feel and device font/inset differences.

Regeneration tools are in `tools/`; run them from the project root. `generate_sudoku.py` uses Python stdlib; `generate_mosaics.py` also uses Pillow. Catalog data is bundled; these tools never run on the phone. Artwork provenance and prompts are in `ARTWORK.md`.
