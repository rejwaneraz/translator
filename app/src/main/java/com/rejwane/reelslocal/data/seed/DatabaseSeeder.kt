package com.rejwane.reelslocal.data.seed

import android.util.Log
import androidx.room.withTransaction
import com.rejwane.reelslocal.data.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds deterministic sample content the first time the database is empty.
 * Also powers "Reset app data" (clear + reseed) from Settings.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun seedIfEmpty() {
        if (db.userDao().getCountOnce() > 0) return
        seed()
        Log.i(TAG, "Seed data inserted")
    }

    suspend fun seed() {
        val bundle = SeedData.build()
        db.withTransaction {
            db.userDao().insertAll(bundle.users)
            db.videoDao().insertAll(bundle.videos)
            db.commentDao().insertAll(bundle.comments)
            db.socialDao().insertFollows(bundle.follows)
            db.socialDao().insertVideoLikes(bundle.videoLikes)
            db.socialDao().insertSavedVideos(bundle.savedVideos)
            db.socialDao().insertCommentLikes(bundle.commentLikes)
            db.activityDao().insertAll(bundle.activities)
        }
    }

    /** Full reset: wipes every table and restores the seed content. */
    suspend fun resetAndReseed() {
        db.clearAllTables()
        seed()
        Log.i(TAG, "Database reset and reseeded")
    }

    companion object {
        private const val TAG = "DatabaseSeeder"
    }
}
