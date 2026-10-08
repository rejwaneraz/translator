package com.rejwane.reelslocal.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String,
    val bio: String = "",
    val avatarPath: String? = null,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val likesCount: Long = 0,
    val isVerified: Boolean = false,
    val isSystem: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = 0
)
