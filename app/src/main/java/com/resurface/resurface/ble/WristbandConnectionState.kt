package com.resurface.resurface.ble

/** A wristband seen during a scan. The name is decorative — see [BleUuids.ADVERTISED_NAME]. */
data class DiscoveredWristband(
    val address: String,
    val name: String?,
    val rssi: Int,
) {
    /** Never blank: the advertised name may be truncated or absent entirely. */
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() }
            ?: "Wristband (${address.takeLast(5)})"
}

/** Why a connection attempt ended badly. Carried in [WristbandConnectionState.Failed]. */
enum class ConnectionFailure {
    /** The Bluetooth permissions are not granted. */
    PERMISSION_DENIED,

    /** The adapter is off, or the device has no BLE radio. */
    BLUETOOTH_UNAVAILABLE,

    /** Connected and discovered, but the command characteristic is not there. */
    COMMAND_CHARACTERISTIC_MISSING,

    /** GATT reported an error, or the retry cap was reached. */
    GATT_ERROR,

    /** The attempt did not resolve in time. */
    TIMEOUT,
}

/**
 * The wristband link, owned by [WristbandRepository] and observed by the UI.
 *
 * Deliberately flat — the screen renders one of these six and nothing else.
 */
sealed interface WristbandConnectionState {

    /** Nothing in progress, nothing connected. */
    data object Idle : WristbandConnectionState

    /**
     * A bounded scan is running. Results are not part of the connection state — they
     * live in their own flow so that "scan finished, results still on screen" does not
     * need a seventh state.
     */
    data object Scanning : WristbandConnectionState

    data class Connecting(val address: String, val attempt: Int = 1) : WristbandConnectionState

    /** Connected with the command characteristic present. Ready to send. */
    data class Connected(val address: String, val name: String?) : WristbandConnectionState

    /** Was connected, no longer is. Distinct from [Idle], which never connected. */
    data class Disconnected(val address: String) : WristbandConnectionState

    data class Failed(
        val reason: ConnectionFailure,
        val detail: String? = null,
        val address: String? = null,
    ) : WristbandConnectionState

    /** True only when a command can actually be written. */
    val canSend: Boolean
        get() = this is Connected
}

/** The outcome of writing one command. */
sealed interface SendResult {
    data object Success : SendResult

    /** No link, so nothing was written. */
    data object NotConnected : SendResult

    /** The platform rejected or failed the write. [status] is the GATT status code. */
    data class Failed(val status: Int, val detail: String? = null) : SendResult

    val isSuccess: Boolean
        get() = this is Success
}
