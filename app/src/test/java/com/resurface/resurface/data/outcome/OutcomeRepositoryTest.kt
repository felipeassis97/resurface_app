package com.resurface.resurface.data.outcome

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Fake in-memory do DAO (D-2). */
private class FakeOutcomeDao : AlertOutcomeDao {
    val items = MutableStateFlow<List<AlertOutcomeEntity>>(emptyList())

    override suspend fun insert(outcome: AlertOutcomeEntity): Long {
        val id = items.value.size + 1L
        items.value = items.value + outcome.copy(id = id)
        return id
    }

    override suspend fun setResponse(id: Long, response: String, at: Long) {
        items.value = items.value.map {
            if (it.id == id) it.copy(response = response, respondedAt = at) else it
        }
    }

    override fun observeAll(): Flow<List<AlertOutcomeEntity>> = items

    override suspend fun countBetween(from: Long, to: Long): Int =
        items.value.count { it.firedAt in from..to }

    override suspend fun countSince(from: Long): Int =
        items.value.count { it.firedAt >= from }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OutcomeRepositoryTest {

    /** Registrar um aviso cria uma linha sem resposta. */
    @Test
    fun `registrar aviso cria linha sem resposta`() = runTest {
        val dao = FakeOutcomeDao()
        val repo = OutcomeRepository(dao, UnconfinedTestDispatcher(testScheduler))
        val id = repo.recordFired(firedAt = 1_000, appLabel = "Instagram")
        val row = dao.items.first().first { it.id == id }
        assertEquals("Instagram", row.appLabel)
        assertNull(row.response)
    }

    /** Tocar um botão grava a resposta no aviso correspondente. */
    @Test
    fun `tocar botao grava a resposta`() = runTest {
        val dao = FakeOutcomeDao()
        val repo = OutcomeRepository(dao, UnconfinedTestDispatcher(testScheduler))
        val id = repo.recordFired(1_000, "TikTok")
        repo.recordResponse(id, AlertResponse.ERA_HORA, at = 2_000)
        val row = dao.items.first().first { it.id == id }
        assertEquals(AlertResponse.ERA_HORA.stored, row.response)
        assertEquals(2_000L, row.respondedAt)
    }

    /** Contagem no episódio e no dia (base do dobro e do teto). */
    @Test
    fun `conta avisos no episodio e no dia`() = runTest {
        val dao = FakeOutcomeDao()
        val repo = OutcomeRepository(dao, UnconfinedTestDispatcher(testScheduler))
        repo.recordFired(1_000, "Instagram")
        repo.recordFired(2_000, "Instagram")
        repo.recordFired(9_000, "TikTok")
        assertEquals(2, repo.countInEpisode(episodeStartedAt = 500, now = 2_500))
        assertEquals(3, repo.countSince(startOfDay = 0))
    }
}
