package com.rejwane.reelslocal.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Video::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("videoId"), Index("authorId")]
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: Long,
    val authorId: Long,
    val text: String,
    val likeCount: Long = 0,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0
)
