package com.rejwane.reelslocal.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.relation.ActivityDetails
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    settings: SettingsRepository
) : ViewModel() {

    private val currentUserId: StateFlow<Long> = settings.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_USER_ID)

    val activities: StateFlow<List<ActivityDetails>> = currentUserId
        .flatMapLatest { activityRepository.observeForUser(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = currentUserId
        .flatMapLatest { activityRepository.observeUnreadCount(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun markAllRead() {
        viewModelScope.launch { activityRepository.markAllRead(currentUserId.value) }
    }

    fun delete(activityId: Long) {
        viewModelScope.launch { activityRepository.deleteActivity(activityId) }
    }

    private companion object {
        const val DEFAULT_USER_ID = 2L
    }
}
