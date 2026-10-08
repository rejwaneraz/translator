package com.rejwane.reelslocal.ui.screens.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.relation.CommentWithAuthor
import com.rejwane.reelslocal.data.database.relation.UserWithVideoCount
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.data.repository.CommentRepository
import com.rejwane.reelslocal.data.repository.UserRepository
import com.rejwane.reelslocal.data.repository.VideoRepository
import com.rejwane.reelslocal.data.seed.DatabaseSeeder
import com.rejwane.reelslocal.utils.MediaImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Local content management (the "admin" editor). Everything mutates Room directly and reacts
 * through the same observable flows the rest of the app uses, so edits propagate everywhere.
 */
@HiltViewModel
class ContentManagerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository,
    private val commentRepository: CommentRepository,
    private val seeder: DatabaseSeeder,
    private val mediaImporter: MediaImporter
) : ViewModel() {

    val users: StateFlow<List<UserWithVideoCount>> = userRepository.observeAllWithVideoCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allUsers: StateFlow<List<User>> = userRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val videos: StateFlow<List<VideoWithOwner>> = videoRepository.observeAllWithOwner()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val comments: StateFlow<List<CommentWithAuthor>> = commentRepository.observeAllWithAuthor()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- Users ----

    fun saveUser(user: User) = viewModelScope.launch {
        if (user.id == 0L) userRepository.createUser(user) else userRepository.updateUser(user)
    }

    fun duplicateUser(userId: Long) = viewModelScope.launch {
        userRepository.duplicateUser(userId)
    }

    fun deleteUser(userId: Long) = viewModelScope.launch {
        userRepository.deleteUser(userId)
    }

    // ---- Videos ----

    fun saveVideo(video: Video) = viewModelScope.launch {
        if (video.id == 0L) videoRepository.createVideo(video) else videoRepository.updateVideo(video)
    }

    fun duplicateVideo(videoId: Long) = viewModelScope.launch {
        videoRepository.duplicateVideo(videoId)
    }

    fun deleteVideo(videoId: Long) = viewModelScope.launch {
        videoRepository.deleteVideo(videoId)
    }

    // ---- Comments ----

    fun saveComment(comment: Comment) = viewModelScope.launch {
        if (comment.id == 0L) {
            commentRepository.addComment(comment.videoId, comment.authorId, comment.text)
        } else {
            commentRepository.updateComment(comment)
        }
    }

    fun deleteComment(commentId: Long) = viewModelScope.launch {
        commentRepository.deleteComment(commentId)
    }

    fun setPinned(commentId: Long, pinned: Boolean) = viewModelScope.launch {
        commentRepository.setPinned(commentId, pinned)
    }

    fun setHidden(commentId: Long, hidden: Boolean) = viewModelScope.launch {
        commentRepository.setHidden(commentId, hidden)
    }

    // ---- Seed ----

    fun resetAndReseed() = viewModelScope.launch {
        seeder.resetAndReseed()
    }

    // ---- Media import ----

    /** Copies a SAF-picked file into app storage and reports its stored absolute path. */
    fun importMedia(uri: Uri, kind: MediaImporter.Kind, onResult: (String?) -> Unit) =
        viewModelScope.launch {
            onResult(mediaImporter.import(uri, kind))
        }
}
