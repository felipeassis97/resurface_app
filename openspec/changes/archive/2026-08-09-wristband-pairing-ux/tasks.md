## 1. Camada BLE: desconectar

- [x] 1.1 `WristbandLink.disconnect()` (interface)
- [x] 1.2 `WristbandRepository.disconnect()`: fecha o gatt (`WristbandGattClient`) e emite `Disconnected`/`Idle`
- [x] 1.3 `forget`: `disconnect()` + `RememberedWristbandStore` limpa (não reconecta passivamente depois)

## 2. WristbandViewModel

- [x] 2.1 `WristbandUiState` (Rest / Scanning(devices) / Empty / Connecting / Connected / Failed)
- [x] 2.2 Derivar de `link.state` + `scanResults` + sinal de timeout
- [x] 2.3 `onScan()` com timeout de 12s → Empty se vazio; `onRetry()`
- [x] 2.4 Ações: `connect(address)`, `disconnect()`, `forget()`, `testPulse()` (`AlertHaptics.pulse`), `setIntensity`
- [x] 2.5 `signalBars(rssi)` (4/3/2/1)

## 3. Tela por estado

- [x] 3.1 Radar animado (anéis concêntricos, ícone central) no estado buscando; reduced-motion → estático
- [x] 3.2 Lista de devices ao vivo com barras de sinal; tocar conecta
- [x] 3.3 Empty state (dicas + Retry) após timeout
- [x] 3.4 Connecting (pulso "Connecting…")
- [x] 3.5 Connected: nome + selo + intensidade + "Send test pulse" + "Forget"
- [x] 3.6 Failed: mensagem por motivo + Retry
- [x] 3.7 Rest (repouso): CTA "Scan" pulsando

## 4. Limpeza

- [x] 4.1 Aposentar `WristbandSettingsSection` (a tela nova absorve)
- [x] 4.2 Remover métodos órfãos do `SettingsViewModel` (onPairWristband/onSetIntensity se não usados) e refs

## 5. Verificação

- [x] 5.1 `./gradlew :app:compileDebugKotlin` e `:app:testDebugUnitTest` passam
- [x] 5.2 Device: scan mostra radar; device aparece com sinal; tocar conecta; empty após timeout — **manual**
- [x] 5.3 Device: conectado mostra ações; test pulse vibra; forget desconecta e não reconecta — **manual**
