package com.resurface.resurface.data.outcome

import com.resurface.resurface.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Registra os avisos e suas respostas (F7). Porta da camada de dados pros outcomes. */
@Singleton
class OutcomeRepository @Inject constructor(
    private val dao: AlertOutcomeDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    /** Todos os outcomes, do mais recente pro mais antigo. */
    val outcomes: Flow<List<AlertOutcomeEntity>> = dao.observeAll()

    /** Registra que um aviso disparou (com tom + fonte, H4); devolve o id pra ligar aos botões. */
    suspend fun recordFired(firedAt: Long, appLabel: String, tone: String? = null, source: String? = null): Long = withContext(io) {
        dao.insert(AlertOutcomeEntity(firedAt = firedAt, appLabel = appLabel, tone = tone, source = source))
    }

    /** Grava a resposta do usuário a um aviso. */
    suspend fun recordResponse(id: Long, response: AlertResponse, at: Long) = withContext(io) {
        dao.setResponse(id, response.stored, at)
    }

    /** Quantos avisos dispararam dentro do episódio [episodeStartedAt, now] (pro dobro, D18). */
    suspend fun countInEpisode(episodeStartedAt: Long, now: Long): Int = withContext(io) {
        dao.countBetween(episodeStartedAt, now)
    }

    /** Quantos avisos dispararam hoje desde [startOfDay] (pro teto diário, D5). */
    suspend fun countSince(startOfDay: Long): Int = withContext(io) {
        dao.countSince(startOfDay)
    }
}
