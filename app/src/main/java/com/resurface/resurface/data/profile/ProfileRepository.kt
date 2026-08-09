package com.resurface.resurface.data.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.model.Profile
import com.resurface.resurface.domain.model.Tone
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Persiste o perfil (tom + hobbies). Fonte única (G3). */
@Singleton
class ProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private object Keys {
        val NAME = stringPreferencesKey("profile_name")
        val TONE = stringPreferencesKey("profile_tone")
        val HOBBIES = stringSetPreferencesKey("profile_hobbies")
        val HOBBY_FREE = stringPreferencesKey("profile_hobby_free")
    }

    /** Perfil gravado, com defaults (sem nome, tom GENTIL, sem hobbies). */
    val profile: Flow<Profile> = dataStore.data.map { prefs ->
        Profile(
            name = prefs[Keys.NAME] ?: "",
            tone = prefs[Keys.TONE]?.let { runCatching { Tone.valueOf(it) }.getOrNull() } ?: Tone.GENTIL,
            hobbies = prefs[Keys.HOBBIES] ?: emptySet(),
            hobbyFree = prefs[Keys.HOBBY_FREE],
        )
    }

    /** Grava o nome pelo qual a pessoa quer ser chamada. */
    suspend fun setName(name: String) = withContext(io) {
        dataStore.edit { it[Keys.NAME] = name.trim() }
    }

    /** Grava o tom escolhido. */
    suspend fun setTone(tone: Tone) = withContext(io) {
        dataStore.edit { it[Keys.TONE] = tone.name }
    }

    /** Grava os hobbies (múltipla escolha + campo livre opcional). */
    suspend fun setHobbies(hobbies: Set<String>, free: String?) = withContext(io) {
        dataStore.edit {
            it[Keys.HOBBIES] = hobbies
            if (free.isNullOrBlank()) it.remove(Keys.HOBBY_FREE) else it[Keys.HOBBY_FREE] = free
        }
    }
}
