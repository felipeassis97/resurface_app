## Context

Três telas hoje: `HomeScreen` (contador vivo, `HomeViewModel`), `InsightsScreen` (dashboard, `InsightsViewModel` + `InsightsAggregator`), `SettingsScreen`. Navegação por `NavigationSuiteScaffold` (bottom nav) em `MainShell`, rotas em `ResurfaceNavHost`/`Destination` (Home/Insights/Settings). O `InsightsAggregator` já entrega quase tudo (semana+tendência, por dia, **por hora**, cruza-apps, comportamento, avisos+S2) — mas `InsightsScreen` **não renderiza o heatmap por hora**. Direção visual (âmbar/Bricolage/Hanken/Geist) já aplicada no tema.

## Goals / Non-Goals

**Goals:**
- Uma tela inicial = contador vivo + observações, hero adaptativo.
- Remover a bottom nav; ajustes via ícone na top bar.
- Acender o heatmap por hora (dado já calculado).
- Copy da tela em inglês.
- Reusar 100% das fontes de dados; zero mudança no serviço/permissões.

**Non-Goals:**
- Bloco H1 (efeito do aviso vs controle) — dado novo, change própria.
- Faixas dia/mês — aggregator é semana-fixo.
- i18n do resto do app (onboarding/ajustes/notificação seguem PT).
- Mudar `SettingsScreen` (só passa a ser alcançada pelo ícone).

## Decisions

### 1. `MainShell`: `Scaffold` + `TopAppBar`, sem bottom nav
Trocar `NavigationSuiteScaffold` por `Scaffold` com `TopAppBar` (título "Resurface" + `IconButton` de ajustes como action). O conteúdo é o `DashboardScreen`. O ícone navega pra rota de settings.

_Alternativa descartada:_ manter `NavigationSuiteScaffold` com 1 item — continua sendo bottom nav, contraria o pedido.

### 2. Navegação: dashboard start, settings secundária
`Destination` deixa de existir como enum de abas. `ResurfaceNavHost`: `startDestination = DashboardRoute`, `composable<DashboardRoute>` e `composable<SettingsRoute>`. O ícone da top bar faz `navController.navigate(SettingsRoute)`; predictive back volta. Remover `HomeRoute`/`InsightsRoute`.

### 3. Tela: `ui/screens/dashboard/DashboardScreen` + `DashboardViewModel`
Um `DashboardViewModel` combina o estado ao vivo (do `EpisodeStateHolder` + `ConfigRepository.pausedToday` + ticker, como o `HomeViewModel`) com o `InsightsUiState` (como o `InsightsViewModel`). Um único `DashboardUiState { live: HomeUiState, insights: InsightsUiState }`. `HomeScreen`/`InsightsScreen` e seus VMs são absorvidos/removidos; a lógica pura (`toHomeUiState`, `InsightsAggregator`) é reaproveitada intacta.

_Alternativa descartada:_ dois VMs lado a lado na mesma tela — funciona, mas um VM só simplifica o hero adaptativo (decide live vs week num lugar).

### 4. Hero adaptativo
`if (live.active) → contador vivo (Geist Mono grande + app)` senão `→ resumo da semana como hero`. Uma decisão, no topo do `LazyColumn`.

### 5. Heatmap por hora (a assinatura)
Renderizar `insights.hourBuckets` (24 valores) como faixa de intensidade âmbar (0 → superfície; máx → âmbar cheio), com marcas de hora. É o gráfico distintivo — responde "quando eu rolo". Cuidado com craft: rampa de cor por `lerp`, hora de pico destacada.

### 6. Copy em inglês
Strings do dashboard/top bar em inglês. No `InsightsAggregator`, `dayLabel` → "Mon…Sun" e `responseLabel` → "right time"/"not now"/"no response". App names (Instagram/TikTok) são nomes próprios (`AppLabels`), inalterados.

## Risks / Trade-offs

- **Remover Home/Insights quebra referências** (`Destination`, nav host, previews, testes) → atualizar tudo num passo; `computeStartRoute`/onboarding não dependem das rotas internas do MainShell, então o gate fica intacto.
- **Aggregator em inglês quebra `InsightsAggregatorTest`** → atualizar asserts de rótulo. Baixo risco (só strings).
- **Dashboard rico vs P6** → mitigado pelo hero adaptativo (ocioso = calmo, foco na semana; sem números de alarme) e empty states sem culpa.
- **Heatmap com pouco dado** (primeira semana) → estado quase vazio; garantir que não pareça quebrado (barras baixas + rótulo).
- **Inconsistência PT/EN temporária** (dashboard EN, resto PT) → aceita e documentada; i18n completo é follow-up.

## Migration Plan

1. Criar `DashboardRoute`; ajustar `ResurfaceNavHost` (start=dashboard, +settings) e remover `Destination`/rotas de abas.
2. `MainShell` → `Scaffold` + `TopAppBar` com ícone de ajustes.
3. `DashboardViewModel` + `DashboardScreen` fundindo live + insights; renderizar heatmap.
4. Inglês nas strings da tela + `InsightsAggregator` (dayLabel/responseLabel) + testes.
5. Remover `screens/home` e `screens/insights` (código morto) após o dashboard cobrir.

Rollback: reverter `MainShell`/nav/telas; dados e serviço intactos.

## Open Questions

- Ícone de ajustes: engrenagem simples (`Icons.Filled.Settings`) — ok? (Default: sim.)
- Ordem das seções abaixo do hero — semana → dia → hora → cruza-apps/comportamento → avisos. (Default: essa.)
- Mostrar o contador vivo mesmo com a notificação fixa já exibindo? (Default: sim, é o hero quando ativo.)
