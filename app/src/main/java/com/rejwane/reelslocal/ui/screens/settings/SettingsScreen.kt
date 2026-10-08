package com.rejwane.reelslocal.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.model.AppThemeMode
import com.rejwane.reelslocal.ui.components.UserAvatar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenContentManager: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenBackup: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    var showAccountPicker by remember { mutableStateOf(false) }

    val currentUser = users.firstOrNull { it.id == state.currentUserId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        SectionHeader(stringResource(R.string.section_appearance))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.themeMode == mode,
                    onClick = { viewModel.setThemeMode(mode) },
                    label = { Text(stringResource(mode.labelRes())) }
                )
            }
        }

        SectionHeader(stringResource(R.string.section_playback))
        SwitchRow(stringResource(R.string.setting_autoplay), state.autoplay, viewModel::setAutoplay)
        SwitchRow(stringResource(R.string.setting_muted), state.muted, viewModel::setMuted)
        SwitchRow(stringResource(R.string.setting_view_counts), state.showViewCounts, viewModel::setShowViewCounts)
        SwitchRow(stringResource(R.string.setting_comments), state.showComments, viewModel::setShowComments)

        SectionHeader(stringResource(R.string.section_account))
        TwoLineRow(
            title = stringResource(R.string.current_user),
            subtitle = currentUser?.let { "${it.displayName} (@${it.username})" } ?: "—",
            onClick = { showAccountPicker = true }
        )

        SectionHeader(stringResource(R.string.section_data))
        TwoLineRow(
            title = stringResource(R.string.content_manager),
            subtitle = stringResource(R.string.content_manager_summary),
            onClick = onOpenContentManager
        )
        TwoLineRow(
            title = stringResource(R.string.backup_restore),
            subtitle = stringResource(R.string.backup_restore_summary),
            onClick = onOpenBackup
        )

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = viewModel::reset,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(stringResource(R.string.reset_settings), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showAccountPicker) {
        AccountPickerDialog(
            users = users,
            currentUserId = state.currentUserId,
            onDismiss = { showAccountPicker = false },
            onSelect = { userId ->
                viewModel.switchUser(userId)
                showAccountPicker = false
            }
        )
    }
}

@Composable
private fun AccountPickerDialog(
    users: List<User>,
    currentUserId: Long,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        title = { Text(stringResource(R.string.choose_account)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                users.filterNot { it.isSystem }.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(user.id) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(user = user, size = 36.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.displayName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "@${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (user.id == currentUserId) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TwoLineRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun AppThemeMode.labelRes(): Int = when (this) {
    AppThemeMode.SYSTEM -> R.string.theme_system
    AppThemeMode.LIGHT -> R.string.theme_light
    AppThemeMode.DARK -> R.string.theme_dark
}
