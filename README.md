# 🧩 Ray's Patches

A Morphe patch bundle for **LINE** (`jp.naver.line.android`). It replaces LINE's UI icons with
**Microsoft Fluent** icons.

## Custom icons mod

The only patch in this bundle. It replaces **203 vector drawables**:

- bottom tab bar and Timeline tabs
- top bars and headers of the main tabs
- Chats and Friends headers, including the plus menu
- Settings rows
- Home services, feed controls and AI summary

The patch keeps LINE's own colour values, so light and dark themes, pressed states and the red
"new" dot keep working. Account and brand marks (Apple, Google, PayPay, LINE Premium) and composed
buttons are left unchanged.

**Supported LINE versions:** 26.14.0, 26.15.1, 26.16.0.

## Patches list

<!-- PATCHES_START EXPANDED -->
> **[v3.14.0](https://github.com/ray1997/morphe-patches/releases/tag/v3.14.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 LINE&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 26.14.0 | 26.15.1 | 26.16.0 |
| :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Custom icons mod](#custom-icons-mod) | Replaces 203 LINE drawables (tab bar, top bars, Chats and Friends headers, Settings rows and Home) with Microsoft Fluent icons. Colours follow LINE's light and dark themes. Account and brand logos stay unchanged. |  |

</details>

<!-- PATCHES_END -->

## Install

Download `patches-3.14.0.mpp` from the [latest release](https://github.com/ray1997/morphe-patches/releases/latest).

**Morphe Manager:** add this repository as a patch source, or import the `.mpp` file directly.

- Add source: <https://morphe.software/add-source?github=ray1997/morphe-patches>
- Repository: <https://github.com/ray1997/morphe-patches>

**Morphe CLI:**

```bash
java -jar morphe-desktop-*-all.jar patch \
  -p patches-3.14.0.mpp \
  -e "Custom icons mod" \
  -o LINE-icons.apk \
  /path/to/LINE.apkm
```

## Build

The Morphe libraries come from GitHub Packages. Use a classic PAT with `read:packages`:

```bash
./gradlew :patches:buildAndroid -Pgpr.user=<user> -Pgpr.key=<token>
```

The bundle is written to `patches/build/libs/patches-*.mpp`.

## Verify

Apply the bundle to the pinned LINE APK before shipping a change:

```bash
java -jar morphe-desktop-*-all.jar patch \
  -p patches/build/libs/patches-*.mpp \
  --exclusive -e "Custom icons mod" \
  -o work/patched.apk \
  /path/to/LINE.apkm
```

The log must show `203 resource files`. Decode the patched APK and check the drawables that the
change touched.

## License and credits

- Ray's Patches: [GNU General Public License v3.0](LICENSE).
- [Morphe](https://github.com/MorpheApp) supplies the patching framework.
- The icons come from [Microsoft Fluent UI System Icons](https://github.com/microsoft/fluentui-system-icons),
  Copyright (c) Microsoft Corporation, MIT License. The licence is included at
  `patches/src/main/resources/fluent-icons/LICENSE`.
- LINE is a trademark of LY Corporation. This project has no connection to LINE or LY Corporation.
