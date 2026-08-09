package com.resurface.resurface.data.episode

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.resurface.resurface.domain.model.ClosedEpisode

/**
 * Linha de episódio no Room. `apps` = pacotes juntados por vírgula (conjunto de 2, sem tabela
 * de junção — D-5). Nunca vaza pra UI/domínio; converte com [toDomain]/[toEntity].
 *
 * Índice único em `startedAt`: o replay a cada tick re-emite os fechados; com `IGNORE` no insert,
 * re-arquivar o mesmo episódio é no-op (arquivamento idempotente).
 */
@Entity(tableName = "episode", indices = [Index(value = ["startedAt"], unique = true)])
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long,
    val accumulatedMs: Long,
    val apps: String,
)

/** Converte a linha do Room pro modelo de domínio. */
fun EpisodeEntity.toDomain(): ClosedEpisode = ClosedEpisode(
    startedAt = startedAt,
    endedAt = endedAt,
    accumulatedMs = accumulatedMs,
    apps = apps.split(",").filter { it.isNotEmpty() }.toSet(),
)

/** Converte o episódio de domínio pra linha do Room (apps ordenados pra string estável). */
fun ClosedEpisode.toEntity(): EpisodeEntity = EpisodeEntity(
    startedAt = startedAt,
    endedAt = endedAt,
    accumulatedMs = accumulatedMs,
    apps = apps.sorted().joinToString(","),
)
