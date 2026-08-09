## 1. Navegação: remover bottom nav

- [x] 1.1 Criar `DashboardRoute` (serializable) em `ui/navigation`
- [x] 1.2 `ResurfaceNavHost`: `startDestination = DashboardRoute`; registrar `DashboardRoute` + `SettingsRoute`; remover `HomeRoute`/`InsightsRoute`
- [x] 1.3 Remover o enum `Destination` (abas) e usos
- [x] 1.4 `MainShell`: trocar `NavigationSuiteScaffold` por nav host puro; a top bar vive no `DashboardScreen` (`Scaffold` + `TopAppBar` com `IconButton` de ajustes)

## 2. Dashboard: ViewModel unificado

- [x] 2.1 `DashboardUiState { live: LiveState, insights: InsightsUiState }`
- [x] 2.2 `DashboardViewModel` (Hilt): combina `EpisodeStateHolder` + `ConfigRepository.pausedToday` + ticker (live) com `InsightsAggregator` sobre episodes/outcomes/behavior (insights); reusa `toLiveState` e o aggregator puros
- [x] 2.3 Ícone da top bar navega pra `SettingsRoute`; back retorna (predictive back)

## 3. Dashboard: UI (hero adaptativo + seções)

- [x] 3.1 `DashboardScreen` + `DashboardContent` stateless (LazyColumn, insets, tema novo)
- [x] 3.2 Hero adaptativo: ativo → contador vivo (Geist Mono + app); ocioso → total da semana
- [x] 3.3 Seção semana (episódios/média/tendência) + cruza-apps (D2)
- [x] 3.4 Barras por dia (maior em âmbar, resto neutro)
- [x] 3.5 **Heatmap por hora** a partir de `hourBuckets` (rampa âmbar via lerp) — dado antes descartado
- [x] 3.6 Comportamento (vídeos + hesitação) só quando há acessibilidade
- [x] 3.7 Avisos + % "era hora" (S2); empty state calmo sem culpa
- [x] 3.8 Previews (ativo/ocioso)

## 4. Copy em inglês

- [x] 4.1 Strings do dashboard + top bar em inglês
- [x] 4.2 `InsightsAggregator`: `dayLabel` → Mon…Sun; `responseLabel` → right time/not now/no response
- [x] 4.3 `InsightsAggregatorTest` — verificado: não asserta rótulos (só números), segue verde

## 5. Limpeza

- [x] 5.1 Remover `ui/screens/home/*` e `ui/screens/insights/*` (absorvidos pelo dashboard)
- [x] 5.2 Conferir que nada mais referencia `Destination`/`HomeRoute`/`InsightsRoute` (grep limpo)

## 6. Verificação

- [x] 6.1 `./gradlew :app:compileDebugKotlin` e `:app:testDebugUnitTest` passam
- [ ] 6.2 Rodar no device: abre no dashboard, sem bottom bar; ícone → ajustes → back volta — **manual (device)**
- [ ] 6.3 Hero alterna ativo/ocioso; heatmap aparece; seções corretas em light/dark — **manual (device)**
