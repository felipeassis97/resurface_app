package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.Moment
import com.resurface.resurface.domain.model.Profile

/**
 * Compõe a mensagem à mão (D8): escolhe um template do pool do tom (rotação por seed → variedade,
 * H4), preenche os slots. Se não há hobby, só escolhe templates que não precisam de hobby. Puro.
 */
class TemplateComposer {

    /** Compõe a mensagem pro [profile] e [moment]; [seed] rotaciona o pool. */
    fun compose(profile: Profile, moment: Moment, seed: Int): Message {
        val pool = MessageTemplates.pools.getValue(profile.tone)
        val hobby = profile.anyHobby()
        val hasName = profile.name.isNotBlank()
        val eligible = pool.filter { (hobby != null || !it.usesHobby) && (hasName || !it.usesName) }
        val pick = eligible[Math.floorMod(seed, eligible.size)]
        return Message(
            title = fill(pick.title, moment, hobby, profile.name),
            body = fill(pick.body, moment, hobby, profile.name),
        )
    }

    /** Substitui os slots {min}/{app}/{hobby}/{name} pelo momento, hobby e nome. */
    private fun fill(text: String, moment: Moment, hobby: String?, name: String): String =
        text.replace("{min}", moment.minutes.toString())
            .replace("{app}", moment.appLabel)
            .replace("{hobby}", hobby ?: "")
            .replace("{name}", name)
}
