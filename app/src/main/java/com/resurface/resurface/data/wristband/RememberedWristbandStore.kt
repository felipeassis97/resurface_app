package com.resurface.resurface.data.wristband

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The wristband the user last connected to, so the next session reconnects instead of
 * re-scanning.
 *
 * Only the address is stored. The advertised name is not reliably present (README §10)
 * and is not unique, so it cannot serve as identity.
 */
@Singleton
class RememberedWristbandStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val ADDRESS = stringPreferencesKey("wristband_address")
    }

    val address: Flow<String?> = dataStore.data.map { it[Keys.ADDRESS] }

    suspend fun current(): String? = address.first()

    /** Replaces any previously remembered wristband. */
    suspend fun remember(address: String) {
        dataStore.edit { it[Keys.ADDRESS] = address }
    }

    suspend fun forget() {
        dataStore.edit { it.remove(Keys.ADDRESS) }
    }
}
