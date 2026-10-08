package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.model.RepoResult
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun observeForYouFeed(): Flow<List<Video>>
    fun observeFollowingFeed(userId: Long): Flow<List<Video>>
    fun observeVideosByUser(ownerId: Long): Flow<List<Video>>
    fun observeLikedVideos(userId: Long): Flow<List<Video>>
    fun observeSavedVideos(userId: Long): Flow<List<Video>>
    fun observeVideo(videoId: Long): Flow<Video?>
    suspend fun getVideo(videoId: Long): Video?
    fun searchVideos(query: String): Flow<List<Video>>
    fun observeVideosByHashtag(hashtag: String): Flow<List<Video>>
    fun observeAllWithOwner(): Flow<List<VideoWithOwner>>
    fun observeForYouFeedWithOwner(): Flow<List<VideoWithOwner>>
    fun observeFollowingFeedWithOwner(userId: Long): Flow<List<VideoWithOwner>>
    fun observeWithOwnerByUser(ownerId: Long): Flow<List<VideoWithOwner>>
    fun searchVideosWithOwner(query: String): Flow<List<VideoWithOwner>>
    suspend fun createVideo(video: Video): RepoResult<Long>
    suspend fun updateVideo(video: Video): RepoResult<Unit>
    suspend fun duplicateVideo(videoId: Long): RepoResult<Long>
    suspend fun deleteVideo(videoId: Long): RepoResult<Unit>
    suspend fun setStats(videoId: Long, views: Long, likes: Long, comments: Long, shares: Long): RepoResult<Unit>
}
