package com.rejwane.reelslocal.data.repository

import androidx.room.withTransaction
import com.rejwane.reelslocal.data.database.AppDatabase
import com.rejwane.reelslocal.data.database.dao.SocialDao
import com.rejwane.reelslocal.data.database.dao.UserDao
import com.rejwane.reelslocal.data.database.dao.VideoDao
import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.VideoLike
import com.rejwane.reelslocal.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSocialRepository @Inject constructor(
    private val db: AppDatabase,
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val videoDao: VideoDao,
    private val activityRepository: ActivityRepository
) : SocialRepository {

    override fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean> =
        socialDao.observeIsFollowing(followerId, followingId)

    override suspend fun toggleFollow(followerId: Long, followingId: Long): Boolean {
        if (followerId == followingId) return false
        val now = System.currentTimeMillis()
        val existing = db.withTransaction {
            val current = socialDao.getFollow(followerId, followingId)
            if (current != null) {
                socialDao.deleteFollow(followerId, followingId)
                userDao.adjustFollowingCount(followerId, -1)
                userDao.adjustFollowersCount(followingId, -1)
            } else {
                socialDao.insertFollow(Follow(followerId, followingId, now))
                userDao.adjustFollowingCount(followerId, 1)
                userDao.adjustFollowersCount(followingId, 1)
            }
            current
        }
        if (existing == null) {
            val targetName = userDao.getUser(followingId)?.displayName ?: "user"
            activityRepository.record(
                userId = followerId,
                actorId = followerId,
                type = ActivityType.FOLLOW_USER,
                targetUserId = followingId,
                message = "Followed $targetName"
            )
        }
        return existing == null
    }

    override fun observeFollowers(userId: Long): Flow<List<User>> = socialDao.observeFollowers(userId)

    override fun observeFollowing(userId: Long): Flow<List<User>> = socialDao.observeFollowing(userId)

    override fun observeAllFollows(): Flow<List<Follow>> = socialDao.observeAllFollows()

    override fun observeIsLiked(userId: Long, videoId: Long): Flow<Boolean> =
        socialDao.observeIsLiked(userId, videoId)

    override suspend fun toggleLike(userId: Long, videoId: Long): Boolean {
        val now = System.currentTimeMillis()
        val video = videoDao.getVideo(videoId)
        val alreadyLiked = db.withTransaction {
            val liked = socialDao.isLikedOnce(userId, videoId)
            if (liked) {
                socialDao.deleteVideoLike(userId, videoId)
                videoDao.adjustLikesCount(videoId, -1)
                video?.let { userDao.adjustLikesCount(it.ownerId, -1) }
            } else {
                socialDao.insertVideoLike(VideoLike(userId, videoId, now))
                videoDao.adjustLikesCount(videoId, 1)
                video?.let { userDao.adjustLikesCount(it.ownerId, 1) }
            }
            liked
        }
        if (!alreadyLiked && video != null) {
            val ownerName = userDao.getUser(video.ownerId)?.displayName ?: "unknown"
            activityRepository.record(
                userId = userId,
                actorId = userId,
                type = ActivityType.LIKE_VIDEO,
                targetVideoId = videoId,
                message = "Liked $ownerName's video"
            )
        }
        return !alreadyLiked
    }

    override fun observeAllVideoLikes(): Flow<List<VideoLike>> = socialDao.observeAllVideoLikes()

    override fun observeIsSaved(userId: Long, videoId: Long): Flow<Boolean> =
        socialDao.observeIsSaved(userId, videoId)

    override suspend fun toggleSave(userId: Long, videoId: Long): Boolean {
        val now = System.currentTimeMillis()
        val alreadySaved = db.withTransaction {
            val saved = socialDao.isSavedOnce(userId, videoId)
            if (saved) {
                socialDao.deleteSavedVideo(userId, videoId)
            } else {
                socialDao.insertSavedVideo(SavedVideo(userId, videoId, now))
            }
            saved
        }
        if (!alreadySaved) {
            val video = videoDao.getVideo(videoId)
            val ownerName = video?.let { userDao.getUser(it.ownerId)?.displayName } ?: "unknown"
            activityRepository.record(
                userId = userId,
                actorId = userId,
                type = ActivityType.SAVE_VIDEO,
                targetVideoId = videoId,
                message = "Saved $ownerName's video"
            )
        }
        return !alreadySaved
    }

    override fun observeAllSaved(): Flow<List<SavedVideo>> = socialDao.observeAllSavedVideos()

    override fun observeIsCommentLiked(userId: Long, commentId: Long): Flow<Boolean> =
        socialDao.observeIsCommentLiked(userId, commentId)

    override suspend fun toggleCommentLike(userId: Long, commentId: Long): Boolean {
        val now = System.currentTimeMillis()
        return db.withTransaction {
            val liked = socialDao.isCommentLikedOnce(userId, commentId)
            if (liked) {
                socialDao.deleteCommentLike(userId, commentId)
                db.commentDao().adjustLikeCount(commentId, -1)
            } else {
                socialDao.insertCommentLike(CommentLike(userId, commentId, now))
                db.commentDao().adjustLikeCount(commentId, 1)
            }
            !liked
        }
    }

    override suspend fun recordShare(userId: Long, videoId: Long) {
        val video = videoDao.getVideo(videoId) ?: return
        db.withTransaction {
            videoDao.adjustSharesCount(videoId, 1)
        }
        val ownerName = userDao.getUser(video.ownerId)?.displayName ?: "unknown"
        activityRepository.record(
            userId = userId,
            actorId = userId,
            type = ActivityType.SHARE_VIDEO,
            targetVideoId = videoId,
            message = "Shared $ownerName's video"
        )
    }

    override suspend fun getAllCommentLikesOnce(): List<CommentLike> =
        socialDao.getAllCommentLikesOnce()
}
