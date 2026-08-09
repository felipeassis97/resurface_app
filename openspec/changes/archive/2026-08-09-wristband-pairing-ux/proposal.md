## Why

A tela de pulseira é a mais "física" do app (parear um hardware), mas hoje é a mais sem graça: um botão "Scan and connect", um texto de estado e um slider. Um pareamento vivo, estilo Apple Watch / Samsung Watch (radar animado, devices aparecendo, empty state, estado conectado bonito), dá vida ao app e destaque pra essa feature.

## What Changes

- **Redesenhar a tela Wristband** como um fluxo por estado, com uma **animação de radar/sonar** (anéis concêntricos) durante o scan (a assinatura da tela). Reduced-motion → estático.
- **Escolher da lista** em vez de auto-conectar no primeiro: os devices encontrados aparecem ao vivo como cards, com **barras de sinal a partir do rssi**; tocar conecta. **BREAKING** (comportamento de pareamento): deixa de auto-conectar no primeiro.
- **Empty state** após timeout de scan (12s) sem achar nada: "No wristband found" + dicas (ligar a pulseira, aproximar) + Retry.
- **Estado conectado rico**: nome + badge "Connected" (pulso calmo) + intensidade + **Send test pulse** (reusa o caminho de vibração) + **Forget**.
- **Desconectar de verdade**: adicionar `disconnect()` à camada BLE (hoje só há connect/reconnect); "Forget" desconecta e limpa o device lembrado.
- **VM dedicado** `WristbandViewModel` (a tela é própria), expondo `state`, `scanResults`, intensidade e as ações.

Fora de escopo: bateria da pulseira (não existe no estado/protocolo); múltiplas pulseiras (o produto é 1).

## Capabilities

### New Capabilities

- `wristband-pairing`: a experiência de pareamento — scan animado (radar), lista de devices ao vivo com sinal, empty state, apresentação de conectado (test pulse + forget) e falhas legíveis com retry.

### Modified Capabilities

- `wristband-link`: o pareamento passa a **conectar ao device escolhido na lista** (não ao primeiro automático); e ganha **desconectar** (`disconnect()` + limpar lembrado).

## Impact

- **UI:** reescrever `ui/screens/settings/WristbandScreen` (estados + radar); `WristbandSettingsSection` é absorvido/aposentado. Novo `WristbandViewModel`.
- **BLE:** `WristbandLink` (+`disconnect()`), `WristbandRepository`/`WristbandGattClient` (fechar o gatt), `RememberedWristbandStore` (limpar no forget).
- **Reusa:** `WristbandLink.startScan/stopScan/connect`, `scanResults` (com rssi/nome), `WristbandPreferences.intensity`, `AlertHaptics.pulse` (test pulse).
- **Motion:** estende `ResurfaceMotion` se preciso (anéis); reduced-motion respeitado.
- **Não muda:** o hub de ajustes (a linha Wristband já existe), permissões (BLE já declaradas), o serviço/reconexão passiva.
