package com.rejwane.reelslocal.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("actorId")]
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** User whose inbox this activity belongs to. */
    val userId: Long,
    /** User who performed the action (may equal userId for own actions). */
    val actorId: Long,
    /** One of ActivityType names, stored as string for readable JSON exports. */
    val type: String,
    val targetVideoId: Long? = null,
    val targetUserId: Long? = null,
    val targetCommentId: Long? = null,
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0
)
