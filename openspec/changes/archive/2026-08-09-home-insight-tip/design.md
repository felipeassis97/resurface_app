## Context

A home (`DashboardScreen`) mostra números + um card de avisos no rodapé. Queremos um card de tip no topo: uma observação pessoal em 2 linhas. Já existe: `InsightsAggregator` (pico de hora, dia, tendência, cruza-apps, vídeos), `GeminiClient.generate(prompt, maxTokens)` genérico, `ProfileRepository` (tom), `MessageGuard` (guarda P2/P5, hoje com padrões em PT + limite de tamanho).

## Goals / Non-Goals

**Goals:**
- Tip pessoal no topo, 2 linhas, no tom escolhido.
- Fato medido localmente; IA só reescreve (nunca inventa número).
- Funciona offline/sem chave (frase local).
- Alterna entre fatos a cada abertura; rede limitada.
- Remover o card de avisos da home.

**Non-Goals:**
- Tips por-app ou "Instagram X%" (precisa tempo por app por episódio → schema/migração).
- Trazer o H1/S2 de volta numa tela (dívida futura).
- i18n do guard (segue como está).

## Decisions

### 1. Fato local, frase pela IA
`InsightSelector` (puro) recebe as stats e devolve um `Insight` com um **fato canônico** (string curta, ex.: "peak start hour 14-15"). A frase local vem de `InsightTemplates(tone)`; a IA recebe o fato + tom e devolve 2 linhas. A IA nunca vê a série crua (P4) nem inventa número (P2).

### 2. Modelo e seleção
```
enum InsightType { PEAK_HOUR, PEAK_DAY, TREND, CROSS_APP, VIDEOS, WELCOME }
data class Insight(val type: InsightType, val fact: String, val params: ...)
```
`InsightSelector.select(stats, rotationIndex): Insight` monta a lista de **candidatos com dado** (pico de hora se max>0; dia se max>0; tendência se |pct|>=10; cruza-apps se >0; vídeos se a11y e >0), ordena por saliência, e escolhe `candidatos[rotationIndex % size]`. Sem candidatos → `WELCOME` (tip neutro).

### 3. Rotação por abertura + cache de IA por (fato, dia) — resolve a tensão
"Alternar a cada abertura" e "cache por dia" parecem brigar (rotacionar = fato novo = chamada nova). Resolução: **a rotação escolhe o fato (local, instantâneo) a cada abertura**, mas a **reescrita da IA é cacheada por `(dia, factId, tom)`**. Assim, num dia, há no máximo *uma* chamada por fato distinto (não uma por abertura); reabrir num fato já gerado hoje é cache hit.

### 4. Fluxo na abertura (não bloqueia)
`DashboardViewModel`: computa o fato → emite **já** a frase local. Em paralelo, se não houver cache de IA pra `(dia, fato, tom)`, chama `GeminiClient`; se voltar e passar no guard, grava no cache e **re-emite** o tip com a versão da IA. Rede off-main; abertura nunca espera.

### 5. Guard e wording honesto (P2)
Reusa `MessageGuard` (limite de tamanho; padrões P5). `hourBuckets` é somado pela **hora de início** do episódio, então a frase honesta é "you tend to start around 14h", não "you watch most at 14h". Os templates seguem isso. Limitação: o guard tem padrões em PT; texto EN violador não é pego por padrão. Aceito v1 (fato é neutro e o prompt restringe); estender o guard pra EN fica como melhoria.

### 6. Persistência
`InsightTipRepository` (DataStore): contador rotativo (int, incrementa 1x por launch) + cache `{dateKey, factId, tone, text}`. Em memória bastaria, mas DataStore mantém o cache entre processos (evita regenerar no mesmo dia após restart).

### 7. UI
Card de tip no topo do `LazyColumn`, abaixo do `Header`, acima do `LiveCard`/"Your activity". Remove `AlertsCard` e sua chamada. Estilo: card `surfaceContainer`/âmbar-fraco, ícone (lâmpada/estrela), texto 2 linhas.

## Risks / Trade-offs

- **Guard PT vs texto EN** → violação EN pode passar. Mitiga: fato neutro + prompt restrito (no shaming/no mental state/no dashes). Extensão do guard = futuro.
- **hourBuckets por hora de início** → wording deve refletir "início", não "assistir". Tratado nos templates.
- **Rotação vs cache** → resolvido (cache por fato/dia; rede ≤ nº de fatos/dia).
- **Home sem H1/S2** → perde a leitura da hipótese central; dado segue gravado; dívida registrada.
- **Novo usuário sem dado** → tip WELCOME neutro (sem número).

## Migration Plan

1. Domain: `Insight`/`InsightType`, `InsightSelector` (+testes), `InsightTemplates` (+testes).
2. `InsightTipRepository` (contador + cache) e o path de geração (prompt + `GeminiClient` + guard).
3. `DashboardViewModel`: expõe o tip (local imediato + swap async).
4. `DashboardScreen`: card no topo; remove `AlertsCard`.
5. Conferir strings EN sem travessões.

Rollback: reverter UI + remover domain novo; sem migração de dados.

## Open Questions

- Ícone do card de tip: lâmpada (`Lightbulb`) ou estrela? (Default: lâmpada.)
- Fatos "fortes" default e ordem de saliência: peak hour > trend > peak day > cross-app > videos. (Default essa.)
