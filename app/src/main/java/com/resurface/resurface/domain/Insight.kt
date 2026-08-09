package com.resurface.resurface.domain

/** Tipo do fato do tip da home. */
enum class InsightType { PEAK_HOUR, PEAK_DAY, TREND, CROSS_APP, VIDEOS, WELCOME }

/**
 * Um fato pessoal derivado das estatísticas medidas. [fact] é a string canônica curta usada como
 * chave de cache e como o ÚNICO conteúdo enviado pro cloud (nunca os dados crus). [value]/[label]
 * são parâmetros pra frase local.
 */
data class Insight(
    val type: InsightType,
    val fact: String,
    val value: Int = 0,
    val label: String = "",
)
