## 1. Domínio do insight (puro)

- [x] 1.1 `InsightType` (PEAK_HOUR, PEAK_DAY, TREND, CROSS_APP, VIDEOS, WELCOME) + `Insight(type, fact, params)`
- [x] 1.2 `InsightSelector.select(stats, rotationIndex)`: monta candidatos com dado, ordena por saliência (peak hour > trend > peak day > cross-app > videos), escolhe por `rotationIndex % size`; sem candidatos → WELCOME
- [x] 1.3 Testes do selector (candidatos, rotação, empty → WELCOME, wording de hora de início)
- [x] 1.4 `InsightTemplates(tone).phrase(insight)`: frase local por tipo + tom, 2 linhas, sem travessões, wording honesto ("start around 14h")
- [x] 1.5 Testes dos templates (todos os tipos × tons preenchem sem slot cru; sem travessão)

## 2. Geração + cache + rotação

- [x] 2.1 Prompt builder do tip (fato + tom, 2 linhas, no shaming/no mental state/no dashes, EN) reusando `GeminiClient.generate`
- [x] 2.2 Guard na saída (reusa `MessageGuard`: tamanho/P5); rejeição → mantém frase local
- [x] 2.3 `InsightTipRepository` (DataStore): contador rotativo (incrementa 1x/launch) + cache `{dateKey, factId, tone, text}`
- [x] 2.4 Só o fato-resumo + tom vão no prompt (nunca a série crua)

## 3. VM + UI

- [x] 3.1 `DashboardViewModel`: computa o fato → emite frase local já; se sem cache do dia, chama IA async e re-emite ao passar no guard
- [x] 3.2 `DashboardScreen`: card de tip no topo (abaixo do header), 2 linhas, ícone
- [x] 3.3 Remover `AlertsCard` e sua chamada do dashboard
- [x] 3.4 Strings EN sem travessões

## 4. Verificação

- [x] 4.1 `./gradlew :app:compileDebugKotlin` e `:app:testDebugUnitTest` passam
- [x] 4.2 Device: abre → tip no topo no tom; sem chave/rede mostra frase local; card de avisos sumiu — **manual**
- [ ] 4.3 Device: aberturas seguidas alternam o fato; reabrir no mesmo dia não refaz a rede pro mesmo fato — **manual**
