package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.dao.ActivityDao
import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.relation.ActivityDetails
import com.rejwane.reelslocal.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) : ActivityRepository {

    override fun observeForUser(userId: Long): Flow<List<ActivityDetails>> =
        activityDao.observeForUser(userId)

    override fun observeUnreadCount(userId: Long): Flow<Int> =
        activityDao.observeUnreadCount(userId)

    override suspend fun markAllRead(userId: Long) = activityDao.markAllRead(userId)

    override suspend fun record(
        userId: Long,
        actorId: Long,
        type: ActivityType,
        targetVideoId: Long?,
        targetUserId: Long?,
        targetCommentId: Long?,
        message: String
    ) {
        activityDao.insert(
            Activity(
                userId = userId,
                actorId = actorId,
                type = type.name,
                targetVideoId = targetVideoId,
                targetUserId = targetUserId,
                targetCommentId = targetCommentId,
                message = message,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteActivity(activityId: Long) = activityDao.deleteById(activityId)
}
