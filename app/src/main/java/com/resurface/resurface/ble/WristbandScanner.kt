package com.resurface.resurface.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BLE discovery for the wristband.
 *
 * **Filtered on the service UUID, never on the advertised name.** Flags (3) + the
 * 128-bit service UUID (18) + `"Resurface"` (11) is 32 bytes against a 31-byte
 * advertising PDU, and the firmware never populates a scan response — so
 * `setDeviceName` may match nothing at all (README §10). The name is read only for
 * display, with an address-derived fallback.
 *
 * The returned flow scans while collected and stops on cancellation. Bounding it —
 * timeout, user stop, screen exit — is [WristbandRepository]'s job.
 */
@Singleton
class WristbandScanner @Inject constructor(
    private val environment: BluetoothEnvironment,
) {

    /** Emits the accumulated match list, newest information winning per address. */
    @SuppressLint("MissingPermission") // Readiness (incl. BLUETOOTH_SCAN) checked by the caller.
    fun scan(): Flow<List<DiscoveredWristband>> = callbackFlow {
        val scanner = environment.bluetoothLeScanner()
        if (scanner == null) {
            close(IllegalStateException("No BLE scanner: adapter unavailable"))
            return@callbackFlow
        }

        val found = LinkedHashMap<String, DiscoveredWristband>()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                found[device.address] = DiscoveredWristband(
                    address = device.address,
                    name = result.scanRecord?.deviceName ?: device.name,
                    rssi = result.rssi,
                )
                trySend(found.values.toList())
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("BLE scan failed with code $errorCode"))
            }
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleUuids.SERVICE))
                .build(),
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(filters, settings, callback)

        awaitClose { runCatching { scanner.stopScan(callback) } }
    }
}
