package com.rejwane.reelslocal.data.repository

import androidx.room.withTransaction
import com.rejwane.reelslocal.data.database.AppDatabase
import com.rejwane.reelslocal.data.database.dao.UserDao
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.relation.UserWithVideoCount
import com.rejwane.reelslocal.data.model.RepoResult
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserRepository @Inject constructor(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val settings: SettingsRepository
) : UserRepository {

    override fun observeAll(): Flow<List<User>> = userDao.observeAll()

    override fun observeAllWithVideoCount(): Flow<List<UserWithVideoCount>> =
        userDao.observeAllWithVideoCount()

    override fun observeUser(userId: Long): Flow<User?> = userDao.observeUser(userId)

    override fun observeCurrentUser(): Flow<User?> =
        settings.currentUserId.flatMapLatest { id ->
            userDao.observeUser(id)
        }

    override suspend fun getUser(userId: Long): User? = userDao.getUser(userId)

    override suspend fun getCurrentUserOnce(): User? =
        userDao.getUser(settings.currentUserId.first())

    override fun searchUsers(query: String): Flow<List<User>> =
        userDao.searchUsers(query.trim())

    override suspend fun isUsernameTaken(username: String, excludeUserId: Long): Boolean =
        userDao.usernameTakenCount(username.trim(), excludeUserId) > 0

    override suspend fun createUser(user: User): RepoResult<Long> {
        validate(user)?.let { return RepoResult.Error(it) }
        if (isUsernameTaken(user.username)) {
            return RepoResult.Error("Username @${user.username.trim()} already exists")
        }
        val id = userDao.insert(
            user.copy(
                username = user.username.trim(),
                displayName = user.displayName.trim(),
                followersCount = user.followersCount.coerceAtLeast(0),
                followingCount = user.followingCount.coerceAtLeast(0),
                likesCount = user.likesCount.coerceAtLeast(0)
            )
        )
        return RepoResult.Success(id)
    }

    override suspend fun updateUser(user: User): RepoResult<Unit> {
        validate(user)?.let { return RepoResult.Error(it) }
        if (isUsernameTaken(user.username, excludeUserId = user.id)) {
            return RepoResult.Error("Username @${user.username.trim()} already exists")
        }
        userDao.update(
            user.copy(
                username = user.username.trim(),
                displayName = user.displayName.trim(),
                followersCount = user.followersCount.coerceAtLeast(0),
                followingCount = user.followingCount.coerceAtLeast(0),
                likesCount = user.likesCount.coerceAtLeast(0)
            )
        )
        return RepoResult.Success(Unit)
    }

    override suspend fun duplicateUser(userId: Long): RepoResult<Long> {
        val source = userDao.getUser(userId) ?: return RepoResult.Error("User not found")
        val copy = source.copy(
            id = 0,
            username = uniqueUsername(source.username),
            displayName = "${source.displayName} (copy)",
            createdAt = System.currentTimeMillis()
        )
        return RepoResult.Success(userDao.insert(copy))
    }

    override suspend fun deleteUser(userId: Long): RepoResult<Unit> {
        val user = userDao.getUser(userId) ?: return RepoResult.Error("User not found")
        if (user.isSystem) return RepoResult.Error("The system user cannot be deleted")

        db.withTransaction {
            // Orphan-safe: content is reassigned to the system user instead of cascading away.
            val systemUser = userDao.getSystemUser()
            if (systemUser != null) {
                db.videoDao().reassignOwner(userId, systemUser.id)
                db.commentDao().reassignAuthor(userId, systemUser.id)
            }

            // Keep cached counters coherent for the users this one followed / was followed by.
            val following = db.socialDao().getFollowingOnce(userId)
            following.forEach { follow ->
                userDao.adjustFollowersCount(follow.followingId, -1)
            }
            val followers = db.socialDao().getFollowersOnce(userId)
            followers.forEach { follow ->
                userDao.adjustFollowingCount(follow.followerId, -1)
            }

            userDao.delete(user)
        }

        // If the deleted user was the current user, fall back to Rejwane, then system.
        val currentId = settings.currentUserId.first()
        if (currentId == userId) {
            val fallback = userDao.getUserByUsername(DEFAULT_USERNAME) ?: userDao.getSystemUser()
            fallback?.let { settings.setCurrentUserId(it.id) }
        }
        return RepoResult.Success(Unit)
    }

    private fun validate(user: User): String? = when {
        user.username.isBlank() -> "Username cannot be empty"
        user.username.trim().length > 30 -> "Username must be 30 characters or fewer"
        user.displayName.isBlank() -> "Display name cannot be empty"
        user.displayName.trim().length > 60 -> "Display name must be 60 characters or fewer"
        user.bio.length > 300 -> "Bio must be 300 characters or fewer"
        else -> null
    }

    private suspend fun uniqueUsername(base: String): String {
        var candidate = "${base}_copy"
        var suffix = 2
        while (isUsernameTaken(candidate)) {
            candidate = "${base}_copy$suffix"
            suffix++
        }
        return candidate
    }

    companion object {
        const val DEFAULT_USERNAME = "rejwane"
    }
}
