package com.rejwane.reelslocal.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            status = when (event) {
                BackupEvent.Exported -> "exported"
                BackupEvent.Restored -> "restored"
                is BackupEvent.Failed -> "failed:${event.message}"
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::import) }

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
                    text = stringResource(R.string.backup_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BackupAction(
                title = stringResource(R.string.backup_export),
                summary = stringResource(R.string.backup_export_summary),
                enabled = !busy,
                onClick = { exportLauncher.launch("reelslocal-backup.json") }
            )

            BackupAction(
                title = stringResource(R.string.backup_import),
                summary = stringResource(R.string.backup_import_summary),
                enabled = !busy,
                outlined = true,
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
            )

            Text(
                text = stringResource(R.string.backup_import_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            if (busy) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.backup_busy),
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            status?.let { raw ->
                val text = when {
                    raw == "exported" -> stringResource(R.string.backup_exported)
                    raw == "restored" -> stringResource(R.string.backup_restored)
                    raw.startsWith("failed:") ->
                        stringResource(R.string.backup_failed, raw.removePrefix("failed:"))
                    else -> raw
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (raw.startsWith("failed:")) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BackupAction(
    title: String,
    summary: String,
    enabled: Boolean,
    onClick: () -> Unit,
    outlined: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (outlined) {
            OutlinedButton(onClick = onClick, enabled = enabled) {
                Text(title, modifier = Modifier.fillMaxWidth())
            }
        } else {
            Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(title)
            }
        }
    }
}
