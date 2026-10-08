package com.rejwane.reelslocal.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.SocialRepository
import com.rejwane.reelslocal.data.repository.UserRepository
import com.rejwane.reelslocal.data.repository.VideoRepository
import com.rejwane.reelslocal.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileTab { VIDEOS, LIKED, SAVED, FOLLOWING }

sealed interface ProfileContent {
    data class Videos(val items: List<Video>) : ProfileContent
    data class Users(val items: List<User>) : ProfileContent
    data object Empty : ProfileContent
}

data class ProfileUiState(
    val user: User? = null,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.VIDEOS,
    val content: ProfileContent = ProfileContent.Empty,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository,
    private val socialRepository: SocialRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    /** Present when opened via profile/{userId}; null means "the current local user". */
    private val argUserId: Long? = savedStateHandle.get<Long>(Routes.PROFILE_DETAIL_ARG)

    private val selectedTab = MutableStateFlow(ProfileTab.VIDEOS)

    private val userIdFlow: Flow<Long> =
        argUserId?.let { flowOf(it) } ?: settings.currentUserId

    val profileUserId: StateFlow<Long> = userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), argUserId ?: DEFAULT_USER_ID)

    val currentUserId: StateFlow<Long> = settings.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_USER_ID)

    val uiState: StateFlow<ProfileUiState> = userIdFlow.flatMapLatest { uid ->
        combine(
            userRepository.observeUser(uid),
            settings.currentUserId,
            selectedTab,
            isFollowingFlow(uid)
        ) { user, currentId, tab, following ->
            Quad(user, currentId, tab, following)
        }.flatMapLatest { (user, currentId, tab, following) ->
            contentFlow(uid, tab).map { content ->
                ProfileUiState(
                    user = user,
                    isCurrentUser = uid == currentId,
                    isFollowing = following,
                    selectedTab = tab,
                    content = content,
                    isLoading = false
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private fun isFollowingFlow(uid: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { currentId ->
            if (currentId == uid) flowOf(false)
            else socialRepository.observeIsFollowing(currentId, uid)
        }

    private fun contentFlow(uid: Long, tab: ProfileTab): Flow<ProfileContent> = when (tab) {
        ProfileTab.VIDEOS -> videoRepository.observeVideosByUser(uid).map { ProfileContent.Videos(it) }
        ProfileTab.LIKED -> videoRepository.observeLikedVideos(uid).map { ProfileContent.Videos(it) }
        ProfileTab.SAVED -> videoRepository.observeSavedVideos(uid).map { ProfileContent.Videos(it) }
        ProfileTab.FOLLOWING -> socialRepository.observeFollowing(uid).map { ProfileContent.Users(it) }
    }

    fun selectTab(tab: ProfileTab) {
        selectedTab.value = tab
    }

    fun toggleFollow() = viewModelScope.launch {
        socialRepository.toggleFollow(currentUserId.value, profileUserId.value)
    }

    private companion object {
        const val DEFAULT_USER_ID = 2L
    }
}

private data class Quad(
    val user: User?,
    val currentId: Long,
    val tab: ProfileTab,
    val following: Boolean
)
