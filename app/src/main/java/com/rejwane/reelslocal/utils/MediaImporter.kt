package com.rejwane.reelslocal.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies user-picked media (via the Storage Access Framework) into app-specific storage so the
 * library stays fully offline and self-contained. SAF needs no runtime storage permission.
 * Returns the absolute file path, which is exactly what [MediaPaths] expects for non-asset media.
 */
@Singleton
class MediaImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun import(uri: Uri, kind: Kind): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(mediaDir(), kind.folder).apply { mkdirs() }
            val target = File(dir, "${kind.prefix}_${System.currentTimeMillis()}${extensionFor(uri)}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            target.absolutePath
        }.getOrNull()
    }

    private fun mediaDir(): File =
        context.getExternalFilesDir("media") ?: File(context.filesDir, "media")

    private fun extensionFor(uri: Uri): String {
        val resolver = context.contentResolver
        val displayName = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
        displayName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() && it.length <= 5 }
            ?.let { return ".$it" }
        val mime = resolver.getType(uri)
        val fromMime = mime?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
        return if (!fromMime.isNullOrBlank()) ".$fromMime" else ""
    }

    enum class Kind(val folder: String, val prefix: String) {
        VIDEO("videos", "vid"),
        IMAGE("images", "img")
    }
}
