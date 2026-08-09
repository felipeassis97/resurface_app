## Why

O app é a ferramenta do mestrado: o autor usa redes normalmente e **acompanha a própria evolução
no app**. Hoje o Insights é só uma lista de episódios e avisos. O valor central — ver o **padrão
ao longo do tempo** (subiu/desceu, por dia, por hora, quantos vídeos, hesitação) — não existe
ainda. Este change transforma o Insights no **dashboard de evolução**, agregando as três fontes
já persistidas (episódios, avisos, comportamento) numa visão que responde "como estou indo?".

## What Changes

- `InsightsAggregator` **puro**: dado episódios + outcomes + eventos de comportamento + relógio,
  deriva os números do dashboard — todos testáveis (G11):
  - **Esta semana:** tempo total, nº de episódios, média por episódio, **tendência** vs semana anterior.
  - **Por dia:** barras dos últimos 7 dias (minutos/dia).
  - **Por hora:** distribuição do uso por faixa de hora (onde concentra).
  - **Comportamento:** nº de vídeos e % de hesitação na semana (da acessibilidade).
  - **Cruza-apps:** nº de episódios que atravessaram Instagram e TikTok (D2).
  - Mantém: lista de avisos com resposta + a razão **S2** (era hora %).
- `InsightsViewModel` expande: `combine(episodes, outcomes, behavior)` → UiState rico; relógio injetado.
- `InsightsScreen` vira dashboard (cartão da semana, barras por dia, faixa por hora, avisos), tokens
  do tema (G10), números tabulares (`statDisplay`/`statBody`).
- Testes do `InsightsAggregator` (puro) cobrindo cada derivação.

## Capabilities

### Modified Capabilities
- `usage-insights`: adiciona agregações de evolução (semana, por dia, por hora, tendência, vídeos,
  hesitação, cruza-apps) sobre o que hoje é só lista. Delta em `specs/usage-insights/spec.md`.

## Impact

- **Código:** novo `domain/InsightsAggregator` (puro) + expansão de `InsightsViewModel`/`InsightsScreen`;
  testes do aggregator. Reusa `EpisodeRepository`, `OutcomeRepository`, `BehaviorRepository`.
- **Sem novas dependências** (Compose/Hilt/Turbine já no build). Sem Room novo, sem migração.
- **Não toca** em serviço/alarme/contador — é só leitura + apresentação.
- **Fecha o valor do mestrado:** a tela que o autor abre pra acompanhar a própria evolução.
- Correlação comportamento↔episódio é **por tempo** (query/aggregate-time), como desenhado no
  `accessibility-capture` (desacoplado).
