package com.rejwane.reelslocal.data.repository

import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.relation.UserWithVideoCount
import com.rejwane.reelslocal.data.model.RepoResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeAll(): Flow<List<User>>
    fun observeAllWithVideoCount(): Flow<List<UserWithVideoCount>>
    fun observeUser(userId: Long): Flow<User?>
    fun observeCurrentUser(): Flow<User?>
    suspend fun getUser(userId: Long): User?
    suspend fun getCurrentUserOnce(): User?
    fun searchUsers(query: String): Flow<List<User>>
    suspend fun isUsernameTaken(username: String, excludeUserId: Long = -1): Boolean
    suspend fun createUser(user: User): RepoResult<Long>
    suspend fun updateUser(user: User): RepoResult<Unit>
    suspend fun duplicateUser(userId: Long): RepoResult<Long>
    suspend fun deleteUser(userId: Long): RepoResult<Unit>
}
