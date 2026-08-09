package com.resurface.resurface.ble

import com.resurface.resurface.data.wristband.RememberedWristbandStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The single owner of the wristband link.
 *
 * Application-scoped on purpose: the connection must survive rotation and screen
 * navigation, and a later foreground sender (F8) will have no UI at all. A ViewModel
 * holding the GATT could do neither. Constructed only by `BleModule`, which scopes it
 * `@Singleton` — [dispatcher] carries a default that Dagger cannot see, so there is no
 * `@Inject` constructor to be confused about.
 */
class WristbandRepository(
    private val environment: BluetoothEnvironment,
    private val scanner: WristbandScanner,
    private val gattClient: WristbandGattClient,
    private val rememberedStore: RememberedWristbandStore,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : HapticSender, WristbandLink {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _state = MutableStateFlow<WristbandConnectionState>(WristbandConnectionState.Idle)
    override val state: StateFlow<WristbandConnectionState> = _state.asStateFlow()

    /** Last scan's matches, kept after the scan ends so the list stays on screen. */
    private val _scanResults = MutableStateFlow<List<DiscoveredWristband>>(emptyList())
    override val scanResults: StateFlow<List<DiscoveredWristband>> = _scanResults.asStateFlow()

    val rememberedAddress: Flow<String?> = rememberedStore.address

    private var scanJob: Job? = null
    private var connectJob: Job? = null

    /** Manual selections connect fast; the remembered device reconnects opportunistically. */
    @Volatile
    private var autoConnectTarget: Boolean = false

    init {
        scope.launch {
            gattClient.events.collect { event -> onLinkEvent(event) }
        }
    }

    fun readiness(): BluetoothReadiness = environment.readiness()

    // --- scanning -------------------------------------------------------------

    /**
     * Starts a bounded scan. Stops on [stopScan], on [SCAN_TIMEOUT_MILLIS], or when a
     * new scan replaces it. Nothing here scans in the background — BLE scanning costs
     * battery and Android throttles apps that never stop.
     */
    override fun startScan() {
        WristbandStateReducer.fromReadiness(readiness())?.let { blocked ->
            _state.value = blocked
            return
        }
        scanJob?.cancel()
        _scanResults.value = emptyList()
        _state.value = WristbandConnectionState.Scanning
        scanJob = scope.launch {
            withTimeoutOrNull(SCAN_TIMEOUT_MILLIS) {
                scanner.scan()
                    .catch { error ->
                        _state.value = WristbandConnectionState.Failed(
                            reason = ConnectionFailure.GATT_ERROR,
                            detail = error.message ?: "Scan failed",
                        )
                    }
                    .collect { found ->
                        // Never auto-connect to the first match: more than one wristband
                        // may be in range, and the choice is the user's.
                        _scanResults.value = found
                    }
            }
            // Timed out or ended: stop scanning, leave the results on screen.
            if (_state.value is WristbandConnectionState.Scanning) {
                _state.value = WristbandConnectionState.Idle
            }
        }
    }

    override fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        if (_state.value is WristbandConnectionState.Scanning) {
            _state.value = WristbandConnectionState.Idle
        }
    }

    // --- connecting -----------------------------------------------------------

    /** Connects to a device the user picked out of the scan list. */
    override fun connect(address: String) = connectInternal(address, autoConnect = false)

    /**
     * Reconnects to the remembered wristband without scanning. No-op when nothing is
     * remembered or a link is already up.
     */
    override fun reconnectRemembered() {
        scope.launch {
            val address = rememberedStore.current() ?: return@launch
            if (gattClient.isConnected) return@launch
            connectInternal(address, autoConnect = true)
        }
    }

    private fun connectInternal(address: String, autoConnect: Boolean) {
        WristbandStateReducer.fromReadiness(readiness())?.let { blocked ->
            _state.value = blocked
            return
        }
        scanJob?.cancel()
        scanJob = null
        connectJob?.cancel()
        autoConnectTarget = autoConnect
        _state.value = WristbandConnectionState.Connecting(address, attempt = 1)
        connectJob = scope.launch { gattClient.connect(address, autoConnect) }
    }

    fun disconnect() {
        connectJob?.cancel()
        connectJob = null
        gattClient.disconnect()
        val address = (_state.value as? WristbandConnectionState.Connected)?.address
        _state.value = address
            ?.let { WristbandConnectionState.Disconnected(it) }
            ?: WristbandConnectionState.Idle
    }

    /** Clears the remembered device and tears down any link to it. */
    fun forget() {
        scope.launch {
            connectJob?.cancel()
            connectJob = null
            gattClient.disconnect()
            rememberedStore.forget()
            _state.value = WristbandConnectionState.Idle
        }
    }

    private suspend fun onLinkEvent(event: GattLinkEvent) {
        val transition = WristbandStateReducer.reduce(_state.value, event)
        _state.value = transition.state
        if (event is GattLinkEvent.Ready) {
            // Replaces any previously remembered wristband.
            rememberedStore.remember(event.address)
        }
        if (transition.retry) {
            val attempt = (transition.state as? WristbandConnectionState.Connecting)?.attempt ?: 1
            delay(WristbandStateReducer.backoffMillis(attempt))
            // connect() closes the previous client first — leaked clients cause repeat 133s.
            gattClient.connect(event.address, autoConnectTarget)
        }
    }

    // --- sending --------------------------------------------------------------

    /** Writes one command. Serialization is handled inside [WristbandGattClient]. */
    override suspend fun send(command: HapticCommand): SendResult {
        if (!_state.value.canSend) return SendResult.NotConnected
        return gattClient.write(command.toBytes())
    }

    private companion object {
        const val SCAN_TIMEOUT_MILLIS = 15_000L
    }
}
