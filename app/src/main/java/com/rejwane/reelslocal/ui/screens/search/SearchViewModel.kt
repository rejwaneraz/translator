package com.rejwane.reelslocal.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.repository.UserRepository
import com.rejwane.reelslocal.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val users: List<User> = emptyList(),
    val videos: List<VideoWithOwner> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val searched: Boolean = false
) {
    val isEmpty: Boolean get() = users.isEmpty() && videos.isEmpty() && hashtags.isEmpty()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    userRepository: UserRepository,
    videoRepository: VideoRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = query
        .flatMapLatest { raw ->
            val q = raw.trim()
            if (q.isEmpty()) {
                flowOf(SearchUiState(query = raw))
            } else {
                combine(
                    userRepository.searchUsers(q),
                    videoRepository.searchVideosWithOwner(q)
                ) { users, videos ->
                    val tags = videos
                        .flatMap { it.video.hashtags.split(",") }
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.contains(q, ignoreCase = true) }
                        .distinct()
                    SearchUiState(
                        query = raw,
                        users = users,
                        videos = videos,
                        hashtags = tags,
                        searched = true
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun clear() {
        query.value = ""
    }
}
