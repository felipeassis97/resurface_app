## Context

`SettingsScreen` hoje é um `Column` com scroll único: nome, limite, tom, hobbies, janela (dias+horário), pulseira (`WristbandSettingsSection`) e dev (`DevToolsSection`, gated). Tudo servido pelo `SettingsViewModel` (fonte = repos). A navegação (`ResurfaceNavHost`) tem `DashboardRoute` + `SettingsRoute`; o gear do dashboard abre `SettingsRoute`. O reset de onboarding em debug está **hardcoded** no `AppViewModel.init` (`BuildConfig.DEBUG → onboarding.resetForTesting()`).

## Goals / Non-Goals

**Goals:**
- Ajustes como hub (lista) + sub-telas focadas, com voltar.
- Linhas com valor atual.
- Debug numa tela própria (gated), com toggle que substitui o reset hardcoded.
- Reusar o `SettingsViewModel` e os repos (zero dado novo, zero regra nova).

**Non-Goals:**
- Mudar limite/tom/janela/pausa (comportamento igual).
- Traduções (já em inglês).
- Nested nav graph / navegação aninhada (rotas planas bastam).

## Decisions

### 1. Rotas planas no `ResurfaceNavHost`
Adicionar `ProfileRoute`, `RemindersRoute`, `ScheduleRoute`, `WristbandRoute`, `DebugRoute`. `SettingsRoute` = hub. O hub recebe callbacks de navegação; cada sub-tela recebe `onBack = navController::navigateUp`. Back stack natural (sub → hub → dashboard).

_Alternativa descartada:_ nested NavHost dentro de settings — mais complexo sem ganho aqui.

### 2. Hub monta as linhas do `SettingsUiState`
`SettingsHubScreen` lista linhas (ícone + label + subtítulo + chevron). Subtítulos por formatadores: Profile = nome (+ nº hobbies); Reminders = "20 min · Gentle"; Schedule = "Always on" ou "Mon,Tue · 23:00–01:00"; Wristband = estado do link. "Pause for today" = ação rápida no topo (reusa `onPauseToday`). Linha Debug só se `BuildConfig.DEBUG`.

### 3. Sub-telas reusam o `SettingsViewModel`
Cada sub-tela pega `viewModel: SettingsViewModel = hiltViewModel()` e usa só os campos/ações que lhe cabem. Instâncias por nav-entry, todas lendo os repos (fonte da verdade) → consistentes. Cada sub-tela é `Scaffold` + `TopAppBar(back)`. Extrair os blocos atuais do `SettingsContent` pra `ProfileScreen`/`RemindersScreen`/`ScheduleScreen`; `WristbandScreen` embrulha a `WristbandSettingsSection`.

### 4. Debug: flag substitui o reset hardcoded
Novo `DebugPreferences` (DataStore): `alwaysShowOnboarding: Boolean` (get flow + set). `AppViewModel.init` troca o `if (BuildConfig.DEBUG) resetForTesting()` por `if (BuildConfig.DEBUG && debugPrefs.alwaysShowOnboarding.first()) resetForTesting()` (antes de computar a rota). A `DebugScreen` tem: o `DevToolsSection` (aviso de teste), o toggle `always show onboarding` e o botão `reset onboarding now` (`OnboardingRepository.resetForTesting`). Release: `BuildConfig.DEBUG` falso → nunca reseta.

### 5. `dev/` isolado, ponto de contato único
O único gancho de produção é a **linha/rota Debug gated** por `BuildConfig.DEBUG` no hub/nav. Apagar o pacote `dev/` + a rota some com tudo (mantém o requisito "isolamento removível").

## Risks / Trade-offs

- **Múltiplas instâncias de `SettingsViewModel`** (uma por sub-tela) → aceitável (repos são a fonte; custo de collectors pequeno). Se incomodar, escopar a um parent depois.
- **Ordem no `AppViewModel.init`** → ler o flag e (talvez) resetar ANTES de computar a rota, num único coroutine (como o hardcode faz hoje).
- **Drift do dashboard** (o redesign da home não está 100% nos specs) → fora do escopo desta change.
- **DataStore extra (DebugPreferences)** → chave nova, aditiva, default false; sem migração.

## Migration Plan

1. Rotas novas + `ResurfaceNavHost` (hub navega; sub-telas recebem `onBack`).
2. `SettingsHubScreen` (linhas + subtítulos + pause + Debug gated).
3. Sub-telas: Profile, Reminders, Schedule (extrair do `SettingsContent`), Wristband (embrulha a seção), cada uma com back bar.
4. `DebugPreferences` + `DebugScreen` (DevTools + toggle/reset) + `AppViewModel` lê o flag (remove hardcode).
5. Remover o `SettingsContent` monolítico antigo.

Rollback: reverter UI/nav; `DebugPreferences` é aditivo. Sem migração de dados.

## Open Questions

- Ícones das linhas (Profile/Reminders/Schedule/Wristband/Debug) — Material Icons. (Default: Person/Notifications/Schedule/Watch/BugReport.)
- Estilo: linhas de lista agrupadas vs cards. (Default: linhas de lista com divisórias sutis.)
