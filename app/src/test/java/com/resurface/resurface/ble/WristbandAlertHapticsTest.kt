package com.resurface.resurface.ble

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.resurface.resurface.data.wristband.WristbandPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class WristbandAlertHapticsTest {

    @get:Rule
    val tmp = TemporaryFolder()

    // Sender de teste: captura o último comando e devolve um resultado configurável.
    private class FakeSender(private val result: SendResult = SendResult.Success) : HapticSender {
        var last: HapticCommand? = null
        override suspend fun send(command: HapticCommand): SendResult {
            last = command
            return result
        }
    }

    private fun prefs(scope: TestScope): WristbandPreferences {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("wb-${System.nanoTime()}.preferences_pb")
        }
        return WristbandPreferences(ds, UnconfinedTestDispatcher(scope.testScheduler))
    }

    /** Com intensidade gravada, pulsa Gentle com essa intensidade. */
    @Test
    fun `pulsa Gentle com a intensidade gravada`() = runTest {
        val sender = FakeSender()
        val p = prefs(this)
        p.setIntensity(200)
        val haptics = WristbandAlertHaptics(sender, p, UnconfinedTestDispatcher(testScheduler))

        haptics.pulseOnce()

        assertEquals(HapticEffect.Gentle, sender.last?.effect)
        assertEquals(200, sender.last?.intensity)
    }

    /** Sem intensidade (auto), envia Gentle sem byte de intensidade (null). */
    @Test
    fun `intensidade auto omite o byte`() = runTest {
        val sender = FakeSender()
        val haptics = WristbandAlertHaptics(sender, prefs(this), UnconfinedTestDispatcher(testScheduler))

        haptics.pulseOnce()

        assertEquals(HapticEffect.Gentle, sender.last?.effect)
        assertNull(sender.last?.intensity)
    }

    /** Sender NotConnected não lança: pulse é no-op silencioso. */
    @Test
    fun `no-op nao lanca quando nao conectado`() = runTest {
        val sender = FakeSender(SendResult.NotConnected)
        val haptics = WristbandAlertHaptics(sender, prefs(this), UnconfinedTestDispatcher(testScheduler))

        haptics.pulseOnce()

        // Tentou enviar (o "no-op" é decidido dentro do send/repo, sem exceção aqui).
        assertEquals(HapticEffect.Gentle, sender.last?.effect)
    }
}
