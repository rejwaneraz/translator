package com.rejwane.reelslocal.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.model.AppThemeMode
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val autoplay: Boolean = true,
    val muted: Boolean = true,
    val showViewCounts: Boolean = true,
    val showComments: Boolean = true,
    val currentUserId: Long = 2L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(settings.themeMode, settings.autoplay, settings.muted) { theme, auto, mute ->
            Triple(theme, auto, mute)
        },
        combine(settings.showViewCounts, settings.showComments, settings.currentUserId) { views, comments, user ->
            Triple(views, comments, user)
        }
    ) { (theme, auto, mute), (views, comments, user) ->
        SettingsUiState(theme, auto, mute, views, comments, user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    val users: StateFlow<List<User>> = userRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setAutoplay(enabled: Boolean) = viewModelScope.launch { settings.setAutoplay(enabled) }
    fun setMuted(muted: Boolean) = viewModelScope.launch { settings.setMuted(muted) }
    fun setShowViewCounts(show: Boolean) = viewModelScope.launch { settings.setShowViewCounts(show) }
    fun setShowComments(show: Boolean) = viewModelScope.launch { settings.setShowComments(show) }
    fun switchUser(userId: Long) = viewModelScope.launch { settings.setCurrentUserId(userId) }
    fun reset() = viewModelScope.launch { settings.resetSettings() }
}
