package com.resurface.resurface.data.episode

import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.model.ClosedEpisode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arquivo permanente dos episódios fechados (D24). Expõe o histórico como Flow imutável e
 * arquiva fora da main. É a porta da camada de dados pro histórico — esconde o Room/DAO (G2).
 */
@Singleton
class EpisodeRepository @Inject constructor(
    private val dao: EpisodeDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    /** Histórico observável, do mais recente pro mais antigo, em modelo de domínio. */
    val history: Flow<List<ClosedEpisode>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    /** Arquiva um episódio fechado. */
    suspend fun archive(episode: ClosedEpisode) = withContext(io) {
        dao.insert(episode.toEntity())
    }
}
