package com.rejwane.reelslocal.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.rejwane.reelslocal.data.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports and restores the entire local library as a single versioned JSON document.
 * Import replaces all content atomically inside one Room transaction, preserving the
 * original row ids so relational links (owner, author, likes, follows) stay intact.
 */
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun buildBackupJson(): String = withContext(Dispatchers.IO) {
        val payload = BackupPayload(
            exportedAt = System.currentTimeMillis(),
            users = db.userDao().getAllOnce().map { it.toDto() },
            videos = db.videoDao().getAllOnce().map { it.toDto() },
            comments = db.commentDao().getAllOnce().map { it.toDto() },
            follows = db.socialDao().getAllFollowsOnce().map { it.toDto() },
            videoLikes = db.socialDao().getAllVideoLikesOnce().map { it.toDto() },
            savedVideos = db.socialDao().getAllSavedOnce().map { it.toDto() },
            commentLikes = db.socialDao().getAllCommentLikesOnce().map { it.toDto() },
            activities = db.activityDao().getAllOnce().map { it.toDto() }
        )
        json.encodeToString(BackupPayload.serializer(), payload)
    }

    suspend fun restoreFromJson(raw: String) = withContext(Dispatchers.IO) {
        val payload = json.decodeFromString(BackupPayload.serializer(), raw)
        require(payload.schemaVersion <= BACKUP_SCHEMA_VERSION) {
            "Unsupported backup version ${payload.schemaVersion}"
        }
        db.withTransaction {
            db.socialDao().deleteAllCommentLikes()
            db.socialDao().deleteAllSavedVideos()
            db.socialDao().deleteAllVideoLikes()
            db.socialDao().deleteAllFollows()
            db.activityDao().deleteAll()
            db.commentDao().deleteAll()
            db.videoDao().deleteAll()
            db.userDao().deleteAll()

            db.userDao().insertAll(payload.users.map { it.toEntity() })
            db.videoDao().insertAll(payload.videos.map { it.toEntity() })
            db.commentDao().insertAll(payload.comments.map { it.toEntity() })
            db.socialDao().insertFollows(payload.follows.map { it.toEntity() })
            db.socialDao().insertVideoLikes(payload.videoLikes.map { it.toEntity() })
            db.socialDao().insertSavedVideos(payload.savedVideos.map { it.toEntity() })
            db.socialDao().insertCommentLikes(payload.commentLikes.map { it.toEntity() })
            db.activityDao().insertAll(payload.activities.map { it.toEntity() })
        }
    }

    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val text = buildBackupJson()
        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
        } ?: error("Cannot open output stream")
    }

    suspend fun importFrom(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: error("Cannot open input stream")
    }
}
