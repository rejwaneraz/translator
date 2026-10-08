package com.rejwane.reelslocal.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followingId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["followingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("followerId"), Index("followingId")]
)
data class Follow(
    val followerId: Long,
    val followingId: Long,
    val createdAt: Long = 0
)
