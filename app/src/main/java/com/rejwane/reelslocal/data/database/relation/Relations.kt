package com.rejwane.reelslocal.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video

data class VideoWithOwner(
    @Embedded val video: Video,
    @Relation(parentColumn = "ownerId", entityColumn = "id")
    val owner: User?
)

data class CommentWithAuthor(
    @Embedded val comment: Comment,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: User?
)

data class ActivityDetails(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "actorId", entityColumn = "id")
    val actor: User?,
    @Relation(parentColumn = "targetVideoId", entityColumn = "id")
    val targetVideo: Video?
)

data class UserWithVideoCount(
    @Embedded val user: User,
    val videoCount: Int
)
