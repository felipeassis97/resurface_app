package com.resurface.resurface.data.alarm

/** Agenda o disparo do aviso via alarme exato (G5). O impl atravessa Doze (D22, GAPS G1). */
interface AlarmScheduler {

    /** Agenda o disparo daqui a [delayMs] (0 = assim que possível). */
    fun scheduleInMs(delayMs: Long)

    /** Cancela o alarme pendente. */
    fun cancel()
}
