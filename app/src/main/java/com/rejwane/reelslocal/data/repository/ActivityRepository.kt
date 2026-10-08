package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.relation.ActivityDetails
import com.rejwane.reelslocal.data.model.ActivityType
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeForUser(userId: Long): Flow<List<ActivityDetails>>
    fun observeUnreadCount(userId: Long): Flow<Int>
    suspend fun markAllRead(userId: Long)
    suspend fun record(
        userId: Long,
        actorId: Long,
        type: ActivityType,
        targetVideoId: Long? = null,
        targetUserId: Long? = null,
        targetCommentId: Long? = null,
        message: String = ""
    )
    suspend fun deleteActivity(activityId: Long)
}
