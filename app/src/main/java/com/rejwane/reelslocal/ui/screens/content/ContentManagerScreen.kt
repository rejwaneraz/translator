package com.rejwane.reelslocal.ui.screens.content

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.relation.CommentWithAuthor
import com.rejwane.reelslocal.data.database.relation.UserWithVideoCount
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.utils.MediaImporter

private enum class ManagerTab { USERS, VIDEOS, COMMENTS }

@Composable
fun ContentManagerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContentManagerViewModel = hiltViewModel()
) {
    var tab by remember { mutableStateOf(ManagerTab.USERS) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var editingVideo by remember { mutableStateOf<Video?>(null) }
    var editingComment by remember { mutableStateOf<Comment?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val users by viewModel.users.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
                Text(
                    text = stringResource(R.string.content_manager),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { confirmReset = true }) {
                    Text(stringResource(R.string.reset_and_reseed))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (tab) {
                    ManagerTab.USERS -> editingUser = NEW_USER
                    ManagerTab.VIDEOS -> editingVideo = NEW_VIDEO
                    ManagerTab.COMMENTS -> editingComment = NEW_COMMENT
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab.ordinal) {
                ManagerTab.entries.forEach { t ->
                    Tab(
                        selected = tab == t,
                        onClick = { tab = t },
                        text = { Text(stringResource(t.labelRes())) }
                    )
                }
            }

            when (tab) {
                ManagerTab.USERS -> UsersList(
                    users = users,
                    onEdit = { editingUser = it },
                    onDuplicate = viewModel::duplicateUser,
                    onDelete = viewModel::deleteUser
                )
                ManagerTab.VIDEOS -> VideosList(
                    videos = videos,
                    onEdit = { editingVideo = it },
                    onDuplicate = viewModel::duplicateVideo,
                    onDelete = viewModel::deleteVideo
                )
                ManagerTab.COMMENTS -> CommentsList(
                    comments = comments,
                    onEdit = { editingComment = it },
                    onDelete = viewModel::deleteComment,
                    onTogglePin = { id, pinned -> viewModel.setPinned(id, pinned) },
                    onToggleHide = { id, hidden -> viewModel.setHidden(id, hidden) }
                )
            }
        }
    }

    editingUser?.let { draft ->
        UserEditorDialog(
            initial = draft,
            onDismiss = { editingUser = null },
            onSave = { viewModel.saveUser(it); editingUser = null }
        )
    }
    editingVideo?.let { draft ->
        VideoEditorDialog(
            initial = draft,
            users = allUsers,
            onDismiss = { editingVideo = null },
            onSave = { viewModel.saveVideo(it); editingVideo = null },
            onImportMedia = { uri, kind, cb -> viewModel.importMedia(uri, kind, cb) }
        )
    }
    editingComment?.let { draft ->
        CommentEditorDialog(
            initial = draft,
            users = allUsers,
            videos = videos.map { it.video },
            onDismiss = { editingComment = null },
            onSave = { viewModel.saveComment(it); editingComment = null }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_and_reseed)) },
            text = { Text(stringResource(R.string.reset_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetAndReseed(); confirmReset = false }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private val NEW_USER = User(username = "", displayName = "")
private val NEW_VIDEO = Video(ownerId = 2L, videoPath = "asset:videos/video_001.mp4")
private val NEW_COMMENT = Comment(videoId = 1L, authorId = 2L, text = "")

private fun ManagerTab.labelRes(): Int = when (this) {
    ManagerTab.USERS -> R.string.cm_tab_users
    ManagerTab.VIDEOS -> R.string.cm_tab_videos
    ManagerTab.COMMENTS -> R.string.cm_tab_comments
}

// ---------------- Lists ----------------

@Composable
private fun UsersList(
    users: List<UserWithVideoCount>,
    onEdit: (User) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        androidx.compose.foundation.lazy.items(users, key = { it.user.id }) { item ->
            val user = item.user
            ManagerRow(
                title = user.displayName + if (user.isSystem) " (system)" else "",
                subtitle = "@${user.username} • ${item.videoCount} videos • ${user.followersCount} followers",
                onEdit = { onEdit(user) },
                onDuplicate = if (user.isSystem) null else ({ onDuplicate(user.id) }),
                onDelete = if (user.isSystem) null else ({ onDelete(user.id) })
            )
        }
    }
}

@Composable
private fun VideosList(
    videos: List<VideoWithOwner>,
    onEdit: (Video) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        androidx.compose.foundation.lazy.items(videos, key = { it.video.id }) { item ->
            val video = item.video
            ManagerRow(
                title = video.title.ifBlank { video.caption.ifBlank { "Untitled" } },
                subtitle = "${item.owner?.displayName ?: "?"} • ${video.views} views • ${video.likesCount} likes",
                onEdit = { onEdit(video) },
                onDuplicate = { onDuplicate(video.id) },
                onDelete = { onDelete(video.id) }
            )
        }
    }
}

@Composable
private fun CommentsList(
    comments: List<CommentWithAuthor>,
    onEdit: (Comment) -> Unit,
    onDelete: (Long) -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onToggleHide: (Long, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        androidx.compose.foundation.lazy.items(comments, key = { it.comment.id }) { item ->
            val comment = item.comment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.author?.displayName ?: "?"} • video #${comment.videoId}" +
                            if (comment.isHidden) " • hidden" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onTogglePin(comment.id, !comment.isPinned) }) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.pinned),
                        tint = if (comment.isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onToggleHide(comment.id, !comment.isHidden) }) {
                    Icon(Icons.Filled.VisibilityOff, contentDescription = null)
                }
                IconButton(onClick = { onEdit(comment) }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                }
                IconButton(onClick = { onDelete(comment.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagerRow(
    title: String,
    subtitle: String,
    onEdit: () -> Unit,
    onDuplicate: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onDuplicate != null) {
            IconButton(onClick = onDuplicate) {
                Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.action_duplicate))
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ---------------- Editors ----------------

@Composable
private fun EditorShell(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) { content() }
        },
        confirmButton = { TextButton(onClick = onSave) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun UserEditorDialog(initial: User, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var displayName by remember { mutableStateOf(initial.displayName) }
    var username by remember { mutableStateOf(initial.username) }
    var bio by remember { mutableStateOf(initial.bio) }
    var avatarPath by remember { mutableStateOf(initial.avatarPath ?: "") }
    var followers by remember { mutableStateOf(initial.followersCount.toString()) }
    var following by remember { mutableStateOf(initial.followingCount.toString()) }
    var likes by remember { mutableStateOf(initial.likesCount.toString()) }
    var verified by remember { mutableStateOf(initial.isVerified) }

    EditorShell(
        title = stringResource(if (initial.id == 0L) R.string.cm_new_user else R.string.cm_edit_user),
        onDismiss = onDismiss,
        onSave = {
            onSave(
                initial.copy(
                    displayName = displayName.trim(),
                    username = username.trim(),
                    bio = bio.trim(),
                    avatarPath = avatarPath.trim().ifBlank { null },
                    followersCount = followers.toLongOr(initial.followersCount),
                    followingCount = following.toLongOr(initial.followingCount),
                    likesCount = likes.toLongOr(initial.likesCount),
                    isVerified = verified
                )
            )
        }
    ) {
        LabeledField("Display name", displayName) { displayName = it }
        LabeledField("Username", username) { username = it }
        LabeledField("Bio", bio) { bio = it }
        LabeledField("Avatar path", avatarPath) { avatarPath = it }
        LabeledField("Followers", followers, numeric = true) { followers = it }
        LabeledField("Following", following, numeric = true) { following = it }
        LabeledField("Likes", likes, numeric = true) { likes = it }
        SwitchField("Verified", verified) { verified = it }
    }
}

@Composable
private fun VideoEditorDialog(
    initial: Video,
    users: List<User>,
    onDismiss: () -> Unit,
    onSave: (Video) -> Unit,
    onImportMedia: (Uri, MediaImporter.Kind, (String?) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var caption by remember { mutableStateOf(initial.caption) }
    var hashtags by remember { mutableStateOf(initial.hashtags) }
    var videoPath by remember { mutableStateOf(initial.videoPath) }
    var thumbnailPath by remember { mutableStateOf(initial.thumbnailPath ?: "") }
    var musicTitle by remember { mutableStateOf(initial.musicTitle) }
    var ownerId by remember { mutableStateOf(initial.ownerId) }
    var views by remember { mutableStateOf(initial.views.toString()) }
    var likes by remember { mutableStateOf(initial.likesCount.toString()) }
    var comments by remember { mutableStateOf(initial.commentsCount.toString()) }
    var shares by remember { mutableStateOf(initial.sharesCount.toString()) }
    var duration by remember { mutableStateOf(initial.durationMs.toString()) }
    var sortOrder by remember { mutableStateOf(initial.sortOrder.toString()) }
    var published by remember { mutableStateOf(initial.isPublished) }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onImportMedia(it, MediaImporter.Kind.VIDEO) { path -> path?.let { videoPath = it } } }
    }
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onImportMedia(it, MediaImporter.Kind.IMAGE) { path -> path?.let { thumbnailPath = it } } }
    }

    EditorShell(
        title = stringResource(if (initial.id == 0L) R.string.cm_new_video else R.string.cm_edit_video),
        onDismiss = onDismiss,
        onSave = {
            onSave(
                initial.copy(
                    title = title.trim(),
                    caption = caption.trim(),
                    hashtags = hashtags.trim(),
                    videoPath = videoPath.trim(),
                    thumbnailPath = thumbnailPath.trim().ifBlank { null },
                    musicTitle = musicTitle.trim(),
                    ownerId = ownerId,
                    views = views.toLongOr(initial.views),
                    likesCount = likes.toLongOr(initial.likesCount),
                    commentsCount = comments.toLongOr(initial.commentsCount),
                    sharesCount = shares.toLongOr(initial.sharesCount),
                    durationMs = duration.toLongOr(initial.durationMs),
                    sortOrder = sortOrder.toIntOrNull() ?: initial.sortOrder,
                    isPublished = published
                )
            )
        }
    ) {
        PickerField(
            label = "Owner",
            options = users.map { it.id to it.displayName },
            selectedId = ownerId,
            onSelect = { ownerId = it }
        )
        LabeledField("Title", title) { title = it }
        LabeledField("Caption", caption) { caption = it }
        LabeledField("Hashtags (comma separated)", hashtags) { hashtags = it }
        PathFieldWithPick("Video path", videoPath, { videoPath = it }) {
            videoPicker.launch(arrayOf("video/*"))
        }
        PathFieldWithPick("Thumbnail path", thumbnailPath, { thumbnailPath = it }) {
            imagePicker.launch(arrayOf("image/*"))
        }
        LabeledField("Music title", musicTitle) { musicTitle = it }
        LabeledField("Views", views, numeric = true) { views = it }
        LabeledField("Likes", likes, numeric = true) { likes = it }
        LabeledField("Comments", comments, numeric = true) { comments = it }
        LabeledField("Shares", shares, numeric = true) { shares = it }
        LabeledField("Duration (ms)", duration, numeric = true) { duration = it }
        LabeledField("Sort order", sortOrder, numeric = true) { sortOrder = it }
        SwitchField("Published", published) { published = it }
    }
}

@Composable
private fun CommentEditorDialog(
    initial: Comment,
    users: List<User>,
    videos: List<Video>,
    onDismiss: () -> Unit,
    onSave: (Comment) -> Unit
) {
    var text by remember { mutableStateOf(initial.text) }
    var authorId by remember { mutableStateOf(initial.authorId) }
    var videoId by remember { mutableStateOf(initial.videoId) }
    var likeCount by remember { mutableStateOf(initial.likeCount.toString()) }
    var pinned by remember { mutableStateOf(initial.isPinned) }
    var hidden by remember { mutableStateOf(initial.isHidden) }

    EditorShell(
        title = stringResource(if (initial.id == 0L) R.string.cm_new_comment else R.string.cm_edit_comment),
        onDismiss = onDismiss,
        onSave = {
            onSave(
                initial.copy(
                    text = text.trim(),
                    authorId = authorId,
                    videoId = videoId,
                    likeCount = likeCount.toLongOr(initial.likeCount),
                    isPinned = pinned,
                    isHidden = hidden
                )
            )
        }
    ) {
        PickerField(
            label = "Author",
            options = users.map { it.id to it.displayName },
            selectedId = authorId,
            onSelect = { authorId = it }
        )
        PickerField(
            label = "Video",
            options = videos.map { it.id to (it.title.ifBlank { it.caption }.ifBlank { "Video #${it.id}" }) },
            selectedId = videoId,
            onSelect = { videoId = it }
        )
        LabeledField("Comment text", text) { text = it }
        LabeledField("Like count", likeCount, numeric = true) { likeCount = it }
        SwitchField("Pinned", pinned) { pinned = it }
        SwitchField("Hidden", hidden) { hidden = it }
    }
}

// ---------------- Field helpers ----------------

@Composable
private fun LabeledField(
    label: String,
    value: String,
    numeric: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(if (numeric) input.filter { it.isDigit() } else input) },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PathFieldWithPick(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            LabeledField(label, value, onValueChange = onValueChange)
        }
        TextButton(onClick = onPick) {
            Text(stringResource(R.string.action_pick))
        }
    }
}

@Composable
private fun SwitchField(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PickerField(
    label: String,
    options: List<Pair<Long, String>>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: "—"
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(id); expanded = false }
                )
            }
        }
    }
}

private fun String.toLongOr(default: Long): Long = toLongOrNull() ?: default
