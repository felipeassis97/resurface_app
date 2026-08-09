package com.resurface.resurface.data.behavior

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.resurface.resurface.domain.model.DetectedSwipe

/** Uma linha de comportamento = um vídeo detectado (superfície + se hesitou). Fonte à parte (D13/F5). */
@Entity(tableName = "behavior_event")
data class BehaviorEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val pkg: String,
    val surface: String,
    val hesitated: Boolean,
)

/** Converte um deslize detectado do domínio pra linha do Room. */
fun DetectedSwipe.toEntity(): BehaviorEventEntity = BehaviorEventEntity(
    timestamp = timestamp,
    pkg = pkg,
    surface = surface.name,
    hesitated = hesitated,
)
