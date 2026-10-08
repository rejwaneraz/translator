package com.rejwane.reelslocal.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.SocialRepository
import com.rejwane.reelslocal.data.repository.VideoRepository
import com.rejwane.reelslocal.ui.screens.feed.FeedInteractions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab { FOR_YOU, FOLLOWING }

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.FOR_YOU,
    val videos: List<VideoWithOwner> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    videoRepository: VideoRepository,
    private val socialRepository: SocialRepository,
    private val settings: SettingsRepository
) : ViewModel(), FeedInteractions {

    private val selectedTab = MutableStateFlow(HomeTab.FOR_YOU)

    val uiState: StateFlow<HomeUiState> =
        combine(selectedTab, settings.currentUserId) { tab, userId -> tab to userId }
            .flatMapLatest { (tab, userId) ->
                val feed = when (tab) {
                    HomeTab.FOR_YOU -> videoRepository.observeForYouFeedWithOwner()
                    HomeTab.FOLLOWING -> videoRepository.observeFollowingFeedWithOwner(userId)
                }
                feed.map { videos ->
                    HomeUiState(selectedTab = tab, videos = videos, isLoading = false)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    val currentUserId: StateFlow<Long> = settings.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_USER_ID)

    val muted: StateFlow<Boolean> = settings.muted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun selectTab(tab: HomeTab) {
        selectedTab.value = tab
    }

    // ---- Per-item reactive state for the action rail ----

    override fun observeIsLiked(videoId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsLiked(it, videoId) }

    override fun observeIsSaved(videoId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsSaved(it, videoId) }

    override fun observeIsFollowingOwner(ownerId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsFollowing(it, ownerId) }

    // ---- Mutations ----

    override fun toggleLike(videoId: Long) {
        viewModelScope.launch { socialRepository.toggleLike(currentUserId.value, videoId) }
    }

    override fun toggleSave(videoId: Long) {
        viewModelScope.launch { socialRepository.toggleSave(currentUserId.value, videoId) }
    }

    override fun toggleFollow(ownerId: Long) {
        viewModelScope.launch { socialRepository.toggleFollow(currentUserId.value, ownerId) }
    }

    override fun share(videoId: Long) {
        viewModelScope.launch { socialRepository.recordShare(currentUserId.value, videoId) }
    }

    override fun toggleMute() {
        viewModelScope.launch { settings.setMuted(!muted.value) }
    }

    private companion object {
        const val DEFAULT_USER_ID = 2L
    }
}
