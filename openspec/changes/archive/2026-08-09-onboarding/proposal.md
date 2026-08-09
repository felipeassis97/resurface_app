## Why

O app funciona ponta a ponta, mas só instala com adb: as permissões (acesso ao uso, notificações,
bateria, acessibilidade) são concedidas na mão via `adb`. Pro autor usar no mestrado sem babá — e
sobreviver a reinstalações/limpezas de dado — falta o **onboarding**: telas que pedem cada permissão
pela UI, um gate de launch que decide onboarding vs app, e o passo manual do Samsung. É o último
degrau pra o Resurface ser um app de verdade, autossuficiente.

## What Changes

- Gate de launch (`AppViewModel` + `StartRoute`): na abertura resolve consentimento + status **ao
  vivo** das permissões obrigatórias e roteia pra Onboarding ou pro app (padrão do `resurface_old`).
  Re-avalia em todo resume (permissão trocada nas configs não tem callback).
- `OnboardingRepository` (DataStore): guarda o consentimento e "onboarding concluído".
- `OnboardingFlow` (Compose) com as telas (PRODUTO §5.1):
  1. **O que é** + consentimento (local, sem conta — P4).
  2. **Acesso ao uso** (obrigatória) → `ACTION_USAGE_ACCESS_SETTINGS`.
  3. **Notificações** (obrigatória, Android 13+) → diálogo runtime.
  4. **Bateria + Samsung** → isenção + instrução do passo "apps que nunca dormem" (D23).
  5. **Acessibilidade** (opcional, D15) → `ACTION_ACCESSIBILITY_SETTINGS` + nota de restricted settings.
- `MainActivity` passa a rotear pelo gate (`ResurfaceApp` = gate → Onboarding ou `MainShell`); o FGS
  sobe ao entrar no app (permissões concedidas).
- Testes do `AppViewModel` (roteamento) com fakes de `PermissionChecker`/`OnboardingRepository`.

## Capabilities

### New Capabilities
- `onboarding-gate`: o gate de launch que decide, a partir do consentimento e do status ao vivo das
  permissões obrigatórias, se mostra o onboarding ou o app — reavaliando a cada retorno.
- `permission-flow`: o fluxo de telas que explica e leva o usuário a conceder cada permissão
  (obrigatórias e a opcional), com o passo manual do Samsung.

### Modified Capabilities
<!-- Nenhuma — adiciona o gate/fluxo por cima; não altera comportamento existente. -->

## Impact

- **Código:** `ui/AppViewModel` + `StartRoute`, `data/onboarding/OnboardingRepository`,
  `ui/onboarding/OnboardingFlow` (+ telas), ajuste de `ResurfaceApp`/`MainActivity`. Testes do gate.
- **Reusa:** `PermissionChecker` (já cobre uso/notificações/acessibilidade), `ConfigRepository`,
  o padrão do `resurface_old`. Sem Room novo.
- **Fecha o app:** instalável e usável sem adb; sobrevive a reinstala/limpeza (re-onboarda).
- **Android 16:** acessibilidade em sideload precisa do passo de *restricted settings* — o fluxo
  explica; a obrigatória (uso + notificações) não precisa.
- **Não afeta** o loop de fundo — só decide o que mostrar na abertura.
