package com.resurface.resurface.data.onboarding

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

/** Estado persistido do onboarding. Status de permissão NÃO fica aqui — é lido ao vivo (G3). */
data class OnboardingState(
    val consentGiven: Boolean,
    val onboardingCompleted: Boolean,
)

/** Guarda o consentimento e se o onboarding foi concluído (DataStore). */
@Singleton
class OnboardingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private object Keys {
        val CONSENT = booleanPreferencesKey("consent_given")
        val COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    /** Estado observável do onboarding. */
    val state: Flow<OnboardingState> = dataStore.data.map {
        OnboardingState(
            consentGiven = it[Keys.CONSENT] ?: false,
            onboardingCompleted = it[Keys.COMPLETED] ?: false,
        )
    }

    /** Registra o consentimento explícito. */
    suspend fun recordConsent() = withContext(io) {
        dataStore.edit { it[Keys.CONSENT] = true }
    }

    /** Marca o onboarding como concluído. */
    suspend fun setCompleted(completed: Boolean) = withContext(io) {
        dataStore.edit { it[Keys.COMPLETED] = completed }
    }

    /** DEBUG: zera consentimento + conclusão pra ver o onboarding do zero a cada launch. */
    suspend fun resetForTesting() = withContext(io) {
        dataStore.edit {
            it[Keys.CONSENT] = false
            it[Keys.COMPLETED] = false
        }
    }
}
