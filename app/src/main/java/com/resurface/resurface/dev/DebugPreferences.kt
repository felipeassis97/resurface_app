package com.resurface.resurface.dev

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.resurface.resurface.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Flags de debug (isolado em `dev/`). Substitui o reset de onboarding hardcoded: o gate lê
 * [alwaysShowOnboarding] em vez de resetar sempre. Default false (release ignora via BuildConfig).
 */
@Singleton
class DebugPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private object Keys {
        val ALWAYS_ONBOARDING = booleanPreferencesKey("debug_always_show_onboarding")
    }

    val alwaysShowOnboarding: Flow<Boolean> = dataStore.data.map { it[Keys.ALWAYS_ONBOARDING] ?: false }

    suspend fun setAlwaysShowOnboarding(value: Boolean) = withContext(io) {
        dataStore.edit { it[Keys.ALWAYS_ONBOARDING] = value }
    }
}
