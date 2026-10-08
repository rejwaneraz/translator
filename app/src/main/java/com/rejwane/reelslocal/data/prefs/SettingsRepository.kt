package com.rejwane.reelslocal.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.rejwane.reelslocal.data.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface SettingsRepository {
    val currentUserId: Flow<Long>
    val themeMode: Flow<AppThemeMode>
    val autoplay: Flow<Boolean>
    val muted: Flow<Boolean>
    val defaultTab: Flow<String>
    val showViewCounts: Flow<Boolean>
    val showComments: Flow<Boolean>

    suspend fun setCurrentUserId(userId: Long)
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setAutoplay(enabled: Boolean)
    suspend fun setMuted(muted: Boolean)
    suspend fun setDefaultTab(route: String)
    suspend fun setShowViewCounts(show: Boolean)
    suspend fun setShowComments(show: Boolean)
    suspend fun resetSettings()
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val currentUserId: Flow<Long> = dataStore.data.map { it[KEY_CURRENT_USER_ID] ?: DEFAULT_USER_ID }
    override val themeMode: Flow<AppThemeMode> = dataStore.data.map { prefs ->
        runCatching { AppThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: AppThemeMode.SYSTEM.name) }
            .getOrDefault(AppThemeMode.SYSTEM)
    }
    override val autoplay: Flow<Boolean> = dataStore.data.map { it[KEY_AUTOPLAY] ?: true }
    override val muted: Flow<Boolean> = dataStore.data.map { it[KEY_MUTED] ?: true }
    override val defaultTab: Flow<String> = dataStore.data.map { it[KEY_DEFAULT_TAB] ?: "home" }
    override val showViewCounts: Flow<Boolean> = dataStore.data.map { it[KEY_SHOW_VIEW_COUNTS] ?: true }
    override val showComments: Flow<Boolean> = dataStore.data.map { it[KEY_SHOW_COMMENTS] ?: true }

    override suspend fun setCurrentUserId(userId: Long) = edit(KEY_CURRENT_USER_ID, userId)
    override suspend fun setThemeMode(mode: AppThemeMode) = edit(KEY_THEME_MODE, mode.name)
    override suspend fun setAutoplay(enabled: Boolean) = edit(KEY_AUTOPLAY, enabled)
    override suspend fun setMuted(muted: Boolean) = edit(KEY_MUTED, muted)
    override suspend fun setDefaultTab(route: String) = edit(KEY_DEFAULT_TAB, route)
    override suspend fun setShowViewCounts(show: Boolean) = edit(KEY_SHOW_VIEW_COUNTS, show)
    override suspend fun setShowComments(show: Boolean) = edit(KEY_SHOW_COMMENTS, show)

    override suspend fun resetSettings() {
        dataStore.edit { it.clear() }
    }

    private suspend fun <T> edit(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    companion object {
        /** Rejwane — the seeded default current user. */
        const val DEFAULT_USER_ID = 2L

        val KEY_CURRENT_USER_ID = longPreferencesKey("current_user_id")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_AUTOPLAY = booleanPreferencesKey("autoplay")
        val KEY_MUTED = booleanPreferencesKey("muted")
        val KEY_DEFAULT_TAB = stringPreferencesKey("default_tab")
        val KEY_SHOW_VIEW_COUNTS = booleanPreferencesKey("show_view_counts")
        val KEY_SHOW_COMMENTS = booleanPreferencesKey("show_comments")
    }
}

object DataStoreProvider {
    fun create(context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ) {
            context.preferencesDataStoreFile("settings")
        }
}
