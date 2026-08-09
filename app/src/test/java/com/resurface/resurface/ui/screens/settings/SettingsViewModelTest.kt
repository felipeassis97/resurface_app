package com.resurface.resurface.ui.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.MidnightClock
import com.resurface.resurface.data.config.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(mainDispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

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

    private fun profile(scope: TestScope): com.resurface.resurface.data.profile.ProfileRepository {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("prof-${System.nanoTime()}.preferences_pb")
        }
        return com.resurface.resurface.data.profile.ProfileRepository(ds, UnconfinedTestDispatcher(scope.testScheduler))
    }

    // Link BLE fake: estado fixo Idle, métodos no-op (os testes não exercitam pareamento).
    private class FakeLink : com.resurface.resurface.ble.WristbandLink {
        override val state = kotlinx.coroutines.flow.MutableStateFlow<com.resurface.resurface.ble.WristbandConnectionState>(
            com.resurface.resurface.ble.WristbandConnectionState.Idle,
        )
        override val scanResults = kotlinx.coroutines.flow.MutableStateFlow<List<com.resurface.resurface.ble.DiscoveredWristband>>(emptyList())
        override fun startScan() {}
        override fun stopScan() {}
        override fun connect(address: String) {}
        override fun reconnectRemembered() {}
    }

    private fun wristbandPrefs(scope: TestScope): com.resurface.resurface.data.wristband.WristbandPreferences {
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            tmp.newFile("wb-${System.nanoTime()}.preferences_pb")
        }
        return com.resurface.resurface.data.wristband.WristbandPreferences(ds, UnconfinedTestDispatcher(scope.testScheduler))
    }

    private fun vm(scope: TestScope) = SettingsViewModel(config(scope), profile(scope), FakeLink(), wristbandPrefs(scope))

    /** O UiState expõe o limite padrão (20) sem nada gravado. */
    @Test
    fun `expoe o limite padrao`() = runTest {
        val vm = vm(this)
        assertEquals(20, vm.uiState.first().limitMinutes)
    }

    /** onSetLimit dentro da faixa grava e reflete. */
    @Test
    fun `set limit grava`() = runTest {
        val vm = vm(this)
        vm.onSetLimit(30)
        assertEquals(30, vm.uiState.first { it.limitMinutes == 30 }.limitMinutes)
    }

    /** onSetLimit fora da faixa não muda o valor. */
    @Test
    fun `set limit fora da faixa nao muda`() = runTest {
        val vm = vm(this)
        vm.onSetLimit(30)
        vm.uiState.first { it.limitMinutes == 30 }
        vm.onSetLimit(5)
        assertEquals(30, vm.uiState.first().limitMinutes)
    }

    /** onPauseToday marca pausado. */
    @Test
    fun `pausar por hoje marca pausado`() = runTest {
        val vm = vm(this)
        vm.onPauseToday()
        assertTrue(vm.uiState.first { it.pausedToday }.pausedToday)
    }
}
