package com.rejwane.reelslocal.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    videoRepository: VideoRepository
) : ViewModel() {

    /** Explore == every local video, ordered like the For You feed. */
    val videos: StateFlow<List<VideoWithOwner>> =
        videoRepository.observeForYouFeedWithOwner()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
