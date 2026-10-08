package com.rejwane.reelslocal.data.repository

import androidx.room.withTransaction
import com.rejwane.reelslocal.data.database.AppDatabase
import com.rejwane.reelslocal.data.database.dao.CommentDao
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.relation.CommentWithAuthor
import com.rejwane.reelslocal.data.model.ActivityType
import com.rejwane.reelslocal.data.model.RepoResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCommentRepository @Inject constructor(
    private val db: AppDatabase,
    private val commentDao: CommentDao,
    private val activityRepository: ActivityRepository
) : CommentRepository {

    override fun observeCommentsForVideo(videoId: Long): Flow<List<CommentWithAuthor>> =
        commentDao.observeCommentsForVideo(videoId)

    override fun observeAllWithAuthor(): Flow<List<CommentWithAuthor>> =
        commentDao.observeAllWithAuthor()

    override fun observeByVideo(videoId: Long): Flow<List<CommentWithAuthor>> =
        commentDao.observeByVideo(videoId)

    override fun observeByAuthor(authorId: Long): Flow<List<CommentWithAuthor>> =
        commentDao.observeByAuthor(authorId)

    override fun searchComments(query: String): Flow<List<CommentWithAuthor>> =
        commentDao.searchComments(query.trim())

    override suspend fun getComment(commentId: Long): Comment? = commentDao.getComment(commentId)

    override suspend fun addComment(videoId: Long, authorId: Long, text: String): RepoResult<Long> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return RepoResult.Error("Comment cannot be empty")
        if (trimmed.length > MAX_COMMENT_LENGTH) {
            return RepoResult.Error("Comment must be $MAX_COMMENT_LENGTH characters or fewer")
        }
        val video = db.videoDao().getVideo(videoId) ?: return RepoResult.Error("Video not found")
        val author = db.userDao().getUser(authorId) ?: return RepoResult.Error("Author not found")

        val commentId = db.withTransaction {
            val id = commentDao.insert(
                Comment(
                    videoId = videoId,
                    authorId = authorId,
                    text = trimmed,
                    createdAt = System.currentTimeMillis()
                )
            )
            db.videoDao().adjustCommentsCount(videoId, 1)
            id
        }
        activityRepository.record(
            userId = authorId,
            actorId = authorId,
            type = ActivityType.COMMENT_VIDEO,
            targetVideoId = videoId,
            targetCommentId = commentId,
            message = "Commented on ${ownerLabel(video.ownerId)}'s video"
        )
        return RepoResult.Success(commentId)
    }

    override suspend fun updateComment(comment: Comment): RepoResult<Unit> {
        val trimmed = comment.text.trim()
        if (trimmed.isEmpty()) return RepoResult.Error("Comment cannot be empty")
        if (trimmed.length > MAX_COMMENT_LENGTH) {
            return RepoResult.Error("Comment must be $MAX_COMMENT_LENGTH characters or fewer")
        }
        if (commentDao.getComment(comment.id) == null) return RepoResult.Error("Comment not found")
        if (db.userDao().getUser(comment.authorId) == null) return RepoResult.Error("Invalid author")
        if (db.videoDao().getVideo(comment.videoId) == null) return RepoResult.Error("Invalid video")
        commentDao.update(
            comment.copy(text = trimmed, likeCount = comment.likeCount.coerceAtLeast(0))
        )
        return RepoResult.Success(Unit)
    }

    override suspend fun deleteComment(commentId: Long): RepoResult<Unit> {
        val comment = commentDao.getComment(commentId) ?: return RepoResult.Error("Comment not found")
        db.withTransaction {
            commentDao.deleteById(commentId)
            db.videoDao().adjustCommentsCount(comment.videoId, -1)
        }
        return RepoResult.Success(Unit)
    }

    override suspend fun setPinned(commentId: Long, pinned: Boolean) =
        commentDao.setPinned(commentId, pinned)

    override suspend fun setHidden(commentId: Long, hidden: Boolean) =
        commentDao.setHidden(commentId, hidden)

    private suspend fun ownerLabel(ownerId: Long): String =
        db.userDao().getUser(ownerId)?.displayName ?: "unknown"

    companion object {
        const val MAX_COMMENT_LENGTH = 500
    }
}
