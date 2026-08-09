package com.resurface.resurface.ble

/** A state change plus whether the repository should mount another connection attempt. */
data class LinkTransition(
    val state: WristbandConnectionState,
    val retry: Boolean = false,
)

/**
 * The connection state machine, kept pure so it can be tested without a radio.
 *
 * The retry policy lives here rather than in the GATT callbacks because the decision
 * depends on *why* the attempt failed: a transient GATT error is worth retrying, a
 * device without the command characteristic is not — it is simply the wrong device.
 */
object WristbandStateReducer {

    /** Attempts per user-initiated connect, after which the failure is surfaced. */
    const val MAX_ATTEMPTS = 3

    fun backoffMillis(attempt: Int): Long = 500L * attempt

    fun reduce(current: WristbandConnectionState, event: GattLinkEvent): LinkTransition =
        when (event) {
            is GattLinkEvent.Ready ->
                LinkTransition(WristbandConnectionState.Connected(event.address, event.name))

            is GattLinkEvent.Dropped ->
                LinkTransition(WristbandConnectionState.Disconnected(event.address))

            is GattLinkEvent.Failed -> reduceFailure(current, event)
        }

    private fun reduceFailure(
        current: WristbandConnectionState,
        event: GattLinkEvent.Failed,
    ): LinkTransition {
        val attempt = (current as? WristbandConnectionState.Connecting)?.attempt ?: 1
        val retryable = event.reason == ConnectionFailure.GATT_ERROR &&
            current is WristbandConnectionState.Connecting &&
            attempt < MAX_ATTEMPTS
        return if (retryable) {
            LinkTransition(
                state = WristbandConnectionState.Connecting(event.address, attempt + 1),
                retry = true,
            )
        } else {
            LinkTransition(
                WristbandConnectionState.Failed(
                    reason = event.reason,
                    detail = failureDetail(event, attempt),
                    address = event.address,
                ),
            )
        }
    }

    private fun failureDetail(event: GattLinkEvent.Failed, attempt: Int): String? = when {
        event.reason == ConnectionFailure.GATT_ERROR && attempt >= MAX_ATTEMPTS ->
            listOfNotNull(event.detail, "Gave up after $MAX_ATTEMPTS attempts")
                .joinToString(" — ")

        else -> event.detail
    }

    /** How a readiness problem surfaces before any GATT work is attempted. */
    fun fromReadiness(readiness: BluetoothReadiness): WristbandConnectionState? =
        when (readiness) {
            BluetoothReadiness.Ready -> null

            BluetoothReadiness.AdapterOff -> WristbandConnectionState.Failed(
                reason = ConnectionFailure.BLUETOOTH_UNAVAILABLE,
                detail = "Bluetooth is turned off",
            )

            BluetoothReadiness.BleUnsupported -> WristbandConnectionState.Failed(
                reason = ConnectionFailure.BLUETOOTH_UNAVAILABLE,
                detail = "This device has no Bluetooth LE radio",
            )

            is BluetoothReadiness.PermissionsMissing -> WristbandConnectionState.Failed(
                reason = ConnectionFailure.PERMISSION_DENIED,
                detail = "Bluetooth permission is required to find the wristband",
            )
        }
}

/** One line describing a send outcome, for the debug console's log. */
fun SendResult.describe(): String = when (this) {
    SendResult.Success -> "sent"
    SendResult.NotConnected -> "not connected"
    is SendResult.Failed -> listOfNotNull(
        detail,
        status.takeIf { it >= 0 }?.let { "status $it" },
    ).joinToString(" — ").ifEmpty { "failed" }
}
