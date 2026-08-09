## Why

O aviso hoje só chega pela tela (notificação heads-up). Um canal **sem tela** — vibração
no pulso — é qualitativamente diferente: cutuca sem puxar de volta pra tela, que é
justamente o que a notificação faz. A pulseira de teste já existe e o `../resurface_old`
já tem uma conexão BLE madura e testada com ela. Trazer isso pro app fecha o loop físico
do nudge (roadmap PRODUTO §8 "v2 — pulseira háptica").

## What Changes

- **Portar o módulo BLE do old** (quase verbatim, adaptando package/DataStore/manifest):
  comando háptico, GATT client, scanner, ambiente/readiness, state machine, repositório
  dono do link, store do device lembrado, UUIDs, DI.
- **Vibra em todo aviso:** sempre que o app posta uma notificação (`Notifier.postAlert`),
  dispara junto um pulso na pulseira — **incluindo o aviso de teste** (vira o jeito de
  testar a pulseira sem esperar o limite). Fire-and-forget; se não há pulseira conectada,
  no-op silencioso (nunca atrasa nem quebra a notificação).
- **Efeito fixo `Gentle`** (1 pulso) com **intensidade configurável** e persistida.
- **Auto-conecta sempre que possível:** reconecta ao device lembrado passivamente
  (`autoConnect=true`, sem scan contínuo), disparado no start do FGS (`MonitorService`).
- **Pareamento inicial:** botão simples "Procurar e conectar" na tela de Ajustes (scan →
  conecta → lembra). UI rica fica pra depois; este botão é o mínimo pra capturar o device.
- Persistência da intensidade num `WristbandPreferences` **dedicado** (BLE é domínio à parte).

## Capabilities

### New Capabilities
- `wristband-link`: conexão BLE com a pulseira — readiness/permissões, scan+conectar
  (pareamento), lembrar device, auto-reconnect passivo, envio de comando háptico.
- `alert-haptics`: todo aviso postado dispara um pulso `Gentle` na pulseira, na intensidade
  configurada e persistida; no-op quando não conectado; inclui o aviso de teste.

### Modified Capabilities
<!-- Nenhuma. O acoplamento é via um seam AlertHaptics chamado pelo NotifierImpl; o
     requisito de entrega da notificação em si não muda. -->

## Impact

- **Novo pacote `com.resurface.resurface.ble`** (portado do old): `HapticCommand`/`HapticEffect`,
  `WristbandGattClient`, `WristbandScanner`, `BluetoothEnvironment`, `WristbandConnectionState`,
  `WristbandStateReducer`, `WristbandRepository`, `BleUuids`.
- **Novo `data/wristband/`**: `RememberedWristbandStore` (address) + `WristbandPreferences`
  (intensidade). Reusam o `DataStore<Preferences>` "resurface" já provido.
- **Seam novo** `AlertHaptics` (interface) + impl que lê a intensidade e chama
  `WristbandRepository.send(HapticCommand(Gentle, intensidade))`. **Hook** em
  `NotifierImpl.postAlert` (fire-and-forget).
- **`MonitorService`**: chama `repository.reconnectRemembered()` no start (auto-reconnect).
- **DI**: novo `BleModule` (@Singleton para repo/gatt/scanner/env/stores/haptics).
- **Manifest**: `BLUETOOTH_SCAN` (`neverForLocation`) + `BLUETOOTH_CONNECT`.
- **Settings**: botão "Procurar e conectar" + estado do link + slider de intensidade
  (lógica agora; UI polida depois).
- **Sem** alteração no motor de contagem, episódio, política ou banco Room.
