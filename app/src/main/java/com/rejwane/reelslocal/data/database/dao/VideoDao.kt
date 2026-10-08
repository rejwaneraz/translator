package com.rejwane.reelslocal.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    /** For You feed: deterministic ranking — manual order first, then engagement, then recency. */
    @Query(
        """
        SELECT * FROM videos WHERE isPublished = 1
        ORDER BY sortOrder ASC, views DESC, createdAt DESC
        """
    )
    fun observeForYouFeed(): Flow<List<Video>>

    /** Following feed: videos from profiles the given user follows. */
    @Query(
        """
        SELECT v.* FROM videos v
        INNER JOIN follows f ON f.followingId = v.ownerId
        WHERE f.followerId = :userId AND v.isPublished = 1
        ORDER BY v.sortOrder ASC, v.views DESC, v.createdAt DESC
        """
    )
    fun observeFollowingFeed(userId: Long): Flow<List<Video>>

    @Query(
        """
        SELECT * FROM videos WHERE ownerId = :ownerId AND isPublished = 1
        ORDER BY sortOrder ASC, createdAt DESC
        """
    )
    fun observeVideosByUser(ownerId: Long): Flow<List<Video>>

    @Query(
        """
        SELECT v.* FROM videos v
        INNER JOIN video_likes vl ON vl.videoId = v.id
        WHERE vl.userId = :userId AND v.isPublished = 1
        ORDER BY vl.createdAt DESC
        """
    )
    fun observeLikedVideos(userId: Long): Flow<List<Video>>

    @Query(
        """
        SELECT v.* FROM videos v
        INNER JOIN saved_videos sv ON sv.videoId = v.id
        WHERE sv.userId = :userId AND v.isPublished = 1
        ORDER BY sv.createdAt DESC
        """
    )
    fun observeSavedVideos(userId: Long): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun observeVideo(videoId: Long): Flow<Video?>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideo(videoId: Long): Video?

    @Query("SELECT * FROM videos ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAllOnce(): List<Video>

    @Query(
        """
        SELECT * FROM videos
        WHERE isPublished = 1 AND (
            title LIKE '%' || :query || '%' OR
            caption LIKE '%' || :query || '%' OR
            hashtags LIKE '%' || :query || '%'
        )
        ORDER BY views DESC
        """
    )
    fun searchVideos(query: String): Flow<List<Video>>

    @Query(
        """
        SELECT * FROM videos
        WHERE isPublished = 1 AND hashtags LIKE '%' || :hashtag || '%'
        ORDER BY views DESC
        """
    )
    fun observeVideosByHashtag(hashtag: String): Flow<List<Video>>

    @Transaction
    @Query("SELECT * FROM videos ORDER BY sortOrder ASC, views DESC, createdAt DESC")
    fun observeAllWithOwner(): Flow<List<VideoWithOwner>>

    @Transaction
    @Query(
        """
        SELECT * FROM videos WHERE isPublished = 1
        ORDER BY sortOrder ASC, views DESC, createdAt DESC
        """
    )
    fun observeForYouFeedWithOwner(): Flow<List<VideoWithOwner>>

    @Transaction
    @Query(
        """
        SELECT v.* FROM videos v
        INNER JOIN follows f ON f.followingId = v.ownerId
        WHERE f.followerId = :userId AND v.isPublished = 1
        ORDER BY v.sortOrder ASC, v.views DESC, v.createdAt DESC
        """
    )
    fun observeFollowingFeedWithOwner(userId: Long): Flow<List<VideoWithOwner>>

    @Transaction
    @Query("SELECT * FROM videos WHERE ownerId = :ownerId ORDER BY sortOrder ASC, createdAt DESC")
    fun observeWithOwnerByUser(ownerId: Long): Flow<List<VideoWithOwner>>

    @Transaction
    @Query(
        """
        SELECT * FROM videos
        WHERE title LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' OR hashtags LIKE '%' || :query || '%'
        ORDER BY views DESC
        """
    )
    fun searchVideosWithOwner(query: String): Flow<List<VideoWithOwner>>

    @Query("SELECT COUNT(*) FROM videos")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(video: Video): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<Video>)

    @Update
    suspend fun update(video: Video)

    @Delete
    suspend fun delete(video: Video)

    @Query("DELETE FROM videos")
    suspend fun deleteAll()

    @Query("UPDATE videos SET likesCount = MAX(0, likesCount + :delta) WHERE id = :videoId")
    suspend fun adjustLikesCount(videoId: Long, delta: Int)

    @Query("UPDATE videos SET commentsCount = MAX(0, commentsCount + :delta) WHERE id = :videoId")
    suspend fun adjustCommentsCount(videoId: Long, delta: Int)

    @Query("UPDATE videos SET sharesCount = MAX(0, sharesCount + :delta) WHERE id = :videoId")
    suspend fun adjustSharesCount(videoId: Long, delta: Int)

    @Query("UPDATE videos SET views = MAX(0, :views) WHERE id = :videoId")
    suspend fun setViews(videoId: Long, views: Long)

    @Query("UPDATE videos SET ownerId = :newOwnerId WHERE ownerId = :oldOwnerId")
    suspend fun reassignOwner(oldOwnerId: Long, newOwnerId: Long)
}
