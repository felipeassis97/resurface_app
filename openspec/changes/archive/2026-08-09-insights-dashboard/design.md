## Context

Última grande peça de valor pro mestrado: a visão de evolução. Consome as 3 fontes já
persistidas (episódios `Room`, outcomes `Room`, comportamento `Room`) e apresenta. Segue o
`ENGENHARIA` §6 (ViewModel→UiState `StateFlow`, tela stateless) e §7 (tokens). Nada de novo em
serviço/dados — só leitura + agregação + Compose.

## Goals / Non-Goals

**Goals:**
- `InsightsAggregator` **puro** que deriva todos os números (semana, por dia, por hora, tendência,
  vídeos, hesitação, cruza-apps) — testado com relógio+zona injetados.
- Dashboard limpo: cartão da semana, barras por dia, faixa por hora, comportamento, avisos+S2.

**Non-Goals:**
- Reels vs feed % (o comportamento só conta vídeos de Reels; presença em feed é F5+).
- Comparações mês a mês, export (export é decisão separada, não escolhida). Gráficos elaborados/
  biblioteca de charts — barras simples com Compose bastam no F1-finish.

## Decisions

### D-1: `InsightsAggregator` puro em `domain/` (testável, G11)
`fun aggregate(episodes, outcomes, behavior, nowMillis, zone): InsightsUiState`. Toda janela
(semana atual, semana anterior, últimos 7 dias, faixas de hora) é calculada com `java.time` +
zona injetada. Sem I/O, sem Android — testado com listas e um `now` fixo.
- **Por quê:** as derivações são a parte arriscada (fronteiras de semana/dia/hora); ficam cobertas.
- **Alternativa recusada:** agregar por SQL no DAO — menos testável em JVM, espalha a lógica.

### D-2: Comportamento agregado por tempo, não por episódio
Vídeos/hesitação da semana = contar eventos de `behavior_event` cuja `timestamp` cai na semana.
Não precisa ligar cada vídeo a um episódio (desacoplado, `accessibility-capture` D-2). "% hesitação"
= hesitações / vídeos da janela.

### D-3: Cruza-apps derivado dos apps do episódio
Um episódio cruza se `apps` contém os dois pacotes. O aggregator conta esses na semana (D2).

### D-4: ViewModel combina 3 fontes; relógio injetado
`InsightsViewModel`: `combine(episodes.history, outcomes.outcomes, behavior.events)` + `TimeProvider`
→ `aggregate(...)` → `StateFlow` via `stateIn(WhileSubscribed)`. Sem `now` escondido.

### D-5: Dashboard em Compose com tokens, barras simples
`InsightsScreen` = `Screen(vm)`/`Content(state)`. Barras por dia = `Row` de `Box` com altura
proporcional (sem lib de chart). Números com `ResurfaceTextStyles.statBody/statDisplay` (tabular).
Cores/spacing por token (G10). Reduce-motion friendly (sem animação essencial).

## Risks / Trade-offs

- **[Fronteiras de tempo]** semana/dia/hora dependem de fuso e horário de verão. Mitigação: `java.time`
  + zona injetada + testes de fronteira (virada de semana/dia).
- **[Volume]** com meses de dado, as listas dos `Flow` crescem. No F1-finish (semanas, ~1 KB/dia) é
  trivial; se incomodar, migrar as contagens pra query no DAO depois.
- **[Sem comportamento]** a11y desligada → seção de vídeos vazia. Mitigação: o aggregator devolve
  null/0 e a tela omite a seção (D15).

## Migration Plan

Não aplicável — só leitura/UI, sem schema.

## Open Questions

- Semana começa domingo ou segunda? Proposta: segunda (padrão ISO com `java.time.temporal.WeekFields`
  ou `DayOfWeek.MONDAY`), decidir na task; não bloqueia.
- Faixa de hora: 24 baldes ou agrupado (madrugada/manhã/tarde/noite)? Proposta: 24 baldes crus, a UI
  destaca o pico; simples e fiel.
