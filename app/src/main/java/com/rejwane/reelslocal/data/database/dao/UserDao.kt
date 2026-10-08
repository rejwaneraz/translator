package com.rejwane.reelslocal.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.relation.UserWithVideoCount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE isSystem = 0 ORDER BY displayName")
    fun observeAll(): Flow<List<User>>

    @Query(
        """
        SELECT *, (SELECT COUNT(*) FROM videos v WHERE v.ownerId = users.id) AS videoCount
        FROM users WHERE isSystem = 0
        ORDER BY displayName
        """
    )
    fun observeAllWithVideoCount(): Flow<List<UserWithVideoCount>>

    @Query("SELECT * FROM users ORDER BY id")
    suspend fun getAllOnce(): List<User>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: Long): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE isSystem = 1 LIMIT 1")
    suspend fun getSystemUser(): User?

    @Query(
        """
        SELECT * FROM users
        WHERE isSystem = 0 AND (username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%')
        ORDER BY displayName
        """
    )
    fun searchUsers(query: String): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users WHERE username = :username AND id != :excludeId")
    suspend fun usernameTakenCount(username: String, excludeId: Long): Int

    @Query("SELECT COUNT(*) FROM users")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getCountOnce(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("UPDATE users SET followersCount = MAX(0, followersCount + :delta) WHERE id = :userId")
    suspend fun adjustFollowersCount(userId: Long, delta: Int)

    @Query("UPDATE users SET followingCount = MAX(0, followingCount + :delta) WHERE id = :userId")
    suspend fun adjustFollowingCount(userId: Long, delta: Int)

    @Query("UPDATE users SET likesCount = MAX(0, likesCount + :delta) WHERE id = :userId")
    suspend fun adjustLikesCount(userId: Long, delta: Int)

    @Query("UPDATE users SET avatarPath = :avatarPath WHERE id = :userId")
    suspend fun updateAvatar(userId: Long, avatarPath: String?)
}
