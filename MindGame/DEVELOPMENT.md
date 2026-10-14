# Home review checkpoint — 2026-10-14

v0.4.0-home-preview / versionCode 4. The user rejected all v0.3.0 visuals, despite working mechanics. New authorization is **home only**, then present a real Android render beside approved C and a first-screen APK; wait for feedback before touching the remaining game screens.

Approved C differs from rejected implementation: one continuous walnut table; cipher on loose maps; overlapping relic cards; physical owl mosaic frame; smaller physical puzzles between props; nearby parchment labels; art integrated with header/resume/navigation. Former implementation used seven uniformly framed image cards and unrelated large buttons.

ExplorerDesk.kt is a native FrameLayout with a single scene painting and independent native focusable controls over every physical object. Canvas text/icons are live. No WebView/HTML. Art positions and object proportions derive from approved C. Local resume content is truthful, including a distinct first-install state. Press/cancel is tested. Artwork in art/explorer_scene_v04.webp and explorer_owl_resume_v04.webp. Game/rules/Enigma/owl checksums are unchanged.

Comparison must distinguish left approved generated concept, right native Android view render. Scene is newly painted from the approved reference, not pixel-for-pixel identical. All targets tested on 360×640, 393×810 and 412×860. No game engine or game-screen implementation changes are authorized after delivery until user feedback.

Validation: 30 tests passed, 0 failures/errors/skips. Android lint has 0 errors. All 12 home native targets are tested across 3 phone sizes with real touch dispatch and cancellation; original game routes still work. Native scene label spacing visually checked. Manifest has no permissions; APK v2 signature verified.

APK SHA-256: cdc195cd3b0ef5e45abc7e2e0cb28c0d9751370982ef80830ee4528950cd15a5


---

# Checkpoint — 2026-10-14

User approved C Explorer’s Desk, all remaining game mockups, and previously approved Boardgame Mastermind, Relic Hunt and Mosaic Restoration. Development resumed by explicit user instruction. Owl launcher icon requested during this build. User deprioritized migration for this test version; do not delay testing for comprehensive upgrade migration work.

Release: v0.3.0 / versionCode 3. Kotlin native app plus original recovered Enigma Java. No permissions/network gameplay. Local save formats accept previous simple states where practical.

Implemented designs, controls, difficulty and short guides are listed in README.md. All artworks bundled, reference mockups are not used as interactive screen screenshots. Future compiled games still need a new APK; modules isolate changes.

Sudoku: 80 unique technique-rated bases, randomized isomorphisms preserve rules and rating. Expert means beyond implemented singles/locked/pairs analyzer; not a claim of a particular advanced technique. Mosaic: 12 original pixel silhouettes, at most one boundary pixel adjusted on crown/compass/feather to remove ambiguity. Complete line deduction proves uniqueness. Original Enigma four distinct digits / three guesses retained.

Final validation: 28 tests; zero failures/errors/skips. APK build and Android lint succeeded; 0 lint errors, 38 nonblocking warnings (primarily existing API/style and custom-view accessibility recommendations). APK v2 signature verified. Manifest requests no permissions. Native graphics renders checked, including visible original Enigma scratch candidates and single-screen guides. Hardware gesture feel remains for phone review.

APK SHA-256: 33c424bb3d39bfa22938f866af6e283c567ed1923ef3269d940bb72dc1b29f6a

