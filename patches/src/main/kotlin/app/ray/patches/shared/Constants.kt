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
}
