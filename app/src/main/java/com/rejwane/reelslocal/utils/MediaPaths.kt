package com.rejwane.reelslocal.utils

import android.net.Uri
import java.io.File

/**
 * Resolves stored media paths to concrete sources.
 *
 * Convention: `asset:videos/video_001.mp4` → bundled asset; anything else → absolute file
 * path inside app-specific storage (imported media). Never hardcodes machine paths.
 */
object MediaPaths {

    private const val ASSET_PREFIX = "asset:"

    fun isAsset(path: String): Boolean = path.startsWith(ASSET_PREFIX)

    fun assetRelativePath(path: String): String = path.removePrefix(ASSET_PREFIX)

    /** Model for Coil: asset URL or local File. Null-safe for missing paths. */
    fun imageModel(path: String?): Any? = when {
        path.isNullOrBlank() -> null
        isAsset(path) -> "file:///android_asset/${assetRelativePath(path)}"
        else -> {
            val file = File(path)
            if (file.exists()) file else null
        }
    }

    /** Uri for Media3 playback. */
    fun videoUri(path: String): Uri =
        if (isAsset(path)) {
            Uri.parse("asset:///${assetRelativePath(path)}")
        } else {
            Uri.fromFile(File(path))
        }
}
