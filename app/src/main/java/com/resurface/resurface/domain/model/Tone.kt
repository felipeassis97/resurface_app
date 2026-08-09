package com.resurface.resurface.domain.model

/** O tom em que o aviso é escrito (F1). */
enum class Tone { DIRETO, GENTIL, BEM_HUMORADO }

/** Perfil do usuário: nome + como quer ser lembrado + hobbies pra dar textura (nunca cobrança, P5). */
data class Profile(
    val name: String = "",
    val tone: Tone = Tone.GENTIL,
    val hobbies: Set<String> = emptySet(),
    val hobbyFree: String? = null,
) {
    /** Um hobby pra dar textura, ou null se não há nenhum. */
    fun anyHobby(): String? = hobbyFree?.takeIf { it.isNotBlank() } ?: hobbies.firstOrNull()
}
