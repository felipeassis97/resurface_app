package com.resurface.resurface.domain.model

/** Decisão da AlertPolicy: disparar o aviso (com o limite cruzado) ou segurar. */
sealed interface AlertDecision {
    /** Deve avisar agora; [limitMinutes] é o limite que foi cruzado (20, 40, 80…). */
    data class Fire(val limitMinutes: Int) : AlertDecision

    /** Não avisar. */
    data object Hold : AlertDecision
}
