package com.resurface.resurface.ble

import java.util.UUID

/**
 * Single source of truth for the wristband's GATT identifiers (README §10).
 *
 * Only [COMMAND_CHARACTERISTIC] is registered by firmware v3. A status/battery
 * characteristic at `…0003` is documented as planned but does not exist on the
 * flashed device — nothing may wait on it.
 */
object BleUuids {
    val SERVICE: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    val COMMAND_CHARACTERISTIC: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

    /**
     * The advertised name, kept for display only. Scans must filter on [SERVICE] —
     * flags (3) + the 128-bit UUID (18) + this name (11) is 32 bytes against a 31-byte
     * advertising PDU, so the name may be truncated or absent entirely.
     */
    const val ADVERTISED_NAME: String = "Resurface"
}
