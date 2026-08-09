package com.resurface.resurface.data.episode

import app.cash.turbine.test
import com.resurface.resurface.domain.model.ClosedEpisode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val IG = "com.instagram.android"
private const val TT = "com.zhiliaoapp.musically"

/** Fake in-memory do DAO (D-2): testa a lógica do repositório sem Room real. */
private class FakeEpisodeDao : EpisodeDao {
    private val items = MutableStateFlow<List<EpisodeEntity>>(emptyList())

    override suspend fun insert(episode: EpisodeEntity) {
        // Mimetiza o índice único: ignora se já existe um com o mesmo startedAt.
        if (items.value.any { it.startedAt == episode.startedAt }) return
        items.value = items.value + episode.copy(id = items.value.size + 1L)
    }

    override fun observeAll(): Flow<List<EpisodeEntity>> =
        items.map { list -> list.sortedByDescending { it.startedAt } }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeRepositoryTest {

    /** Arquivar vira uma linha no histórico, com os campos mapeados ida-e-volta. */
    @Test
    fun `arquivar vira linha e mapeia`() = runTest {
        val repo = EpisodeRepository(FakeEpisodeDao(), UnconfinedTestDispatcher(testScheduler))
        repo.history.test {
            assertEquals(emptyList<ClosedEpisode>(), awaitItem())
            repo.archive(ClosedEpisode(1_000, 2_000, 60_000, setOf(IG, TT)))
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(setOf(IG, TT), list[0].apps)
            assertEquals(60_000L, list[0].accumulatedMs)
            assertEquals(1_000L, list[0].startedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** O histórico vem do mais recente pro mais antigo e re-emite ao arquivar. */
    @Test
    fun `historico é mais recente primeiro e re-emite`() = runTest {
        val repo = EpisodeRepository(FakeEpisodeDao(), UnconfinedTestDispatcher(testScheduler))
        repo.history.test {
            awaitItem()  // vazio inicial
            repo.archive(ClosedEpisode(1_000, 2_000, 10_000, setOf(IG)))
            awaitItem()
            repo.archive(ClosedEpisode(5_000, 6_000, 20_000, setOf(TT)))
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals(5_000L, list[0].startedAt)   // mais recente no topo
            assertEquals(1_000L, list[1].startedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Arquivar o mesmo episódio duas vezes mantém uma linha (idempotente, replay). */
    @Test
    fun `arquivar duas vezes mantem uma linha`() = runTest {
        val repo = EpisodeRepository(FakeEpisodeDao(), UnconfinedTestDispatcher(testScheduler))
        val ep = ClosedEpisode(1_000, 2_000, 60_000, setOf(IG))
        repo.history.test {
            awaitItem()               // vazio
            repo.archive(ep)
            assertEquals(1, awaitItem().size)
            repo.archive(ep)          // re-arquiva o mesmo
            expectNoEvents()          // nenhuma nova emissão → sem duplicata
            cancelAndIgnoreRemainingEvents()
        }
    }
}
