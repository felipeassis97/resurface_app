## Context

Primeiro código de domínio do Resurface. Vive em `domain/`, Kotlin **puro** (G1 do
`ENGENHARIA.md`), sem `import android.*`. É reusado em três lugares depois (tick ao vivo,
replay no cold-start D24, releitura do alarme D22) — por isso precisa ser determinístico e
sem estado escondido. As regras vêm de `PRODUTO.md` (D2/D3/D14/D4/D18/D5/D11) e a validação
de fonte de tempo já foi feita (`GAPS.md` G2: `queryEvents` fresco em ~1 s). Este change NÃO
toca em Android — a tradução de eventos crus para o domínio é da próxima change.

## Goals / Non-Goals

**Goals:**
- Modelos de domínio imutáveis e um `EpisodeEngine` determinístico que implementa a máquina de
  estados do episódio (FORA/DENTRO/PAUSADO).
- Uma `AlertPolicy` pura: `decide(acumulado, avisosJáDisparados, config, agora) → AlertDecision`.
- Cobertura de teste completa, incluindo o golden test do episódio de 18:43.
- Relógio injetado; zero `System.currentTimeMillis()` interno.

**Non-Goals:**
- Nenhuma leitura de `UsageStatsManager`, alarme, notificação, Room ou DataStore (próximas changes).
- Nenhuma UI. Nenhuma decisão sobre COMO o tick chama o motor (isso é do `MonitorService`).
- Contagem de vídeos / acessibilidade (F5, fora do F1).

## Decisions

### D-1: `EpisodeEngine` é um redutor com estado explícito, não um objeto que guarda estado por dentro
`fun reduce(state: EpisodeState, event: UsageEvent): EpisodeStep` onde `EpisodeStep` carrega o
novo `EpisodeState` e, opcionalmente, um `ClosedEpisode` emitido. O chamador guarda o estado.
- **Por quê:** determinismo e replay (D24) ficam triviais — `events.fold(initial, ::reduce)`.
  Testar é alimentar uma lista. Sem estado mutável interno = sem surpresa entre os 3 pontos de uso.
- **Alternativa recusada:** classe stateful com `var` interno — esconderia estado, dificultaria replay/teste.

### D-2: `AlertPolicy` é função pura, "próximo limite" é derivado (fecha a Q-B)
`fun decide(accumulatedMs, alertsFiredThisEpisode, config, nowMillis, todayAlertCount, pausedToday): AlertDecision`.
O limite atual = `config.limitMinutes * 2^alertsFiredThisEpisode`. Dispara se
`accumulated ≥ limiteAtual` E `!pausedToday` E `todayAlertCount < 6`.
- **Por quê:** D24 — nenhum estado de alarme em disco. `alertsFiredThisEpisode` é contado da
  fonte (as notificações registradas no UsageStats) na próxima camada; aqui é só parâmetro.
- **Alternativa recusada:** guardar "próximoLimite" persistido — reabre o estado frágil que o D24 elimina.

### D-3: Tipos selados para eventos e decisões
`sealed interface UsageEvent { Enter(pkg,ts) · Leave(pkg,ts) · ScreenOff(ts) }` e
`sealed interface AlertDecision { Fire(limitMinutes) · Hold }`. `EpisodeState` é `data class`
com `phase: Phase (FORA/DENTRO/PAUSADO)`, `accumulatedMs`, `currentApp`, marcos de tempo.
- **Por quê:** `when` exaustivo, imutabilidade (G6), casa com o vocabulário do `UsageEvents.Event`
  sem importar Android (a tradução é externa, via mapper na próxima change).

### D-4: Relógio injetado
O tempo entra como parâmetro (`nowMillis`) ou como `clock: () -> Long` no construtor do que
precisar. Nada de `System.currentTimeMillis()` dentro do domínio (G3/G9, §3 do `ENGENHARIA`).
- **Por quê:** teste determinístico, sem `sleep`, controla a janela de 5 min e a virada de meia-noite.

### D-5: Alvos e janela como configuração, não constante mágica
Os pacotes-alvo (`com.instagram.android`, `com.zhiliaoapp.musically`) e a janela de 5 min
entram via `Config`/parâmetro, com default. O domínio não hard-coda nome de package.
- **Por quê:** testável com pacotes fake; escopo do D19 (2 apps) sem prender o motor.

### D-6: Golden test a partir de `PRODUTO.md` §5.5
A linha do tempo do episódio de 18:43 vira uma fixture de `UsageEvent`s no teste. Espera-se um
único `ClosedEpisode` de 18 min 43 s (ou o `EpisodeState` no ponto do aviso). Fonte crua em
`app/docs/logs/`.
- **Por quê:** ancora a lógica em dado real medido, não em teoria.

## Risks / Trade-offs

- **[Ordem de eventos]** Eventos podem chegar fora de ordem ou com `Enter` sem `Leave` par →
  o redutor assume ordem por timestamp e trata `Enter` de outro pacote como fronteira implícita
  do anterior (padrão confirmado na doc oficial do UsageStats). Mitigação: teste explícito de
  `Enter`-sem-`Leave` e de troca direta IG→TikTok.
- **[Meia-noite no meio do episódio]** O teto diário (D5) zera à meia-noite, mas o episódio (e o
  dobro D18) não. Mitigação: `todayAlertCount` é responsabilidade da camada de dados por dia; a
  política só recebe o número. Teste do episódio cruzando meia-noite.
- **[Fixture vs realidade]** O golden é de uso de teste, não natural (`REELS.md` ressalva).
  Mitigação: golden valida a MECÂNICA (fusão, pausa, fechamento), não números de comportamento.

## Migration Plan

Não aplicável — código novo, sem runtime, sem migração de dado. Integra-se pela próxima change.

## Open Questions

- **Q-A (fora deste change):** como o `HomeViewModel` observa o `EpisodeState` ao vivo
  (holder singleton vs re-derivar). Decidido na change `home-counter-ui`.
- `MessageComposer` (texto do aviso) fica nesta change ou na de UI? Proposta: fora daqui — o F1
  usa uma frase à mão (D8), montada perto da notificação. Este change não gera texto.
