package com.rejwane.reelslocal.data.model

/** Result wrapper for repository operations that can fail validation. */
sealed interface RepoResult<out T> {
    data class Success<T>(val data: T) : RepoResult<T>
    data class Error(val message: String) : RepoResult<Nothing>
}

inline fun <T> RepoResult<T>.onSuccess(action: (T) -> Unit): RepoResult<T> {
    if (this is RepoResult.Success) action(data)
    return this
}

inline fun <T> RepoResult<T>.onError(action: (String) -> Unit): RepoResult<T> {
    if (this is RepoResult.Error) action(message)
    return this
}
