package app.ray.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

object Constants {
    /**
     * LINE messenger (jp.naver.line.android).
     *
     * LINE ships as a split app bundle, so it uses [ApkFileType.APKM].
     * Pin the exact version the patches were developed and confirmed against.
     *
     * 26.14.0 and 26.16.0 need Android 11 (API 30). 26.15.1 needs Android 12L (API 32).
     * Keep targets sorted in ascending version order (oldest to newest).
     */
    val COMPATIBILITY_LINE = Compatibility(
        name = "LINE",
        packageName = "jp.naver.line.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x06C755, // LINE brand green.
        targets = listOf(
            AppTarget(
                version = "26.14.0",
                minSdk = 30,
            ),
            AppTarget(
                version = "26.15.1",
                minSdk = 32,
            ),
            AppTarget(
                version = "26.16.0",
                minSdk = 30,
            ),
        ),
    )

    /**
     * YouTube (com.google.android.youtube).
     *
     * YouTube ships as one APK. The version is the non-experimental target from the
     * upstream Morphe patches, so both bundles can patch the same file.
     */
    val COMPATIBILITY_YOUTUBE = Compatibility(
        name = "YouTube",
        packageName = "com.google.android.youtube",
        apkFileType = ApkFileType.APK_REQUIRED,
        appIconColor = 0xFF0033,
        signatures = setOf(
            // Android 13+
            "5aad2bee6db95d17e05a08d7d1e64c10a1511879154483916b6ae6c7fd9cb0c6",
            // Android 7+
            "3d7a1223019aa39d9ea0e3436ab7c0896bfb4fb679f4de5fe7c23f326c8f994a",
        ),
        targets = listOf(
            AppTarget(
                version = "21.30.216",
                minSdk = 29,
            ),
        ),
    )
}
