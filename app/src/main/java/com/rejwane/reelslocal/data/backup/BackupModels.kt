package com.rejwane.reelslocal.data.backup

import kotlinx.serialization.Serializable

/**
 * Versioned, self-contained snapshot of the local library.
 * DTOs are deliberately separate from the Room entities so the on-disk backup
 * format stays stable even as the schema evolves.
 */
const val BACKUP_SCHEMA_VERSION = 1

@Serializable
data class BackupPayload(
    val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    val exportedAt: Long = 0,
    val users: List<UserDto> = emptyList(),
    val videos: List<VideoDto> = emptyList(),
    val comments: List<CommentDto> = emptyList(),
    val follows: List<FollowDto> = emptyList(),
    val videoLikes: List<VideoLikeDto> = emptyList(),
    val savedVideos: List<SavedVideoDto> = emptyList(),
    val commentLikes: List<CommentLikeDto> = emptyList(),
    val activities: List<ActivityDto> = emptyList()
)

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarPath: String?,
    val followersCount: Long,
    val followingCount: Long,
    val likesCount: Long,
    val isVerified: Boolean,
    val isSystem: Boolean,
    val isActive: Boolean,
    val createdAt: Long
)

@Serializable
data class VideoDto(
    val id: Long,
    val ownerId: Long,
    val videoPath: String,
    val thumbnailPath: String?,
    val title: String,
    val caption: String,
    val hashtags: String,
    val musicTitle: String,
    val views: Long,
    val likesCount: Long,
    val commentsCount: Long,
    val sharesCount: Long,
    val durationMs: Long,
    val isPublished: Boolean,
    val sortOrder: Int,
    val createdAt: Long
)

@Serializable
data class CommentDto(
    val id: Long,
    val videoId: Long,
    val authorId: Long,
    val text: String,
    val likeCount: Long,
    val isPinned: Boolean,
    val isHidden: Boolean,
    val sortOrder: Int,
    val createdAt: Long
)

@Serializable
data class FollowDto(
    val followerId: Long,
    val followingId: Long,
    val createdAt: Long
)

@Serializable
data class VideoLikeDto(
    val userId: Long,
    val videoId: Long,
    val createdAt: Long
)

@Serializable
data class SavedVideoDto(
    val userId: Long,
    val videoId: Long,
    val createdAt: Long
)

@Serializable
data class CommentLikeDto(
    val userId: Long,
    val commentId: Long,
    val createdAt: Long
)

@Serializable
data class ActivityDto(
    val id: Long,
    val userId: Long,
    val actorId: Long,
    val type: String,
    val targetVideoId: Long?,
    val targetUserId: Long?,
    val targetCommentId: Long?,
    val message: String,
    val isRead: Boolean,
    val createdAt: Long
)
