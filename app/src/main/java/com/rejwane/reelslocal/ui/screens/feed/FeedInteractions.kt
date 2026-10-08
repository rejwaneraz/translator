package com.rejwane.reelslocal.ui.screens.feed

import kotlinx.coroutines.flow.Flow

/**
 * The interaction surface a video feed page needs. Implemented by any screen-scoped ViewModel that
 * hosts the feed (Home, standalone video detail), so [FeedVideoPage] stays reusable and testable.
 */
interface FeedInteractions {
    fun observeIsLiked(videoId: Long): Flow<Boolean>
    fun observeIsSaved(videoId: Long): Flow<Boolean>
    fun observeIsFollowingOwner(ownerId: Long): Flow<Boolean>
    fun toggleLike(videoId: Long)
    fun toggleSave(videoId: Long)
    fun toggleFollow(ownerId: Long)
    fun share(videoId: Long)
    fun toggleMute()
}
