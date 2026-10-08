package com.rejwane.reelslocal.ui.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejwane.reelslocal.data.backup.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BackupEvent {
    data object Exported : BackupEvent
    data object Restored : BackupEvent
    data class Failed(val message: String) : BackupEvent
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _events = Channel<BackupEvent>(Channel.BUFFERED)
    val events: Flow<BackupEvent> = _events.receiveAsFlow()

    fun export(uri: Uri) = run(uri) { backupRepository.exportTo(it); BackupEvent.Exported }

    fun import(uri: Uri) = run(uri) {
        val json = backupRepository.importFrom(it)
        backupRepository.restoreFromJson(json)
        BackupEvent.Restored
    }

    private fun run(uri: Uri, block: suspend (Uri) -> BackupEvent) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            val event = runCatching { block(uri) }
                .getOrElse { BackupEvent.Failed(it.message ?: "Unknown error") }
            _busy.value = false
            _events.send(event)
        }
    }
}
