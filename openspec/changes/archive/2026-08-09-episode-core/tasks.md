## 1. Modelos de domínio (puro, G1)

- [x] 1.1 Criar `domain/model/UsageEvent.kt` — `sealed interface` com `Enter(pkg,ts)`, `Leave(pkg,ts)`, `ScreenOff(ts)`; comentário por tipo (G12)
- [x] 1.2 Criar `domain/model/EpisodeState.kt` — `data class` imutável com `phase` (enum FORA/DENTRO/PAUSADO), `accumulatedMs`, `currentApp`, marcos de tempo; `Companion.INITIAL`
- [x] 1.3 Criar `domain/model/ClosedEpisode.kt` — `data class` (startedAt, endedAt, accumulatedMs, apps)
- [x] 1.4 Criar `domain/model/AlertDecision.kt` — `sealed interface` `Fire(limitMinutes)` / `Hold`
- [x] 1.5 Criar `domain/model/Config.kt` — `data class` (limitMinutes=20, targetPackages, returnWindowMs=5min) com defaults
- [x] 1.6 Confirmar por grep que `domain/` não tem nenhum `import android.` (G1)

## 2. EpisodeEngine (test-first, D1/D3/D14/D24)

- [x] 2.1 Escrever `EpisodeEngineTest` — troca de app não zera (D2); cada cenário do spec `episode-tracking` vira um teste
- [x] 2.2 Escrever teste — pausar<5min retoma; ≥5min fecha e zera (D3)
- [x] 2.3 Escrever teste — tela apagada pausa (`ScreenOff`); `Enter` de outro alvo é fronteira implícita
- [x] 2.4 Escrever teste — fechamento emite exatamente um `ClosedEpisode` com acumulado final
- [x] 2.5 Escrever teste — replay determinístico (mesmo stream → mesmo estado, D24)
- [x] 2.6 Implementar `domain/EpisodeEngine.kt` — `reduce(state, event, now): EpisodeStep`; relógio via parâmetro `now` (D4/D-1); comentário por método (G12)
- [x] 2.7 Rodar `./gradlew :app:testDebugUnitTest` — todos os testes do §2 verdes

## 3. AlertPolicy (test-first, D4/D18/D5/D11)

- [x] 3.1 Escrever `AlertPolicyTest` — dispara ao cruzar o limite; abaixo não dispara (limite 10–60)
- [x] 3.2 Escrever teste — dobra por episódio (20→40); novo episódio volta ao base (D18)
- [x] 3.3 Escrever teste — função pura: mesma entrada → mesma decisão, sem efeito (D24)
- [x] 3.4 Escrever teste — teto de 6/dia; zera à meia-noite (D5)
- [x] 3.5 Escrever teste — "pausar por hoje" suprime, acumulado continua (D11)
- [x] 3.6 Implementar `domain/AlertPolicy.kt` — `decide(...)`; limite = `limitMinutes * 2^alertsFired` (D-2); comentário por método (G12)
- [x] 3.7 Rodar testes — §3 verdes

## 4. Golden test (dado real, D6)

- [x] 4.1 Criar `domain/EpisodeGoldenTest.kt` — fixture da linha do tempo de `PRODUTO.md` §5.5 como lista de `UsageEvent`
- [x] 4.2 Assert — um único episódio atravessando os dois apps (mecânica; ~18–22 min). Spec ajustado: o resumo §5.5 é ilustrativo, não reproduz 18:43 exato (design D6)
- [x] 4.3 Assert — a `AlertPolicy` decide de forma determinística sobre o acumulado fundido

## 5. Fechamento

- [x] 5.1 `./gradlew :app:testDebugUnitTest` inteiro verde (20 testes); `compileDebugKotlin` limpo
- [x] 5.2 Rodar o checklist §13 do `ENGENHARIA.md` sobre os arquivos novos (G1/G6/G11/G12) — ok
- [x] 5.3 Remover `ExampleUnitTest.kt` + `ExampleInstrumentedTest.kt` (cruft do scaffold)
