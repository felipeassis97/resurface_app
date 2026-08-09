## Why

A tela de ajustes é um scroll único com tudo empilhado (nome, limite, tom, hobbies, janela, pulseira, dev). Fica longa e sem hierarquia. Um **hub** com uma lista de linhas, cada uma abrindo sua tela focada, é mais fácil de escanear e de crescer. E dá um lugar próprio pra o **Debug**, tirando as ferramentas de dev do meio dos ajustes reais.

## What Changes

- **Ajustes vira um hub**: uma lista de linhas (ícone + label + valor atual + chevron), cada uma navega pra sua tela. **BREAKING** (UI): o scroll único de ajustes é substituído por hub + sub-telas.
- **Sub-telas** (reusam o `SettingsViewModel`, sem dado novo):
  - **Profile** — nome + hobbies
  - **Reminders** — remind time (limite 10–60) + tone
  - **Schedule** — dias + janela de horário
  - **Wristband** — pareamento + intensidade (a seção atual vira tela)
  - **Debug** — só em build de debug
- **"Pause for today"** fica como ação rápida **no topo do hub** (F6, frequente).
- **Subtítulos com o valor atual** em cada linha (ex.: "20 min · Gentle", "Always on", "Not connected").
- **Cada sub-tela** ganha `TopAppBar` com voltar + título; o hub também tem barra com voltar pro dashboard.
- **Debug**: move o `DevToolsSection` (aviso de teste) pra a tela Debug e **adiciona um toggle "Always show onboarding"** + "Reset onboarding now", **substituindo o reset hardcoded** hoje no `AppViewModel` (paga dívida técnica; deixa de precisar recompilar pra ligar/desligar).

Fora de escopo: mudar qualquer regra de negócio (limite/tom/janela/pausa seguem iguais); traduções (já em inglês); novos dados.

## Capabilities

### New Capabilities

_(nenhuma — recai sobre capabilities existentes + navegação)_

### Modified Capabilities

- `app-settings`: passa a ser um hub com sub-telas (navegação, linhas com valor atual, back); pausar por hoje acessível no hub. Os requisitos de limite/pausa/nome/copy seguem, agora dentro das sub-telas.
- `dev-tools`: as ferramentas de dev passam a viver numa **tela Debug** (ainda gated por debug), e ganham um **toggle de "sempre mostrar onboarding"** que substitui o reset hardcoded.

## Impact

- **Navegação:** `ui/navigation` (novas rotas: Profile, Reminders, Schedule, Wristband, Debug), `ResurfaceNavHost` (registra + hub navega).
- **UI:** novo `SettingsHubScreen` (lista), sub-telas em `ui/screens/settings/*` (Profile, Reminders, Schedule, Wristband, Debug), cada uma com `TopAppBar`+back; `SettingsScreen` atual é desmembrado.
- **VM:** reusa `SettingsViewModel` (adiciona expor "always show onboarding" + reset, e um `DebugPreferences`).
- **Debug/onboarding:** `AppViewModel` lê um flag persistido (`DebugPreferences.alwaysShowOnboarding`) em vez do reset hardcoded; `dev/` ganha os controles.
- **Reusa:** `WristbandSettingsSection`, `DevToolsSection` (movidos pra telas), `OnboardingRepository.resetForTesting`.
- **Não muda:** repos de config/perfil, serviço, permissões, dado.
