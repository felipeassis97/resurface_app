package com.resurface.resurface.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Everything that must be true before a scan can even be attempted. */
sealed interface BluetoothReadiness {
    data object Ready : BluetoothReadiness

    /** No BLE radio. The wristband feature is unavailable on this device, permanently. */
    data object BleUnsupported : BluetoothReadiness

    /** Radio present but switched off. */
    data object AdapterOff : BluetoothReadiness

    /** [missing] permissions still need granting. */
    data class PermissionsMissing(val missing: List<String>) : BluetoothReadiness
}

/**
 * Live Bluetooth capability and permission status, read from the OS on every call —
 * never cached — because the user can toggle the radio or revoke a permission outside
 * the app, exactly as `PermissionChecker` does for the onboarding trio.
 *
 * Bluetooth is deliberately absent from `AppPermission`: it is requested just-in-time
 * by this feature, not during onboarding.
 */
@Singleton
class BluetoothEnvironment @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val adapter: BluetoothAdapter?
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    /**
     * Scanning needs BLUETOOTH_SCAN, connecting and writing need BLUETOOTH_CONNECT.
     * No location permission: the manifest asserts `neverForLocation`.
     */
    val requiredPermissions: List<String> = listOf(
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
    )

    fun missingPermissions(): List<String> = requiredPermissions.filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

    fun hasPermissions(): Boolean = missingPermissions().isEmpty()

    fun readiness(): BluetoothReadiness {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return BluetoothReadiness.BleUnsupported
        }
        val missing = missingPermissions()
        if (missing.isNotEmpty()) return BluetoothReadiness.PermissionsMissing(missing)
        val adapter = adapter ?: return BluetoothReadiness.BleUnsupported
        if (!adapter.isEnabled) return BluetoothReadiness.AdapterOff
        return BluetoothReadiness.Ready
    }

    /** The device's remembered wristband, resolvable without scanning. */
    fun remoteDevice(address: String) = adapter?.getRemoteDevice(address)

    fun bluetoothLeScanner() = adapter?.bluetoothLeScanner

    /**
     * Bluetooth settings rather than `ACTION_REQUEST_ENABLE`: the latter needs
     * BLUETOOTH_CONNECT, which is exactly what may be missing when the adapter is off.
     */
    fun bluetoothSettingsIntent(): Intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

    /** For permanent denial, where the runtime dialog no longer appears. */
    fun appSettingsIntent(): Intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    )
}
