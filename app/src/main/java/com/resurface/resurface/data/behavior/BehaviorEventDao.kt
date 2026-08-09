package com.resurface.resurface.data.behavior

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Acesso ao Room pros eventos de comportamento. */
@Dao
interface BehaviorEventDao {

    /** Insere um deslize detectado. */
    @Insert
    suspend fun insert(event: BehaviorEventEntity)

    /** Observa todos, do mais recente pro mais antigo. */
    @Query("SELECT * FROM behavior_event ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BehaviorEventEntity>>

    /** Nº de vídeos numa janela (base de contagem no dashboard). */
    @Query("SELECT COUNT(*) FROM behavior_event WHERE timestamp BETWEEN :from AND :to")
    suspend fun countBetween(from: Long, to: Long): Int

    /** Nº de hesitações numa janela. */
    @Query("SELECT COUNT(*) FROM behavior_event WHERE hesitated = 1 AND timestamp BETWEEN :from AND :to")
    suspend fun countHesitatedBetween(from: Long, to: Long): Int
}
