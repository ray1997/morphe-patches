# AGENTS.md

Guidance for coding agents working in this repository. `CLAUDE.md` is a symlink to this file.

## What this is

**Ray's Patches** is a Morphe patch bundle with one patch, **Custom icons mod**. It is a resource
patch: it replaces LINE's vector drawables with Microsoft Fluent icons. There is no bytecode patch
and no extension module.

Package root: `app.ray.patches`.

## Build

The Morphe patcher libraries and Gradle plugin come from the GitHub Packages registry
`maven.pkg.github.com/MorpheApp/registry`. Use a classic PAT with `read:packages`:

```bash
./gradlew :patches:buildAndroid -Pgpr.user=<user> -Pgpr.key=<token>
```

The bundle is `patches/build/libs/patches-*.mpp`.

Without a PAT, build from the Gradle cache:

```bash
./gradlew :patches:buildAndroid --offline --no-daemon -Pgpr.user=dummy -Pgpr.key=dummy
```

The release workflow reads the credentials from the `GPR_USER` and `GPR_KEY` repository secrets.
`settings.gradle.kts` also accepts them as environment variables.

## How the patch works

- **`CustomIconsPatch.kt`** is a `resourcePatch` named `Custom icons mod`.
- **`IconTargets.kt`** lists each LINE drawable and the Fluent 24dp icon that replaces it.
- **`patches/src/main/resources/fluent-icons/`** holds the Fluent vector XMLs, copied from
  `microsoft/fluentui-system-icons` (MIT). `LICENSE` is bundled with them.

For each target, the patch:

1. reads the original drawable and its width, height and viewport;
2. picks the visible icon colour, ignoring the red `#ff334b` badge and transparent hit areas;
3. writes a new vector with the Fluent path in a `<group>` scaled into the original glyph box;
4. copies any red "new" dot path unchanged;
5. keeps `@color/...` references so light, dark and pressed states still follow LINE's theme.

Placement is chosen by the original drawable size:

| Size | Glyph box |
|---|---|
| `64x85` tab bar | 20px at `(22, 40)` |
| `38x38` header | 20px at `(9, 9)` |
| `36x36` Home service | 28px at `(4, 4)` |
| `28x28` chat input | 26px at `(1, 1)` |
| `22x22` Settings row | 21px at `(0.5, 0.5)` |
| `67x67`, `69x69` | 40px / 39px, centred |
| anything else | fitted to the smaller viewport dimension |

The file is skipped if it has no visible colour, no path, or no matching drawable. The patch fails
if fewer than 100 drawables are replaced, which catches a renamed LINE resource family.

## Add or change an icon

1. Find the LINE drawable name in the decompiled resources.
2. Copy the Fluent 24 regular or filled XML into `patches/src/main/resources/fluent-icons/`.
3. Add `IconTarget("<LINE drawable>", "<fluent>_24_<style>")` to `ICON_TARGETS`.
4. Build, apply, and decode the patched APK. Check the colour, scale and any preserved badge.

Do not replace brand marks (Apple, Google, PayPay, LINE Premium), premium badges, or composed
buttons that draw a background circle around the glyph. The remaining archive glyph on the Chats
screen is drawn in Compose, not in a drawable, so a resource patch cannot change it.

## Supported versions

`app.ray.patches.shared.Constants.COMPATIBILITY_LINE` pins 26.14.0, 26.15.1 and 26.16.0. The
resource names are the compatibility contract. Add a version only after applying the bundle to it.

## Release

- All work is on `main`. Use conventional commits. `feat` and `fix` produce a release; `chore` and
  `docs` do not.
- Pushing to `main` runs `.github/workflows/release.yml`: semantic-release bumps the version, builds
  the bundle, drafts the release, uploads the `.mpp`, attests it and publishes it.
- Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`, `gradle.properties`
  (`version`), or the README table between `PATCHES_START` and `PATCHES_END`.
- Do not create or upload releases by hand.

## Legal

- Ray's Patches is GPLv3. Keep the copyright notices.
- The Fluent icons are MIT. Keep `patches/src/main/resources/fluent-icons/LICENSE`.
- LINE is a trademark of LY Corporation. Do not imply endorsement.
