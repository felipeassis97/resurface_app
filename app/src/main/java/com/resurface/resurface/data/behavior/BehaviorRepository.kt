package com.resurface.resurface.data.behavior

import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.model.DetectedSwipe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Guarda e conta os eventos de comportamento (vídeos, hesitação). Fonte à parte do tempo (D13/F5). */
@Singleton
class BehaviorRepository @Inject constructor(
    private val dao: BehaviorEventDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    /** Eventos observáveis, do mais recente pro mais antigo. */
    val events: Flow<List<BehaviorEventEntity>> = dao.observeAll()

    /** Registra um deslize detectado. */
    suspend fun record(swipe: DetectedSwipe) = withContext(io) {
        dao.insert(swipe.toEntity())
    }

    /** Nº de vídeos na janela [from, to]. */
    suspend fun countVideos(from: Long, to: Long): Int = withContext(io) { dao.countBetween(from, to) }

    /** Nº de hesitações na janela [from, to]. */
    suspend fun countHesitations(from: Long, to: Long): Int =
        withContext(io) { dao.countHesitatedBetween(from, to) }
}
