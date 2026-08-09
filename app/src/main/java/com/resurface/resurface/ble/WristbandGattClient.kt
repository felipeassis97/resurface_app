package com.resurface.resurface.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/** What the GATT layer reports upward. Mapped to [WristbandConnectionState] by the repository. */
sealed interface GattLinkEvent {
    /** Which device the event is about — every event has one, including failures. */
    val address: String

    /** Connected, services discovered, command characteristic present. Ready to write. */
    data class Ready(override val address: String, val name: String?) : GattLinkEvent

    /** The link went away after having been established. */
    data class Dropped(override val address: String, val status: Int) : GattLinkEvent

    data class Failed(
        override val address: String,
        val reason: ConnectionFailure,
        val detail: String?,
    ) : GattLinkEvent
}

/**
 * Owns the single [BluetoothGatt] instance and the write path.
 *
 * Two rules the platform enforces and this class encodes:
 *  - **Always `close()` before reconnecting.** Leaked GATT clients are the usual cause
 *    of repeat `status = 133` failures.
 *  - **One outstanding operation per connection.** A second write issued before the
 *    first completes returns busy and the command silently disappears, so [write] is
 *    serialized behind a [Mutex].
 *
 * Callbacks arrive on a binder thread and do no work here beyond emitting a value —
 * the same discipline the firmware's own write callback follows.
 */
@Singleton
class WristbandGattClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val environment: BluetoothEnvironment,
) {

    private val _events = MutableSharedFlow<GattLinkEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<GattLinkEvent> = _events

    @Volatile
    private var gatt: BluetoothGatt? = null

    @Volatile
    private var commandCharacteristic: BluetoothGattCharacteristic? = null

    /** Guards the single-slot GATT operation queue. */
    private val writeMutex = Mutex()

    @Volatile
    private var pendingWrite: CompletableDeferred<Int>? = null

    val isConnected: Boolean
        get() = commandCharacteristic != null

    private val callback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device?.address ?: "?"
            when {
                newState == BluetoothProfile.STATE_CONNECTED &&
                    status == BluetoothGatt.GATT_SUCCESS -> {
                    // Do not report Ready yet — the command characteristic is the real gate.
                    discoverServices(gatt)
                }

                newState == BluetoothProfile.STATE_DISCONNECTED -> {
                    val wasEstablished = commandCharacteristic != null
                    teardown()
                    if (wasEstablished) {
                        _events.tryEmit(GattLinkEvent.Dropped(address, status))
                    } else {
                        _events.tryEmit(
                            GattLinkEvent.Failed(
                                address = address,
                                reason = ConnectionFailure.GATT_ERROR,
                                detail = "Disconnected before ready (status $status)",
                            ),
                        )
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val address = gatt.device?.address ?: "?"
            if (status != BluetoothGatt.GATT_SUCCESS) {
                teardown()
                _events.tryEmit(
                    GattLinkEvent.Failed(
                        address = address,
                        reason = ConnectionFailure.GATT_ERROR,
                        detail = "Service discovery failed (status $status)",
                    ),
                )
                return
            }
            // A connection is valid on the command characteristic alone. The status
            // characteristic (…0003) is documented but absent from firmware v3, so its
            // absence must never block or delay readiness.
            val characteristic = gatt.getService(BleUuids.SERVICE)
                ?.getCharacteristic(BleUuids.COMMAND_CHARACTERISTIC)
            if (characteristic == null) {
                teardown()
                _events.tryEmit(
                    GattLinkEvent.Failed(
                        address = address,
                        reason = ConnectionFailure.COMMAND_CHARACTERISTIC_MISSING,
                        detail = "Command characteristic not found on this device",
                    ),
                )
                return
            }
            commandCharacteristic = characteristic
            _events.tryEmit(GattLinkEvent.Ready(address, readDeviceName(gatt)))
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            pendingWrite?.complete(status)
        }
    }

    /**
     * Starts a connection attempt. Any previous client is closed first.
     *
     * @param autoConnect true for the remembered-device path — slower to establish, but
     *   the OS reconnects opportunistically whenever the wristband comes into range,
     *   which pairs with the firmware's `restartOnDisconnect(true)`.
     */
    @SuppressLint("MissingPermission") // Readiness checked by the caller before connecting.
    fun connect(address: String, autoConnect: Boolean): Boolean {
        close()
        val device = environment.remoteDevice(address)
        if (device == null) {
            _events.tryEmit(
                GattLinkEvent.Failed(
                    address = address,
                    reason = ConnectionFailure.BLUETOOTH_UNAVAILABLE,
                    detail = "Adapter cannot resolve $address",
                ),
            )
            return false
        }
        return runCatching {
            gatt = device.connectGatt(context, autoConnect, callback)
            gatt != null
        }.getOrElse { error ->
            _events.tryEmit(
                GattLinkEvent.Failed(
                    address = address,
                    reason = ConnectionFailure.GATT_ERROR,
                    detail = error.message,
                ),
            )
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.let { runCatching { it.disconnect() } }
        close()
    }

    /**
     * Writes one payload with write-without-response — the characteristic declares it,
     * and the tap has to feel immediate. The ATT layer still acknowledges, so the write
     * is awaited to keep the single-slot queue honest.
     */
    @SuppressLint("MissingPermission")
    suspend fun write(payload: ByteArray): SendResult = writeMutex.withLock {
        val gatt = gatt ?: return@withLock SendResult.NotConnected
        val characteristic = commandCharacteristic ?: return@withLock SendResult.NotConnected

        val ack = CompletableDeferred<Int>()
        pendingWrite = ack
        val requestStatus = runCatching {
            gatt.writeCharacteristic(
                characteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
            )
        }.getOrElse { error ->
            pendingWrite = null
            return@withLock SendResult.Failed(-1, error.message)
        }
        if (requestStatus != BluetoothStatusCodes.SUCCESS) {
            pendingWrite = null
            return@withLock SendResult.Failed(requestStatus, "Write rejected by the platform")
        }

        val status = withTimeoutOrNull(WRITE_ACK_TIMEOUT_MILLIS) { ack.await() }
        pendingWrite = null
        return@withLock when {
            status == null -> SendResult.Failed(-1, "No write acknowledgement in time")
            status == BluetoothGatt.GATT_SUCCESS -> SendResult.Success
            else -> SendResult.Failed(status, "GATT write failed")
        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverServices(gatt: BluetoothGatt) {
        if (!gatt.discoverServices()) {
            val address = gatt.device?.address ?: "?"
            teardown()
            _events.tryEmit(
                GattLinkEvent.Failed(
                    address = address,
                    reason = ConnectionFailure.GATT_ERROR,
                    detail = "Could not start service discovery",
                ),
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun readDeviceName(gatt: BluetoothGatt): String? =
        runCatching { gatt.device?.name }.getOrNull()

    /** Releases the client. Mandatory before any reconnection attempt. */
    fun close() {
        commandCharacteristic = null
        pendingWrite?.complete(BluetoothGatt.GATT_FAILURE)
        pendingWrite = null
        gatt?.let { existing ->
            runCatching { existing.close() }
                .onFailure { Log.w(TAG, "Closing GATT client failed", it) }
        }
        gatt = null
    }

    private fun teardown() {
        commandCharacteristic = null
        pendingWrite?.complete(BluetoothGatt.GATT_FAILURE)
        pendingWrite = null
        gatt?.let { runCatching { it.close() } }
        gatt = null
    }

    private companion object {
        const val TAG = "WristbandGatt"
        const val WRITE_ACK_TIMEOUT_MILLIS = 2_000L
    }
}
