package com.resurface.resurface.data.config

/** Fonte de "agora" injetável (G9) — pra testar expiração sem esperar o relógio real. */
fun interface TimeProvider {
    /** Instante atual em epoch millis. */
    fun now(): Long
}
