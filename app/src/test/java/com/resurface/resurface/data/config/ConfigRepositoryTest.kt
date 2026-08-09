package com.resurface.resurface.data.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDateTime
import java.time.ZoneId

/** Relógio mutável de teste: permite avançar o "agora" e reavaliar a expiração. */
private class MutableTime(var value: Long) : TimeProvider {
    override fun now(): Long = value
}

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val zone = ZoneId.of("America/Sao_Paulo")
    private fun at(dt: LocalDateTime) = dt.atZone(zone).toInstant().toEpochMilli()

    /** Cria um repo com DataStore temporário próprio e o relógio dado. */
    private fun repo(scope: TestScope, time: TimeProvider): ConfigRepository {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("cfg-${System.nanoTime()}.preferences_pb")
        }
        return ConfigRepository(
            dataStore = ds,
            io = UnconfinedTestDispatcher(scope.testScheduler),
            time = time,
            midnight = MidnightClock(zone),
        )
    }

    /** Sem nada gravado, o limite é o padrão 20. */
    @Test
    fun `limite padrao é 20`() = runTest {
        val r = repo(this, MutableTime(at(LocalDateTime.of(2026, 8, 9, 12, 0))))
        assertEquals(20, r.limitMinutes.first())
    }

    /** Gravar 30 dentro da faixa e ler 30. */
    @Test
    fun `grava 30 le 30`() = runTest {
        val r = repo(this, MutableTime(at(LocalDateTime.of(2026, 8, 9, 12, 0))))
        assertTrue(r.setLimit(30).isSuccess)
        assertEquals(30, r.limitMinutes.first())
    }

    /** Rejeita fora da faixa (5 < 10) e o valor guardado permanece. */
    @Test
    fun `rejeita fora da faixa`() = runTest {
        val r = repo(this, MutableTime(at(LocalDateTime.of(2026, 8, 9, 12, 0))))
        r.setLimit(30)
        assertTrue(r.setLimit(5).isFailure)
        assertEquals(30, r.limitMinutes.first())
    }

    /** Pausar por hoje fica ativo enquanto o "agora" está antes da meia-noite gravada. */
    @Test
    fun `pausar por hoje fica ativo`() = runTest {
        val r = repo(this, MutableTime(at(LocalDateTime.of(2026, 8, 9, 15, 0))))
        r.pauseForToday()
        assertTrue(r.pausedToday.first())
    }

    /** Na virada do dia, "pausar por hoje" expira (mesmo marco, relógio avançado). */
    @Test
    fun `pausar expira na virada`() = runTest {
        val clock = MutableTime(at(LocalDateTime.of(2026, 8, 9, 15, 0)))
        val r = repo(this, clock)
        r.pauseForToday()
        assertTrue(r.pausedToday.first())                       // 15h < meia-noite → ativo
        clock.value = at(LocalDateTime.of(2026, 8, 10, 0, 1))   // já passou da meia-noite
        assertFalse(r.pausedToday.first())                      // expirou
    }
}
