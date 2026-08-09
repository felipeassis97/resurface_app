package com.resurface.resurface.dev

import android.app.Notification
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.MidnightClock
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.domain.MessageGenerator
import com.resurface.resurface.domain.MessageGuard
import com.resurface.resurface.domain.TemplateComposer
import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.Moment
import com.resurface.resurface.domain.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TestAlertTriggerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    // Notifier de teste: captura o último postAlert (title, body, id) e conta ensureChannels.
    private class FakeNotifier : Notifier {
        var posted: Triple<String, String, Long>? = null
        override fun ensureChannels() {}
        override fun ongoing(text: String): Notification = throw UnsupportedOperationException()
        override fun postAlert(title: String, body: String, alertId: Long) {
            posted = Triple(title, body, alertId)
        }
        override fun cancelAlert() {}
    }

    // Gerador de teste: devolve um valor fixo (mensagem ou null).
    private class FakeGenerator(private val out: Message?) : MessageGenerator {
        override suspend fun generate(profile: Profile, moment: Moment): Message? = out
    }

    private fun config(scope: TestScope): ConfigRepository {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("cfg-${System.nanoTime()}.preferences_pb")
        }
        return ConfigRepository(
            dataStore = ds,
            io = UnconfinedTestDispatcher(scope.testScheduler),
            time = TimeProvider { 0L },
            midnight = MidnightClock(ZoneId.of("America/Sao_Paulo")),
        )
    }

    private fun profile(scope: TestScope): ProfileRepository {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("prof-${System.nanoTime()}.preferences_pb")
        }
        return ProfileRepository(ds, UnconfinedTestDispatcher(scope.testScheduler))
    }

    private fun trigger(scope: TestScope, generator: MessageGenerator, notifier: Notifier) =
        TestAlertTrigger(
            config = config(scope),
            profileRepo = profile(scope),
            generator = generator,
            notifier = notifier,
            time = TimeProvider { 0L },
            io = UnconfinedTestDispatcher(scope.testScheduler),
        )

    /** Gerador devolve mensagem segura → posta a gerada e reporta GENERATED. */
    @Test
    fun `posta a mensagem gerada quando segura`() = runTest {
        val gen = Message(title = "Título gerado", body = "Corpo gerado seguro")
        val notifier = FakeNotifier()
        val t = trigger(this, FakeGenerator(gen), notifier)

        val source = t.fire()

        assertEquals(TestAlertTrigger.MessageSourceUsed.GENERATED, source)
        assertEquals("Título gerado", notifier.posted?.first)
        assertEquals("Corpo gerado seguro", notifier.posted?.second)
    }

    /** Gerador nulo → cai no template à mão e reporta TEMPLATE. */
    @Test
    fun `cai no template quando geracao falha`() = runTest {
        val notifier = FakeNotifier()
        val t = trigger(this, FakeGenerator(null), notifier)

        val source = t.fire()

        assertEquals(TestAlertTrigger.MessageSourceUsed.TEMPLATE, source)
        assert(notifier.posted != null)
    }

    /** Gerador devolve mensagem que fere P5 (cobrança) → guard rejeita, cai no template. */
    @Test
    fun `rejeita mensagem gerada insegura`() = runTest {
        val unsafe = Message(title = "Você precisa parar", body = "Larga esse celular agora")
        val notifier = FakeNotifier()
        val t = trigger(this, FakeGenerator(unsafe), notifier)

        val source = t.fire()

        assertEquals(TestAlertTrigger.MessageSourceUsed.TEMPLATE, source)
        assertNull("não deve postar o texto inseguro", notifier.posted?.takeIf { it.first == "Você precisa parar" })
    }

    /** Usa o id sentinela (-1) → não casa com nenhuma linha de outcome (não contamina o dado). */
    @Test
    fun `usa id sentinela`() = runTest {
        val notifier = FakeNotifier()
        val t = trigger(this, FakeGenerator(Message("t", "b")), notifier)

        t.fire()

        assertEquals(-1L, notifier.posted?.third)
    }
}
