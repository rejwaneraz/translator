package com.rejwane.reelslocal.ui.screens.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.SocialRepository
import com.rejwane.reelslocal.data.repository.UserRepository
import com.rejwane.reelslocal.data.repository.VideoRepository
import com.rejwane.reelslocal.ui.navigation.Routes
import com.rejwane.reelslocal.ui.screens.feed.FeedInteractions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the standalone video screen reached from Explore / Profile / Search taps. Loads a single
 * video with its owner and provides the same [FeedInteractions] surface as the Home feed, so both
 * reuse the identical full-screen pager.
 */
@HiltViewModel
class VideoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    videoRepository: VideoRepository,
    userRepository: UserRepository,
    private val socialRepository: SocialRepository,
    private val settings: SettingsRepository
) : ViewModel(), FeedInteractions {

    private val videoId: Long = savedStateHandle.get<Long>(Routes.VIDEO_ARG) ?: 0L

    val videos: StateFlow<List<VideoWithOwner>> = videoRepository.observeVideo(videoId)
        .flatMapLatest { video ->
            if (video == null) {
                flowOf(emptyList<VideoWithOwner>())
            } else {
                userRepository.observeUser(video.ownerId).map { owner ->
                    listOf(VideoWithOwner(video, owner))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUserId: StateFlow<Long> = settings.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_USER_ID)

    val muted: StateFlow<Boolean> = settings.muted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    override fun observeIsLiked(videoId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsLiked(it, videoId) }

    override fun observeIsSaved(videoId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsSaved(it, videoId) }

    override fun observeIsFollowingOwner(ownerId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsFollowing(it, ownerId) }

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
