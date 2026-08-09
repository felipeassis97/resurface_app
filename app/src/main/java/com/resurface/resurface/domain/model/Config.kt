package com.resurface.resurface.domain.model

/**
 * Configuração do domínio. Alvos e janela são parâmetro (não constante mágica, D-5) para
 * o motor ser testável com pacotes fake e não prender o escopo dos 2 apps (D19).
 */
data class Config(
    /** Limite base do primeiro aviso, em minutos. Faixa 10–60 (F1). */
    val limitMinutes: Int = 20,
    /** Pacotes contados como vídeo curto (Instagram, TikTok). */
    val targetPackages: Set<String> = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
    ),
    /** Janela de retorno: sair menos que isto retoma; mais que isto fecha o episódio (D3). */
    val returnWindowMs: Long = 5 * 60 * 1000L,
    /** Janela ativa (allow-list): quando o usuário aceita avisos. Vazia = sempre ativo. */
    val schedule: Schedule = Schedule(),
) {
    /** Verdadeiro se [pkg] é um app-alvo. */
    fun isTarget(pkg: String): Boolean = pkg in targetPackages
}
