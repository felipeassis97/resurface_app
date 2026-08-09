package com.resurface.resurface.data.outcome

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Acesso ao Room pros outcomes de aviso. */
@Dao
interface AlertOutcomeDao {

    /** Insere um aviso disparado e devolve o id gerado. */
    @Insert
    suspend fun insert(outcome: AlertOutcomeEntity): Long

    /** Grava a resposta de um aviso pelo id. */
    @Query("UPDATE alert_outcome SET response = :response, respondedAt = :at WHERE id = :id")
    suspend fun setResponse(id: Long, response: String, at: Long)

    /** Observa os outcomes, do mais recente pro mais antigo. */
    @Query("SELECT * FROM alert_outcome ORDER BY firedAt DESC")
    fun observeAll(): Flow<List<AlertOutcomeEntity>>

    /** Quantos avisos dispararam com firedAt em [from, to] (avisos do episódio). */
    @Query("SELECT COUNT(*) FROM alert_outcome WHERE firedAt BETWEEN :from AND :to")
    suspend fun countBetween(from: Long, to: Long): Int

    /** Quantos avisos dispararam desde [from] (teto diário). */
    @Query("SELECT COUNT(*) FROM alert_outcome WHERE firedAt >= :from")
    suspend fun countSince(from: Long): Int
}
