package com.rejwane.reelslocal.data.backup

import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.entity.VideoLike
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRoundTripTest {

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private val user = User(
        id = 2, username = "rejwane", displayName = "Rejwane", bio = "hi",
        avatarPath = "asset:avatars/a.jpg", followersCount = 10, followingCount = 3,
        likesCount = 99, isVerified = true, isSystem = false, isActive = true, createdAt = 123
    )
    private val video = Video(
        id = 5, ownerId = 2, videoPath = "asset:videos/v.mp4", thumbnailPath = null,
        title = "T", caption = "C", hashtags = "a,b", musicTitle = "M", views = 1000,
        likesCount = 50, commentsCount = 5, sharesCount = 2, durationMs = 15000,
        isPublished = true, sortOrder = 1, createdAt = 456
    )
    private val comment = Comment(
        id = 9, videoId = 5, authorId = 2, text = "nice", likeCount = 4,
        isPinned = true, isHidden = false, sortOrder = 0, createdAt = 789
    )

    @Test
    fun `user survives dto round trip`() {
        assertEquals(user, user.toDto().toEntity())
    }

    @Test
    fun `video survives dto round trip`() {
        assertEquals(video, video.toDto().toEntity())
    }

    @Test
    fun `comment survives dto round trip`() {
        assertEquals(comment, comment.toDto().toEntity())
    }

    @Test
    fun `social rows survive dto round trip`() {
        val follow = Follow(2, 3, 1)
        val like = VideoLike(2, 5, 1)
        val saved = SavedVideo(2, 5, 1)
        val commentLike = CommentLike(2, 9, 1)
        assertEquals(follow, follow.toDto().toEntity())
        assertEquals(like, like.toDto().toEntity())
        assertEquals(saved, saved.toDto().toEntity())
        assertEquals(commentLike, commentLike.toDto().toEntity())
    }

    @Test
    fun `activity survives dto round trip`() {
        val activity = Activity(
            id = 1, userId = 2, actorId = 3, type = "LIKE_VIDEO", targetVideoId = 5,
            targetUserId = null, targetCommentId = null, message = "m", isRead = false, createdAt = 9
        )
        assertEquals(activity, activity.toDto().toEntity())
    }

    @Test
    fun `payload serializes to json and back`() {
        val payload = BackupPayload(
            exportedAt = 1,
            users = listOf(user.toDto()),
            videos = listOf(video.toDto()),
            comments = listOf(comment.toDto())
        )
        val encoded = json.encodeToString(BackupPayload.serializer(), payload)
        assertTrue(encoded.contains("rejwane"))
        val decoded = json.decodeFromString(BackupPayload.serializer(), encoded)
        assertEquals(payload, decoded)
        assertEquals(BACKUP_SCHEMA_VERSION, decoded.schemaVersion)
    }
}
