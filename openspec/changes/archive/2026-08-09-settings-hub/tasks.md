## 1. Navegação

- [x] 1.1 Rotas novas em `ui/navigation`: `ProfileRoute`, `RemindersRoute`, `ScheduleRoute`, `WristbandRoute`, `DebugRoute`
- [x] 1.2 `ResurfaceNavHost`: registrar as 5 sub-telas; hub navega; cada sub-tela recebe `onBack = navController::navigateUp`
- [x] 1.3 Linha/rota Debug gated por `BuildConfig.DEBUG`

## 2. Hub

- [x] 2.1 `SettingsHubScreen`: `Scaffold` + `TopAppBar(back → dashboard)`; lista de linhas (ícone + label + subtítulo + chevron)
- [x] 2.2 Subtítulos por formatadores a partir do `SettingsUiState` (Profile: nome; Reminders: "20 min · Gentle"; Schedule: "Always on"/janela; Wristband: estado)
- [x] 2.3 "Pause for today" como ação rápida no topo do hub (reusa `onPauseToday`)
- [x] 2.4 Linha Debug só em `BuildConfig.DEBUG`

## 3. Sub-telas (reusam `SettingsViewModel`, com back bar)

- [x] 3.1 `ProfileScreen` — nome (TextField) + hobbies (chips)
- [x] 3.2 `RemindersScreen` — limite (slider 10–60) + tom (chips)
- [x] 3.3 `ScheduleScreen` — dias (chips) + janela (sliders), com o texto de "always on"
- [x] 3.4 `WristbandScreen` — embrulha `WristbandSettingsSection`

## 4. Debug (paga a dívida do reset hardcoded)

- [x] 4.1 `DebugPreferences` (DataStore): `alwaysShowOnboarding` (flow + set)
- [x] 4.2 `DebugScreen`: `DevToolsSection` (aviso de teste) + toggle "always show onboarding" + botão "reset onboarding now" (`OnboardingRepository.resetForTesting`)
- [x] 4.3 `AppViewModel.init`: ler o flag e resetar só se `BuildConfig.DEBUG && alwaysShowOnboarding` (remove o reset hardcoded), antes de computar a rota

## 5. Limpeza

- [x] 5.1 Remover o `SettingsContent` monolítico antigo e o `DevToolsSection` inline dos ajustes
- [x] 5.2 Conferir que nada mais chama o settings antigo; strings EN sem travessões

## 6. Verificação

- [x] 6.1 `./gradlew :app:compileDebugKotlin` e `:app:testDebugUnitTest` passam
- [x] 6.2 Device: hub lista as linhas com valor; cada uma abre sua tela; back volta; pause no hub — **manual**
- [x] 6.3 Device: Debug só em debug; toggle "always show onboarding" liga/desliga sem recompilar — **manual**
