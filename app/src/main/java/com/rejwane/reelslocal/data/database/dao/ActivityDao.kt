package com.rejwane.reelslocal.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.relation.ActivityDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Transaction
    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: Long): Flow<List<ActivityDetails>>

    @Query("SELECT COUNT(*) FROM activities WHERE userId = :userId AND isRead = 0")
    fun observeUnreadCount(userId: Long): Flow<Int>

    @Query("UPDATE activities SET isRead = 1 WHERE userId = :userId AND isRead = 0")
    suspend fun markAllRead(userId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(activity: Activity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteById(activityId: Long)

    @Query("SELECT * FROM activities ORDER BY id")
    suspend fun getAllOnce(): List<Activity>

    @Query("SELECT COUNT(*) FROM activities")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}
