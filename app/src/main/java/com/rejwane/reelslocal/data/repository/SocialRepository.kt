package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.VideoLike
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean>
    suspend fun toggleFollow(followerId: Long, followingId: Long): Boolean
    fun observeFollowers(userId: Long): Flow<List<User>>
    fun observeFollowing(userId: Long): Flow<List<User>>
    fun observeAllFollows(): Flow<List<Follow>>

    fun observeIsLiked(userId: Long, videoId: Long): Flow<Boolean>
    /** Returns the new liked state. */
    suspend fun toggleLike(userId: Long, videoId: Long): Boolean
    fun observeAllVideoLikes(): Flow<List<VideoLike>>

    fun observeIsSaved(userId: Long, videoId: Long): Flow<Boolean>
    /** Returns the new saved state. */
    suspend fun toggleSave(userId: Long, videoId: Long): Boolean
    fun observeAllSaved(): Flow<List<SavedVideo>>

    fun observeIsCommentLiked(userId: Long, commentId: Long): Flow<Boolean>
    /** Returns the new liked state. */
    suspend fun toggleCommentLike(userId: Long, commentId: Long): Boolean

    suspend fun recordShare(userId: Long, videoId: Long)
    suspend fun getAllCommentLikesOnce(): List<CommentLike>
}
