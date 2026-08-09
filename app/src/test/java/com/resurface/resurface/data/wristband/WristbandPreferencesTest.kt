package com.resurface.resurface.data.wristband

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class WristbandPreferencesTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun prefs(scope: TestScope): WristbandPreferences {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("wb-${System.nanoTime()}.preferences_pb")
        }
        return WristbandPreferences(ds, UnconfinedTestDispatcher(scope.testScheduler))
    }

    /** Sem nada gravado, a intensidade é null (auto). */
    @Test
    fun `padrao null`() = runTest {
        assertNull(prefs(this).intensity.first())
    }

    /** Grava e lê a intensidade. */
    @Test
    fun `grava e le`() = runTest {
        val p = prefs(this)
        p.setIntensity(180)
        assertEquals(180, p.intensity.first())
    }

    /** setIntensity(null) limpa (volta ao auto). */
    @Test
    fun `null limpa`() = runTest {
        val p = prefs(this)
        p.setIntensity(180)
        p.setIntensity(null)
        assertNull(p.intensity.first())
    }

    /** Valores fora de 0–255 são clampados. */
    @Test
    fun `clampa fora da faixa`() = runTest {
        val p = prefs(this)
        p.setIntensity(4096)
        assertEquals(255, p.intensity.first())
    }
}
