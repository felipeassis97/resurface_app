## 1. Portar o módulo BLE (com.resurface.resurface.ble)

- [x] 1.1 Copiar do old, trocando package: `BleUuids`, `HapticCommand`+`HapticEffect`,
      `WristbandConnectionState` (+`SendResult`), `WristbandStateReducer`,
      `BluetoothEnvironment`, `WristbandScanner`, `WristbandGattClient` (+`GattLinkEvent`).
- [x] 1.2 `WristbandRepository` (port): dono @Singleton do link (scan/connect/
      reconnectRemembered/send/disconnect/forget).
- [x] 1.3 Portar os testes puros: `HapticCommandTest`, `WristbandStateReducerTest`.

## 2. Persistência (data/wristband)

- [x] 2.1 `RememberedWristbandStore` (port): address no DataStore "resurface".
- [x] 2.2 `WristbandPreferences` (novo): `intensity: Flow<Int?>` + `setIntensity(Int?)`,
      chave `wristband_intensity`. Null = auto.

## 3. Seam de vibração + hook

- [x] 3.1 `AlertHaptics` (interface) + impl: lê intensidade, chama
      `repository.send(HapticCommand(HapticEffect.Gentle, intensity))` fire-and-forget.
- [x] 3.2 Hook em `NotifierImpl.postAlert`: chamar `haptics.pulse()` ao fim (injeta seam).
- [x] 3.3 Confirmar que o aviso de teste (`TestAlertTrigger` → postAlert) também vibra.

## 4. Auto-reconnect + DI + manifest

- [x] 4.1 `BleModule` (Hilt @Singleton): repo, gatt, scanner, environment, stores, haptics.
- [x] 4.2 `MonitorService`: no start, chamar `repository.reconnectRemembered()`.
- [x] 4.3 Manifest: `BLUETOOTH_SCAN` (`neverForLocation`) + `BLUETOOTH_CONNECT`.

## 5. Settings (lógica; UI polida depois)

- [x] 5.1 `SettingsViewModel`: expor estado do link + intensidade; `onPairWristband()`
      (startScan→conecta 1º match), `onSetIntensity(Int?)`. Pedir permissão BLE just-in-time.
- [x] 5.2 `SettingsScreen`: botão "Procurar e conectar" + status do link + slider de
      intensidade. Tokens de tema (G10).

## 6. Testes e verificação

- [x] 6.1 `HapticCommandTest` + `WristbandStateReducerTest` verdes (portados).
- [x] 6.2 `AlertHaptics` impl (G11): com pulseira "conectada" (fake repo) → envia Gentle com
      a intensidade gravada; sem conexão → no-op sem erro.
- [x] 6.3 `WristbandPreferences`: round-trip da intensidade (grava/lê/null).
- [x] 6.4 `./gradlew :app:testDebugUnitTest` verde.
- [x] 6.5 Device + pulseira: VERIFICADO pelo usuário — pulseira conectada, aviso chegou no
      celular E na pulseira (vibração real). Estado "Pulseira conectada"; address lembrado
      gravado (F0:D8:F9:30:4E:74) → auto-reconnect pronto pro próximo start.
