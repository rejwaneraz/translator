package com.rejwane.reelslocal.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.relation.CommentWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    /** Comments shown in the comment sheet: hidden excluded, pinned first. */
    @Transaction
    @Query(
        """
        SELECT * FROM comments
        WHERE videoId = :videoId AND isHidden = 0
        ORDER BY isPinned DESC, sortOrder ASC, createdAt ASC
        """
    )
    fun observeCommentsForVideo(videoId: Long): Flow<List<CommentWithAuthor>>

    @Transaction
    @Query("SELECT * FROM comments ORDER BY createdAt DESC")
    fun observeAllWithAuthor(): Flow<List<CommentWithAuthor>>

    @Transaction
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createdAt DESC")
    fun observeByVideo(videoId: Long): Flow<List<CommentWithAuthor>>

    @Transaction
    @Query("SELECT * FROM comments WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun observeByAuthor(authorId: Long): Flow<List<CommentWithAuthor>>

    @Transaction
    @Query(
        """
        SELECT c.* FROM comments c
        INNER JOIN users u ON u.id = c.authorId
        WHERE c.text LIKE '%' || :query || '%' OR u.displayName LIKE '%' || :query || '%' OR u.username LIKE '%' || :query || '%'
        ORDER BY c.createdAt DESC
        """
    )
    fun searchComments(query: String): Flow<List<CommentWithAuthor>>

    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getComment(commentId: Long): Comment?

    @Query("SELECT * FROM comments ORDER BY id")
    suspend fun getAllOnce(): List<Comment>

    @Query("SELECT COUNT(*) FROM comments WHERE videoId = :videoId AND isHidden = 0")
    fun observeVisibleCount(videoId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM comments")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(comment: Comment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<Comment>)

    @Update
    suspend fun update(comment: Comment)

    @Delete
    suspend fun delete(comment: Comment)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteById(commentId: Long)

    @Query("DELETE FROM comments")
    suspend fun deleteAll()

    @Query("UPDATE comments SET isPinned = :pinned WHERE id = :commentId")
    suspend fun setPinned(commentId: Long, pinned: Boolean)

    @Query("UPDATE comments SET isHidden = :hidden WHERE id = :commentId")
    suspend fun setHidden(commentId: Long, hidden: Boolean)

    @Query("UPDATE comments SET likeCount = MAX(0, likeCount + :delta) WHERE id = :commentId")
    suspend fun adjustLikeCount(commentId: Long, delta: Int)

    @Query("UPDATE comments SET authorId = :newAuthorId WHERE authorId = :oldAuthorId")
    suspend fun reassignAuthor(oldAuthorId: Long, newAuthorId: Long)
}
