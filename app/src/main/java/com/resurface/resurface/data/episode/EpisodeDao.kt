package com.resurface.resurface.data.episode

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Acesso ao Room pros episódios. Escrita suspend; leitura observável como Flow. */
@Dao
interface EpisodeDao {

    /** Insere um episódio fechado; ignora se já existe um com o mesmo `startedAt` (idempotente). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(episode: EpisodeEntity)

    /** Observa todos os episódios, do mais recente pro mais antigo. */
    @Query("SELECT * FROM episode ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<EpisodeEntity>>
}
