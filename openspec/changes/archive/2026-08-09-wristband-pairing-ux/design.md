## Context

`WristbandScreen` embrulha a `WristbandSettingsSection`: texto de estado + botão "Scan and connect" (auto-conecta no 1º via `SettingsViewModel.onPairWristband`) + slider de intensidade. A camada BLE (`WristbandLink`) expõe `state` (6 estados), `scanResults` (`DiscoveredWristband{address,name,rssi}` ao vivo), `startScan/stopScan/connect/reconnectRemembered` — mas **não tem `disconnect()`**. `AlertHaptics.pulse()` existe (fire-and-forget). `WristbandPreferences.intensity` e `RememberedWristbandStore` (device lembrado) existem.

## Goals / Non-Goals

**Goals:**
- Pareamento vivo por estado, com radar animado (assinatura).
- Escolher da lista (cards com sinal do rssi), empty state, conectado rico.
- Test pulse + Forget; `disconnect()` de verdade.
- VM dedicado; reduced-motion respeitado.

**Non-Goals:**
- Bateria da pulseira; múltiplas pulseiras.
- Mudar a reconexão passiva no start do FGS.

## Decisions

### 1. `WristbandViewModel` deriva um UiState de tela
`WristbandUiState` = modo + dados:
```
sealed: Rest | Scanning(devices) | Empty | Connecting(name) | Connected(name, intensity) | Failed(reason)
```
Derivado de `link.state` + `link.scanResults` + um sinal local de timeout:
- `Connected` → Connected (com intensidade)
- `Connecting` → Connecting
- `Failed` → Failed(reason)
- `Scanning` + results≠∅ → Scanning(devices)
- `Scanning` + ∅ + dentro do tempo → Scanning(vazio, radar girando)
- timeout(12s) + ∅ → Empty
- `Idle/Disconnected` → Rest

### 2. Timeout do scan (12s)
`onScan()`: marca scanning, `startScan()`, e agenda `delay(12_000)`; se `scanResults` seguir vazio, `stopScan()` e marca timeout → Empty. Achou algo antes → some o timeout. Retry re-scaneia.

### 3. Radar como assinatura (reduced-motion aware)
Anéis concêntricos expandindo (alpha decrescente) via `rememberInfiniteTransition`, âmbar sobre surface, com um ícone de pulseira no centro. `rememberReducedMotion()` → anéis estáticos + "Searching…". Uma assinatura só; resto quieto.

### 4. Sinal pelo rssi
`signalBars(rssi)`: ≥ -60 → 4, ≥ -70 → 3, ≥ -80 → 2, senão 1. Cada card de device mostra as barras.

### 5. `disconnect()` na camada BLE
Adicionar `disconnect()` ao `WristbandLink`; `WristbandRepository` fecha o gatt (`WristbandGattClient`) e emite `Disconnected/Idle`. `forget()` = `disconnect()` + `RememberedWristbandStore.clear()` (não reconecta passivamente depois).

### 6. Test pulse e intensidade
"Send test pulse" chama `AlertHaptics.pulse()` (injetado no VM). Intensidade reusa `WristbandPreferences` (get/set) como hoje.

### 7. Aposenta a `WristbandSettingsSection`
A tela nova absorve a seção; a `WristbandScreen` deixa de embrulhar a section antiga. `SettingsViewModel.onPairWristband/onSetIntensity/onPair` deixam de ser usados pela tela (o VM dedicado assume) — remover os que ficarem órfãos.

## Risks / Trade-offs

- **`disconnect()` na camada BLE** → toca `WristbandLink`/`Repository`/`GattClient`. Escopo controlado (fechar gatt + estado). Testar que não quebra a reconexão passiva.
- **Timeout vs scan bounded** → o scan já é limitado; o timeout de 12s é da UI pro empty state. Se o scan parar antes com ∅, também cai em Empty.
- **rssi→barras** é heurístico (ambiente varia) — só indicação visual, não decisão.
- **Radar custo** → animação só enquanto a tela de scan está aberta; reduced-motion desliga.
- **Estado Disconnected após forget** → garantir que a auto-reconexão do FGS não traga de volta (o clear do lembrado resolve).

## Migration Plan

1. BLE: `WristbandLink.disconnect()` + impl no `WristbandRepository` (fecha gatt) + `forget` (clear lembrado).
2. `WristbandViewModel` (state derivado + timeout + ações: scan/connect/disconnect/forget/testPulse/setIntensity).
3. `WristbandScreen` por estado: radar (Scanning), lista com sinal, Empty, Connecting, Connected (test pulse+forget+intensidade), Failed+retry.
4. Aposentar `WristbandSettingsSection`; limpar métodos órfãos do `SettingsViewModel`.

Rollback: reverter a tela/VM; `disconnect()` é aditivo. Sem migração de dados.

## Open Questions

- Ícone central do radar: pulseira (`Watch`) — ok? (Default: sim.)
- Auto-conectar se achar exatamente 1 device, ou sempre exigir toque? (Default: sempre toque, mais Watch-like e previsível.)
