package com.rejwane.reelslocal.data.backup

import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.entity.VideoLike

fun User.toDto() = UserDto(
    id = id, username = username, displayName = displayName, bio = bio,
    avatarPath = avatarPath, followersCount = followersCount, followingCount = followingCount,
    likesCount = likesCount, isVerified = isVerified, isSystem = isSystem,
    isActive = isActive, createdAt = createdAt
)

fun UserDto.toEntity() = User(
    id = id, username = username, displayName = displayName, bio = bio,
    avatarPath = avatarPath, followersCount = followersCount, followingCount = followingCount,
    likesCount = likesCount, isVerified = isVerified, isSystem = isSystem,
    isActive = isActive, createdAt = createdAt
)

fun Video.toDto() = VideoDto(
    id = id, ownerId = ownerId, videoPath = videoPath, thumbnailPath = thumbnailPath,
    title = title, caption = caption, hashtags = hashtags, musicTitle = musicTitle,
    views = views, likesCount = likesCount, commentsCount = commentsCount,
    sharesCount = sharesCount, durationMs = durationMs, isPublished = isPublished,
    sortOrder = sortOrder, createdAt = createdAt
)

fun VideoDto.toEntity() = Video(
    id = id, ownerId = ownerId, videoPath = videoPath, thumbnailPath = thumbnailPath,
    title = title, caption = caption, hashtags = hashtags, musicTitle = musicTitle,
    views = views, likesCount = likesCount, commentsCount = commentsCount,
    sharesCount = sharesCount, durationMs = durationMs, isPublished = isPublished,
    sortOrder = sortOrder, createdAt = createdAt
)

fun Comment.toDto() = CommentDto(
    id = id, videoId = videoId, authorId = authorId, text = text, likeCount = likeCount,
    isPinned = isPinned, isHidden = isHidden, sortOrder = sortOrder, createdAt = createdAt
)

fun CommentDto.toEntity() = Comment(
    id = id, videoId = videoId, authorId = authorId, text = text, likeCount = likeCount,
    isPinned = isPinned, isHidden = isHidden, sortOrder = sortOrder, createdAt = createdAt
)

fun Follow.toDto() = FollowDto(followerId, followingId, createdAt)
fun FollowDto.toEntity() = Follow(followerId, followingId, createdAt)

fun VideoLike.toDto() = VideoLikeDto(userId, videoId, createdAt)
fun VideoLikeDto.toEntity() = VideoLike(userId, videoId, createdAt)

fun SavedVideo.toDto() = SavedVideoDto(userId, videoId, createdAt)
fun SavedVideoDto.toEntity() = SavedVideo(userId, videoId, createdAt)

fun CommentLike.toDto() = CommentLikeDto(userId, commentId, createdAt)
fun CommentLikeDto.toEntity() = CommentLike(userId, commentId, createdAt)

fun Activity.toDto() = ActivityDto(
    id = id, userId = userId, actorId = actorId, type = type, targetVideoId = targetVideoId,
    targetUserId = targetUserId, targetCommentId = targetCommentId, message = message,
    isRead = isRead, createdAt = createdAt
)

fun ActivityDto.toEntity() = Activity(
    id = id, userId = userId, actorId = actorId, type = type, targetVideoId = targetVideoId,
    targetUserId = targetUserId, targetCommentId = targetCommentId, message = message,
    isRead = isRead, createdAt = createdAt
)
