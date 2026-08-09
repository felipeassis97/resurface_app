## Why

O loop invisível já roda no aparelho (contar, arquivar, avisar, registrar outcome). Mas o app
não tem **rosto**: as telas são placeholders. Pro F1 fechar e as 4 semanas de teste do H1
começarem, falta a camada de UI que mostra o contador vivo, deixa pausar/ajustar, e exibe os
avisos e respostas (a métrica S2). Toda a lógica e os dados já existem — isto é só a apresentação
(MVVM, ViewModel consumindo os repositórios/holder).

## What Changes

- `HomeViewModel` + Home real: consome o `EpisodeStateHolder` (Q-A, opção 1) + um ticker de 1 s
  → mostra o **contador vivo** (minutos acumulados, app em foco, se está pausado hoje).
- `SettingsViewModel` + Settings real (F6): trocar o **limite** (10–60) e **pausar por hoje** (D11),
  gravando no `ConfigRepository`.
- `InsightsViewModel` + Insights real (F5, mínimo): lista dos episódios recentes (`EpisodeRepository`)
  e dos avisos com a resposta (`OutcomeRepository`) — a visão que mede o H1/S2.
- ViewModels seguem o padrão do `ENGENHARIA` §6: `StateFlow<UiState>` imutável via
  `combine(...).stateIn(WhileSubscribed)`; telas stateless com `collectAsStateWithLifecycle`.
- Testes dos três ViewModels (Turbine + fakes) — G11. Telas (composables) não testadas (UI visual).

## Capabilities

### New Capabilities
- `home-counter`: a tela inicial mostra o contador vivo de tempo de vídeo curto (minutos, app em
  foco, estado de pausa), atualizado continuamente enquanto DENTRO.
- `usage-insights`: a tela de observações lista os episódios recentes e os avisos com sua resposta
  (era hora / agora não / sem resposta) — a leitura do H1 e da S2.
- `app-settings`: a tela de ajustes deixa trocar o limite de minutos e pausar por hoje.

### Modified Capabilities
<!-- Nenhuma — consome os repositórios/holder existentes sem alterar comportamento. -->

## Impact

- **Código novo:** `ui/screens/home/HomeViewModel`, `ui/screens/settings/SettingsViewModel`,
  `ui/screens/insights/InsightsViewModel`, e o conteúdo real das três telas + um `ui/TickerFlow`.
  Testes dos ViewModels em `src/test/`.
- **Consome:** `EpisodeStateHolder`, `EpisodeRepository`, `OutcomeRepository`, `ConfigRepository`.
- **Sem novas dependências** (Compose/Hilt/Turbine já no build). Sem tocar em serviço/domínio/dados.
- **Fecha o F1:** com a UI, o app é usável no dia a dia e as métricas S1–S5 (`NEGOCIO.md`) começam
  a acumular. Onboarding real e mensagem no tom (F2) continuam adiados.
- **Tema/UI:** respeita G10 (tokens do tema), tipografia tabular pro número que anda (`ResurfaceTextStyles`).
