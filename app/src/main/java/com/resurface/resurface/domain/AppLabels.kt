package com.resurface.resurface.domain

/** Rótulo humano de um pacote-alvo (D25). Puro, no domínio pra service e ui reusarem sem duplicar. */
object AppLabels {
    /** Nome amigável do pacote; "vídeo curto" pra desconhecido/nulo. */
    fun of(pkg: String?): String = when (pkg) {
        "com.instagram.android" -> "Instagram"
        "com.zhiliaoapp.musically" -> "TikTok"
        else -> "vídeo curto"
    }
}
