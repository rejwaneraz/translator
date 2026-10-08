package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.AppDatabase
import com.rejwane.reelslocal.data.database.dao.VideoDao
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.model.RepoResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalVideoRepository @Inject constructor(
    private val db: AppDatabase,
    private val videoDao: VideoDao
) : VideoRepository {

    override fun observeForYouFeed(): Flow<List<Video>> = videoDao.observeForYouFeed()

    override fun observeFollowingFeed(userId: Long): Flow<List<Video>> =
        videoDao.observeFollowingFeed(userId)

    override fun observeVideosByUser(ownerId: Long): Flow<List<Video>> =
        videoDao.observeVideosByUser(ownerId)

    override fun observeLikedVideos(userId: Long): Flow<List<Video>> =
        videoDao.observeLikedVideos(userId)

    override fun observeSavedVideos(userId: Long): Flow<List<Video>> =
        videoDao.observeSavedVideos(userId)

    override fun observeVideo(videoId: Long): Flow<Video?> = videoDao.observeVideo(videoId)

    override suspend fun getVideo(videoId: Long): Video? = videoDao.getVideo(videoId)

    override fun searchVideos(query: String): Flow<List<Video>> =
        videoDao.searchVideos(normalize(query))

    override fun observeVideosByHashtag(hashtag: String): Flow<List<Video>> =
        videoDao.observeVideosByHashtag(hashtag.removePrefix("#"))

    override fun observeAllWithOwner(): Flow<List<VideoWithOwner>> = videoDao.observeAllWithOwner()

    override fun observeForYouFeedWithOwner(): Flow<List<VideoWithOwner>> =
        videoDao.observeForYouFeedWithOwner()

    override fun observeFollowingFeedWithOwner(userId: Long): Flow<List<VideoWithOwner>> =
        videoDao.observeFollowingFeedWithOwner(userId)

    override fun observeWithOwnerByUser(ownerId: Long): Flow<List<VideoWithOwner>> =
        videoDao.observeWithOwnerByUser(ownerId)

    override fun searchVideosWithOwner(query: String): Flow<List<VideoWithOwner>> =
        videoDao.searchVideosWithOwner(normalize(query))

    override suspend fun createVideo(video: Video): RepoResult<Long> {
        validate(video)?.let { return RepoResult.Error(it) }
        if (db.userDao().getUser(video.ownerId) == null) {
            return RepoResult.Error("Invalid owner")
        }
        return RepoResult.Success(videoDao.insert(sanitize(video)))
    }

    override suspend fun updateVideo(video: Video): RepoResult<Unit> {
        validate(video)?.let { return RepoResult.Error(it) }
        if (db.userDao().getUser(video.ownerId) == null) {
            return RepoResult.Error("Invalid owner")
        }
        videoDao.update(sanitize(video))
        return RepoResult.Success(Unit)
    }

    override suspend fun duplicateVideo(videoId: Long): RepoResult<Long> {
        val source = videoDao.getVideo(videoId) ?: return RepoResult.Error("Video not found")
        val copy = source.copy(
            id = 0,
            title = "${source.title} (copy)",
            views = source.views,
            likesCount = source.likesCount,
            commentsCount = 0,
            sharesCount = 0,
            sortOrder = source.sortOrder + 1,
            createdAt = System.currentTimeMillis()
        )
        return RepoResult.Success(videoDao.insert(copy))
    }

    override suspend fun deleteVideo(videoId: Long): RepoResult<Unit> {
        val video = videoDao.getVideo(videoId) ?: return RepoResult.Error("Video not found")
        // Comments, likes and saves cascade-delete with the video (FK constraints).
        videoDao.delete(video)
        return RepoResult.Success(Unit)
    }

    override suspend fun setStats(
        videoId: Long,
        views: Long,
        likes: Long,
        comments: Long,
        shares: Long
    ): RepoResult<Unit> {
        val video = videoDao.getVideo(videoId) ?: return RepoResult.Error("Video not found")
        videoDao.update(
            video.copy(
                views = views.coerceAtLeast(0),
                likesCount = likes.coerceAtLeast(0),
                commentsCount = comments.coerceAtLeast(0),
                sharesCount = shares.coerceAtLeast(0)
            )
        )
        return RepoResult.Success(Unit)
    }

    private fun validate(video: Video): String? = when {
        video.videoPath.isBlank() -> "Video file path cannot be empty"
        video.title.isBlank() && video.caption.isBlank() -> "Title or caption is required"
        video.title.length > 120 -> "Title must be 120 characters or fewer"
        video.caption.length > 500 -> "Caption must be 500 characters or fewer"
        video.views < 0 || video.likesCount < 0 || video.commentsCount < 0 || video.sharesCount < 0 ->
            "Statistics cannot be negative"
        else -> null
    }

    private fun sanitize(video: Video): Video = video.copy(
        title = video.title.trim(),
        caption = video.caption.trim(),
        hashtags = video.hashtags.trim(),
        views = video.views.coerceAtLeast(0),
        likesCount = video.likesCount.coerceAtLeast(0),
        commentsCount = video.commentsCount.coerceAtLeast(0),
        sharesCount = video.sharesCount.coerceAtLeast(0),
        durationMs = video.durationMs.coerceAtLeast(0)
    )

    private fun normalize(query: String): String = query.trim()
}
