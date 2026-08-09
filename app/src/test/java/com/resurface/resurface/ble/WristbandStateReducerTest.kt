package com.resurface.resurface.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WristbandStateReducerTest {

    private val address = "AA:BB:CC:DD:EE:FF"

    @Test
    fun `ready connects regardless of the previous state`() {
        val previous = listOf(
            WristbandConnectionState.Idle,
            WristbandConnectionState.Connecting(address, attempt = 2),
            WristbandConnectionState.Disconnected(address),
        )
        for (current in previous) {
            val transition = WristbandStateReducer.reduce(
                current,
                GattLinkEvent.Ready(address, "Resurface"),
            )
            assertEquals(
                WristbandConnectionState.Connected(address, "Resurface"),
                transition.state,
            )
            assertFalse(transition.retry)
        }
    }

    @Test
    fun `connected state can send`() {
        assertTrue(WristbandConnectionState.Connected(address, null).canSend)
        assertFalse(WristbandConnectionState.Idle.canSend)
        assertFalse(WristbandConnectionState.Scanning.canSend)
        assertFalse(WristbandConnectionState.Connecting(address).canSend)
        assertFalse(WristbandConnectionState.Disconnected(address).canSend)
    }

    @Test
    fun `drop becomes disconnected and does not retry`() {
        val transition = WristbandStateReducer.reduce(
            WristbandConnectionState.Connected(address, null),
            GattLinkEvent.Dropped(address, status = 19),
        )
        assertEquals(WristbandConnectionState.Disconnected(address), transition.state)
        assertFalse(transition.retry)
    }

    @Test
    fun `transient gatt error retries with an incremented attempt`() {
        val transition = WristbandStateReducer.reduce(
            WristbandConnectionState.Connecting(address, attempt = 1),
            GattLinkEvent.Failed(address, ConnectionFailure.GATT_ERROR, "status 133"),
        )
        assertTrue(transition.retry)
        assertEquals(WristbandConnectionState.Connecting(address, attempt = 2), transition.state)
    }

    @Test
    fun `retries stop at the cap and surface the failure`() {
        val transition = WristbandStateReducer.reduce(
            WristbandConnectionState.Connecting(
                address,
                attempt = WristbandStateReducer.MAX_ATTEMPTS,
            ),
            GattLinkEvent.Failed(address, ConnectionFailure.GATT_ERROR, "status 133"),
        )
        assertFalse(transition.retry)
        val failed = transition.state as WristbandConnectionState.Failed
        assertEquals(ConnectionFailure.GATT_ERROR, failed.reason)
        assertTrue(
            "detail should say it gave up: ${failed.detail}",
            failed.detail?.contains("Gave up") == true,
        )
    }

    @Test
    fun `missing command characteristic never retries`() {
        // Retrying cannot conjure a characteristic — this is the wrong device.
        val transition = WristbandStateReducer.reduce(
            WristbandConnectionState.Connecting(address, attempt = 1),
            GattLinkEvent.Failed(
                address,
                ConnectionFailure.COMMAND_CHARACTERISTIC_MISSING,
                "not found",
            ),
        )
        assertFalse(transition.retry)
        assertEquals(
            ConnectionFailure.COMMAND_CHARACTERISTIC_MISSING,
            (transition.state as WristbandConnectionState.Failed).reason,
        )
    }

    @Test
    fun `failure outside a connect attempt does not retry`() {
        val transition = WristbandStateReducer.reduce(
            WristbandConnectionState.Idle,
            GattLinkEvent.Failed(address, ConnectionFailure.GATT_ERROR, "spurious"),
        )
        assertFalse(transition.retry)
    }

    @Test
    fun `backoff grows with the attempt`() {
        val first = WristbandStateReducer.backoffMillis(1)
        val second = WristbandStateReducer.backoffMillis(2)
        assertTrue("backoff should grow: $first then $second", second > first)
    }

    @Test
    fun `readiness maps to a blocking state except when ready`() {
        assertEquals(null, WristbandStateReducer.fromReadiness(BluetoothReadiness.Ready))

        val adapterOff = WristbandStateReducer.fromReadiness(BluetoothReadiness.AdapterOff)
                as WristbandConnectionState.Failed
        assertEquals(ConnectionFailure.BLUETOOTH_UNAVAILABLE, adapterOff.reason)

        val unsupported = WristbandStateReducer.fromReadiness(BluetoothReadiness.BleUnsupported)
                as WristbandConnectionState.Failed
        assertEquals(ConnectionFailure.BLUETOOTH_UNAVAILABLE, unsupported.reason)

        val missing = WristbandStateReducer.fromReadiness(
            BluetoothReadiness.PermissionsMissing(listOf("android.permission.BLUETOOTH_SCAN")),
        ) as WristbandConnectionState.Failed
        assertEquals(ConnectionFailure.PERMISSION_DENIED, missing.reason)
    }

    // --- send-result mapping ---

    @Test
    fun `send results describe themselves for the log`() {
        assertEquals("sent", SendResult.Success.describe())
        assertEquals("not connected", SendResult.NotConnected.describe())
        assertEquals(
            "GATT write failed — status 8",
            SendResult.Failed(status = 8, detail = "GATT write failed").describe(),
        )
    }

    @Test
    fun `a failure with no usable status omits it`() {
        assertEquals(
            "No write acknowledgement in time",
            SendResult.Failed(status = -1, detail = "No write acknowledgement in time").describe(),
        )
    }

    @Test
    fun `only success reports success`() {
        assertTrue(SendResult.Success.isSuccess)
        assertFalse(SendResult.NotConnected.isSuccess)
        assertFalse(SendResult.Failed(8).isSuccess)
    }

    @Test
    fun `discovered wristband falls back to an address label when unnamed`() {
        // The advertised name does not fit in the 31-byte PDU, so it may be missing.
        assertEquals("Resurface", DiscoveredWristband(address, "Resurface", -50).displayName)
        assertTrue(DiscoveredWristband(address, null, -50).displayName.contains("EE:FF"))
        assertTrue(DiscoveredWristband(address, "  ", -50).displayName.contains("EE:FF"))
    }
}
