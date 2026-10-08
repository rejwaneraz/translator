package com.rejwane.reelslocal.ui.screens.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.database.relation.CommentWithAuthor
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.CommentRepository
import com.rejwane.reelslocal.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentUiState(
    val videoId: Long? = null,
    val comments: List<CommentWithAuthor> = emptyList()
)

/**
 * Video-agnostic comment sheet controller. A screen opens it for a specific video via [open]; the
 * same instance then streams that video's comments, so it can be reused across the feed and the
 * standalone video screen without per-video ViewModel instantiation.
 */
@HiltViewModel
class CommentSheetViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val socialRepository: SocialRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _videoId = MutableStateFlow<Long?>(null)

    /** Non-null while the sheet should be visible. */
    val videoId: StateFlow<Long?> = _videoId

    val uiState: StateFlow<CommentUiState> = _videoId.flatMapLatest { id ->
        if (id == null) {
            flowOf(CommentUiState())
        } else {
            commentRepository.observeCommentsForVideo(id).map { comments ->
                CommentUiState(videoId = id, comments = comments)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommentUiState())

    val currentUserId: StateFlow<Long> = settings.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_USER_ID)

    fun open(videoId: Long) {
        _videoId.value = videoId
    }

    fun close() {
        _videoId.value = null
    }

    fun send(text: String) {
        val id = _videoId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            commentRepository.addComment(id, currentUserId.value, text.trim())
        }
    }

    fun observeIsCommentLiked(commentId: Long): Flow<Boolean> =
        settings.currentUserId.flatMapLatest { socialRepository.observeIsCommentLiked(it, commentId) }

    fun toggleCommentLike(commentId: Long) {
        viewModelScope.launch {
            socialRepository.toggleCommentLike(currentUserId.value, commentId)
        }
    }

    private companion object {
        const val DEFAULT_USER_ID = 2L
    }
}
