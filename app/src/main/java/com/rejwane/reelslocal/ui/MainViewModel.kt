package com.rejwane.reelslocal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.model.AppThemeMode
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.ActivityRepository
import com.rejwane.reelslocal.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val currentUser: User? = null,
    val unreadCount: Int = 0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsRepository,
    userRepository: UserRepository,
    activityRepository: ActivityRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        settings.themeMode,
        settings.currentUserId.flatMapLatest { userRepository.observeUser(it) },
        settings.currentUserId.flatMapLatest { activityRepository.observeUnreadCount(it) }
    ) { theme, user, unread ->
        MainUiState(themeMode = theme, currentUser = user, unreadCount = unread)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())
}
