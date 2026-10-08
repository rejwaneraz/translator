package com.rejwane.reelslocal.di

import android.util.Log
import com.rejwane.reelslocal.data.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Runs one-off startup work (database seeding) off the main thread. */
@Singleton
class AppInitializer @Inject constructor(
    private val seeder: DatabaseSeeder
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        scope.launch {
            runCatching { seeder.seedIfEmpty() }
                .onFailure { Log.e(TAG, "Database seeding failed", it) }
        }
    }

    companion object {
        private const val TAG = "AppInitializer"
    }
}
