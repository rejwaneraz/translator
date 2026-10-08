package com.rejwane.reelslocal.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class Video(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val videoPath: String,
    val thumbnailPath: String? = null,
    val title: String = "",
    val caption: String = "",
    val hashtags: String = "",
    val musicTitle: String = "",
    val views: Long = 0,
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    val sharesCount: Long = 0,
    val durationMs: Long = 0,
    val isPublished: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0
)
