package com.rejwane.reelslocal.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "comment_likes",
    primaryKeys = ["userId", "commentId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Comment::class,
            parentColumns = ["id"],
            childColumns = ["commentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("commentId")]
)
data class CommentLike(
    val userId: Long,
    val commentId: Long,
    val createdAt: Long = 0
)
