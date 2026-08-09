## Why

Hoje o app tem três destinos numa bottom bar (Home, Insights, Ajustes), mas Home é só um contador e Insights é o dashboard — separá-los força troca de aba pra ver algo simples. Como ferramenta pessoal aberta poucas vezes, o valor é abrir e **ver tudo de uma vez**: o estado agora + o padrão da semana. A maior parte dos dados já existe e está pronta no `InsightsAggregator`; um deles (distribuição por hora) já é calculado e nunca exibido.

## What Changes

- **Fundir Home + Insights numa única tela inicial** (dashboard), com **hero adaptativo**: episódio ativo → contador vivo; ocioso → resumo da semana.
- **Remover a bottom navigation bar** (o `NavigationSuiteScaffold` de 3 destinos). **BREAKING** (navegação): não há mais abas.
- **Top bar com ícone de ajustes** que navega pra tela de config existente (push; voltar retorna). Ajustes deixa de ser aba e vira tela secundária.
- **Exibir o heatmap por hora** (o `hourBuckets` já calculado, hoje descartado) — a assinatura visual da tela.
- Compor no dashboard as seções já existentes: resumo da semana + tendência, por dia, por hora, cruza-apps (D2), comportamento (se a11y), avisos + S2.
- **Copy da tela em inglês** — todo o texto do dashboard e da top bar em inglês, incluindo as strings de display servidas pelo aggregator (rótulos de dia, rótulos de resposta). Onboarding, Ajustes e os botões da notificação seguem em português (i18n completo é follow-up).
- Aplicar a direção visual (âmbar + Bricolage/Hanken/Geist), gráficos com craft e empty states sem culpa (P5/P6).

Fora de escopo: o bloco H1 (efeito do aviso: "saiu em <2min" vs controle) — é dado novo, merece change própria. Faixas dia/mês (o aggregator é fixo em semana).

## Capabilities

### New Capabilities

- `dashboard`: a tela inicial única — hero adaptativo (contador vivo + semana), composição das seções de dados, top bar com acesso aos ajustes, sem bottom nav, copy em inglês.

### Modified Capabilities

_(nenhuma — `home-counter` e `usage-insights` mantêm seus requisitos de comportamento/dados; passam a ser compostos dentro do `dashboard` em vez de telas separadas)_

## Impact

- **UI**: `ui/MainShell.kt` (troca `NavigationSuiteScaffold` por `Scaffold` + `TopAppBar` com ação de ajustes), `ui/navigation/{Destination,ResurfaceNavHost}.kt` (remover abas; dashboard como start, settings como rota secundária), nova `ui/screens/dashboard/*` fundindo Home + Insights, remover/absorver `screens/home` e `screens/insights`.
- **Domain**: `InsightsAggregator` — `dayLabel`/`responseLabel` passam a inglês (strings de display).
- **Dados**: nenhuma mudança — reusa `EpisodeRepository`, `OutcomeRepository`, `BehaviorRepository`, `EpisodeStateHolder`, `ConfigRepository`.
- **Testes**: `InsightsAggregatorTest` (rótulos em inglês), previews do dashboard.
- **Não muda**: `SettingsScreen` (segue igual, só passa a ser alcançada pelo ícone), permissões, serviço, tema.
