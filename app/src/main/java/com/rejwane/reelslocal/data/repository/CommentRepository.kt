package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.relation.CommentWithAuthor
import com.rejwane.reelslocal.data.model.RepoResult
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun observeCommentsForVideo(videoId: Long): Flow<List<CommentWithAuthor>>
    fun observeAllWithAuthor(): Flow<List<CommentWithAuthor>>
    fun observeByVideo(videoId: Long): Flow<List<CommentWithAuthor>>
    fun observeByAuthor(authorId: Long): Flow<List<CommentWithAuthor>>
    fun searchComments(query: String): Flow<List<CommentWithAuthor>>
    suspend fun getComment(commentId: Long): Comment?
    suspend fun addComment(videoId: Long, authorId: Long, text: String): RepoResult<Long>
    suspend fun updateComment(comment: Comment): RepoResult<Unit>
    suspend fun deleteComment(commentId: Long): RepoResult<Unit>
    suspend fun setPinned(commentId: Long, pinned: Boolean)
    suspend fun setHidden(commentId: Long, hidden: Boolean)
}
