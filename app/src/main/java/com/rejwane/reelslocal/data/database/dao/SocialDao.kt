package com.rejwane.reelslocal.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.VideoLike
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // ---- Follows ----

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followingId = :followingId)")
    fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean>

    @Query("SELECT * FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun getFollow(followerId: Long, followingId: Long): Follow?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFollow(follow: Follow)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFollows(follows: List<Follow>)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: Long, followingId: Long)

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN follows f ON f.followerId = u.id
        WHERE f.followingId = :userId
        ORDER BY u.displayName
        """
    )
    fun observeFollowers(userId: Long): Flow<List<User>>

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN follows f ON f.followingId = u.id
        WHERE f.followerId = :userId
        ORDER BY u.displayName
        """
    )
    fun observeFollowing(userId: Long): Flow<List<User>>

    @Query("SELECT * FROM follows ORDER BY createdAt DESC")
    fun observeAllFollows(): Flow<List<Follow>>

    @Query("SELECT * FROM follows WHERE followerId = :userId")
    suspend fun getFollowingOnce(userId: Long): List<Follow>

    @Query("SELECT * FROM follows WHERE followingId = :userId")
    suspend fun getFollowersOnce(userId: Long): List<Follow>

    @Query("SELECT * FROM follows ORDER BY followerId, followingId")
    suspend fun getAllFollowsOnce(): List<Follow>

    @Query("SELECT COUNT(*) FROM follows")
    fun observeFollowCount(): Flow<Int>

    @Query("DELETE FROM follows")
    suspend fun deleteAllFollows()

    // ---- Video likes ----

    @Query("SELECT EXISTS(SELECT 1 FROM video_likes WHERE userId = :userId AND videoId = :videoId)")
    fun observeIsLiked(userId: Long, videoId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM video_likes WHERE userId = :userId AND videoId = :videoId)")
    suspend fun isLikedOnce(userId: Long, videoId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideoLike(like: VideoLike)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideoLikes(likes: List<VideoLike>)

    @Query("DELETE FROM video_likes WHERE userId = :userId AND videoId = :videoId")
    suspend fun deleteVideoLike(userId: Long, videoId: Long)

    @Query("SELECT * FROM video_likes ORDER BY createdAt DESC")
    fun observeAllVideoLikes(): Flow<List<VideoLike>>

    @Query("SELECT * FROM video_likes ORDER BY userId, videoId")
    suspend fun getAllVideoLikesOnce(): List<VideoLike>

    @Query("SELECT COUNT(*) FROM video_likes")
    fun observeVideoLikeCount(): Flow<Int>

    @Query("DELETE FROM video_likes")
    suspend fun deleteAllVideoLikes()

    // ---- Saved videos ----

    @Query("SELECT EXISTS(SELECT 1 FROM saved_videos WHERE userId = :userId AND videoId = :videoId)")
    fun observeIsSaved(userId: Long, videoId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_videos WHERE userId = :userId AND videoId = :videoId)")
    suspend fun isSavedOnce(userId: Long, videoId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedVideo(saved: SavedVideo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedVideos(saved: List<SavedVideo>)

    @Query("DELETE FROM saved_videos WHERE userId = :userId AND videoId = :videoId")
    suspend fun deleteSavedVideo(userId: Long, videoId: Long)

    @Query("SELECT * FROM saved_videos ORDER BY createdAt DESC")
    fun observeAllSavedVideos(): Flow<List<SavedVideo>>

    @Query("SELECT * FROM saved_videos ORDER BY userId, videoId")
    suspend fun getAllSavedOnce(): List<SavedVideo>

    @Query("SELECT COUNT(*) FROM saved_videos")
    fun observeSavedCount(): Flow<Int>

    @Query("DELETE FROM saved_videos")
    suspend fun deleteAllSavedVideos()

    // ---- Comment likes ----

    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE userId = :userId AND commentId = :commentId)")
    fun observeIsCommentLiked(userId: Long, commentId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE userId = :userId AND commentId = :commentId)")
    suspend fun isCommentLikedOnce(userId: Long, commentId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentLike(like: CommentLike)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentLikes(likes: List<CommentLike>)

    @Query("DELETE FROM comment_likes WHERE userId = :userId AND commentId = :commentId")
    suspend fun deleteCommentLike(userId: Long, commentId: Long)

    @Query("SELECT * FROM comment_likes")
    suspend fun getAllCommentLikesOnce(): List<CommentLike>

    @Query("DELETE FROM comment_likes")
    suspend fun deleteAllCommentLikes()
}
