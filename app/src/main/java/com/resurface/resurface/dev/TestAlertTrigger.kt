package com.resurface.resurface.dev

import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.domain.MessageGenerator
import com.resurface.resurface.domain.MessageGuard
import com.resurface.resurface.domain.TemplateComposer
import com.resurface.resurface.domain.model.Moment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ferramenta de dev (isolada, só em debug): dispara um aviso na hora no tom atual pelo MESMO
 * caminho de composição do aviso real (gera → guard P2/P5 → fallback à mão). Não grava nada:
 * usa um id sentinela, então não toca em outcome/episódio nem exige migração (design D-2/D-3).
 */
@Singleton
class TestAlertTrigger @Inject constructor(
    private val config: ConfigRepository,
    private val profileRepo: ProfileRepository,
    private val generator: MessageGenerator,
    private val notifier: Notifier,
    private val time: TimeProvider,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    // Peças puras de composição, instanciadas inline (iguais ao AlertEvaluator; não têm @Inject).
    private val guard = MessageGuard()
    private val templates = TemplateComposer()

    /** Compõe e posta o aviso de teste; devolve a fonte usada (gerada ou template) pra inspeção. */
    suspend fun fire(): MessageSourceUsed = withContext(io) {
        val profile = profileRepo.profile.first()
        val limit = config.limitMinutes.first()
        val moment = Moment(limit, AppLabels.of(PKG), hourOf(time.now()))
        val generated = generator.generate(profile, moment)?.takeIf { guard.isSafe(it) }
        val message = generated ?: templates.compose(profile, moment, seed = 0)
        notifier.ensureChannels()
        notifier.postAlert(message.title, message.body, TEST_ALERT_ID)
        if (generated != null) MessageSourceUsed.GENERATED else MessageSourceUsed.TEMPLATE
    }

    /** Hora do dia (0–23) do instante, no fuso do dispositivo. */
    private fun hourOf(now: Long): Int = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).hour

    /** Fonte do texto do aviso de teste (só pra teste/inspeção). */
    enum class MessageSourceUsed { GENERATED, TEMPLATE }

    private companion object {
        // Instagram como app representativo no aviso de teste.
        const val PKG = "com.instagram.android"
        // Id sentinela: não casa com nenhuma linha de outcome → resposta F7 vira UPDATE de 0 linhas (D-2).
        const val TEST_ALERT_ID = -1L
    }
}
