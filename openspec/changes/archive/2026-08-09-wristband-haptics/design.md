## Context

O `../resurface_old` tem um módulo BLE maduro e testado (`com.resurface.app.ble`): payload
háptico puro, GATT client com mutex de 1 slot e close-antes-de-reconectar, scanner por
service UUID, readiness de ambiente, state machine pura com retry, repositório @Singleton
dono do link, e store do device lembrado. Mas o old só chama `send()` de um console manual —
não há acoplamento notificação→vibração nem intensidade persistida. Este change porta o
encanamento e adiciona o comportamento novo. O app novo tem um choke point único de aviso
(`Notifier.postAlert`) e um FGS (`MonitorService`) já rodando — os dois pontos de encaixe.

## Goals / Non-Goals

**Goals:**
- Vibrar em todo aviso postado (real + teste), fire-and-forget, no-op sem pulseira.
- Efeito `Gentle` com intensidade configurável e persistida (store dedicado).
- Auto-reconexão passiva ao device lembrado no start do FGS.
- Pareamento inicial mínimo: botão "Procurar e conectar" em Ajustes.

**Non-Goals:**
- UI rica de pareamento (lista de scan, RSSI, múltiplos devices) — depois.
- Escolha de efeito (Firm/Escalation) ou duração — fixo Gentle nesta versão.
- Vibração fora do caminho de aviso (ex.: feedback de UI).
- Status/bateria da pulseira (characteristic …0003 não existe no firmware v3).

## Decisions

**D-1 — Portar o módulo BLE quase verbatim.**
Copiar `HapticCommand`/`HapticEffect`, `WristbandGattClient`, `WristbandScanner`,
`BluetoothEnvironment`, `WristbandConnectionState`, `WristbandStateReducer`,
`WristbandRepository`, `BleUuids` para `com.resurface.resurface.ble`. Só muda: package,
`RememberedWristbandStore` passa a usar o `DataStore<Preferences>` "resurface" já provido, e
o `BleModule` referencia os tipos novos. Traz junto os testes puros (HapticCommand, Reducer).

**D-2 — Acoplamento via seam `AlertHaptics`, hook no NotifierImpl.**
Interface `AlertHaptics { fun pulse() }`. Impl lê a intensidade do `WristbandPreferences` e
chama `repository.send(HapticCommand(HapticEffect.Gentle, intensity))` num escopo próprio
(fire-and-forget). `NotifierImpl.postAlert` chama `haptics.pulse()` ao fim. Assim **todo**
aviso vibra (real e teste, que já passam por `postAlert`) sem o Notifier depender de BLE
direto e sem tocar os chamadores. `send()` já é no-op quando não conectado (canSend=false).

**D-3 — Intensidade em store dedicado.**
`WristbandPreferences` (novo, em `data/wristband/`): `intensity: Flow<Int?>` +
`setIntensity(Int?)`, sobre o mesmo DataStore. Chave própria (`wristband_intensity`). Null =
posição "auto" (omite o byte, firmware usa padrão). Separado de `ConfigRepository` porque BLE
é um domínio à parte (D-3 do usuário).

**D-4 — Auto-reconnect no start do FGS.**
`MonitorService.onStartCommand` (ou onCreate) chama `repository.reconnectRemembered()`. Usa
`autoConnect=true`: o OS reconecta quando a pulseira aparece, sem scan ativo. No-op se nada
lembrado ou sem permissão. O FGS já sobrevive ao Doze (D20), então o link acompanha.

**D-5 — Pareamento mínimo em Settings.**
Botão "Procurar e conectar": dispara `repository.startScan()` e conecta ao primeiro match
válido (app pessoal, uma pulseira). Estado do link exposto no UiState de Settings. Permissão
BLE pedida just-in-time pela Activity (a UI de scan/estado detalhada fica pra depois).

## Risks / Trade-offs

- **Permissão BLE precisa de Activity.** O pareamento (scan) roda a partir da tela de
  Ajustes, que tem Activity pra pedir `BLUETOOTH_SCAN`/`CONNECT`. Auto-reconnect no FGS usa
  só `CONNECT` já concedido; se faltar, no-op silencioso (readiness guarda).
- **Escrita BLE no Doze.** O app já tem FGS specialUse + isenção de bateria; a escrita parte
  de um link ativo e o autoConnect é gerido pelo OS. Sem novo risco de plataforma.
- **Vibração atrasar a notificação.** Mitigado: `pulse()` é fire-and-forget num escopo
  próprio; `postAlert` não espera o resultado do write.
- **Pulseira ausente/fora de alcance.** `send()` devolve NotConnected sem exceção; a
  notificação na tela nunca é afetada.
- **Divergência com o old.** O port é quase verbatim; os testes puros portados (HapticCommand,
  Reducer) travam o comportamento contra regressão na cópia.
- **Firmware 1-slot.** Um aviso raramente coincide com outro; se coincidir, o write mais novo
  sobrescreve o pendente (comportamento aceito do firmware, documentado no old).
