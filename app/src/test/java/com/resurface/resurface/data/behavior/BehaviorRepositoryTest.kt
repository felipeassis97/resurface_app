package com.resurface.resurface.data.behavior

import com.resurface.resurface.domain.model.DetectedSwipe
import com.resurface.resurface.domain.model.Surface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val IG = "com.instagram.android"

/** Fake in-memory do DAO (D-2). */
private class FakeBehaviorDao : BehaviorEventDao {
    val items = MutableStateFlow<List<BehaviorEventEntity>>(emptyList())
    override suspend fun insert(event: BehaviorEventEntity) {
        items.value = items.value + event.copy(id = items.value.size + 1L)
    }
    override fun observeAll(): Flow<List<BehaviorEventEntity>> = items
    override suspend fun countBetween(from: Long, to: Long): Int =
        items.value.count { it.timestamp in from..to }
    override suspend fun countHesitatedBetween(from: Long, to: Long): Int =
        items.value.count { it.hesitated && it.timestamp in from..to }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BehaviorRepositoryTest {

    /** Registrar um deslize vira uma linha com a superfície e a hesitação. */
    @Test
    fun `registrar deslize vira linha`() = runTest {
        val dao = FakeBehaviorDao()
        val repo = BehaviorRepository(dao, UnconfinedTestDispatcher(testScheduler))
        repo.record(DetectedSwipe(1_000, IG, Surface.REELS, hesitated = true))
        val row = dao.items.value.single()
        assertEquals("REELS", row.surface)
        assertEquals(true, row.hesitated)
    }

    /** Conta vídeos e hesitações numa janela. */
    @Test
    fun `conta videos e hesitacoes`() = runTest {
        val dao = FakeBehaviorDao()
        val repo = BehaviorRepository(dao, UnconfinedTestDispatcher(testScheduler))
        repo.record(DetectedSwipe(100, IG, Surface.REELS, hesitated = false))
        repo.record(DetectedSwipe(200, IG, Surface.REELS, hesitated = true))
        repo.record(DetectedSwipe(900, IG, Surface.REELS, hesitated = true))
        assertEquals(2, repo.countVideos(0, 300))
        assertEquals(1, repo.countHesitations(0, 300))
    }
}
