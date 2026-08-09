## Context

Junta as duas changes anteriores num loop rodando. Todo mecanismo de plataforma já foi validado
no aparelho (`GAPS.md`): alarme exato atravessa Doze em 12 ms (G1), FGS `specialUse` sem timeout
(G4), heads-up próprio sobre Reels (G5), religa no boot sob 2 condições (G3). O probe validado
está em `scratchpad/probe_reference/` como referência das chamadas. O desafio aqui não é
descobrir a plataforma — é manter o **serviço fino** e a **lógica testável**, com o grosso das
decisões já coberto por teste no `episode-core`.

## Goals / Non-Goals

**Goals:**
- `MonitorService` fino (FGS `specialUse`) que só bombeia dado: Reader → Engine → Repos → Scheduler.
- Disparo por alarme exato com **acordar-pra-conferir** (D22); a conta do "quando" num `AlarmPlanner` puro.
- Aviso heads-up HIGH com 2 botões; registro do outcome (F7).
- Religamento no boot; estado reconstruído por replay (D24), nada de estado de episódio persistido.

**Non-Goals:**
- UI/ViewModel (change `home-counter-ui`).
- Onboarding real (adiado; FGS inicia da MainActivity, permissões via adb no F1).
- Lado objetivo completo do F7 (correlação notificação→saída) além de contar avisos já disparados.
- Módulo de acessibilidade / contagem de vídeos (F5).

## Decisions

### D-1: Serviço fino + duas cadências (tick de manutenção × alarme de disparo)
O `MonitorService` roda um loop de corrotina (`delay(~45 s)`) enquanto vivo: lê a janela desde a
última leitura, alimenta o `EpisodeEngine`, arquiva fechados, atualiza o `EpisodeStateHolder`, e
chama `reagendarAlarme`. O **disparo** do aviso NÃO depende desse tick — é o `setExactAndAllowWhileIdle`
(D22). Sob freeze profundo o tick pode atrasar; o alarme carrega a pontualidade.
- **Alternativa recusada:** WorkManager pro tick (suspenso em Doze) e `Handler` puro (morre no freeze).

### D-2: `AlarmPlanner` puro em `domain/` (testável, G11)
`fun nextFireDelayMs(state, config, alertsFired, pausedToday, todayAlertCount, now): Long?` —
null se não-DENTRO, pausado, ou no teto; senão `max(0, thresholdMs − accumulatedAt(now))` onde
`thresholdMs = limite × 2^alertsFired`. O serviço converte esse delay pra `elapsedRealtime` do alarme.
- **Por quê:** a decisão de "quando" fica coberta por teste; o serviço só agenda.

### D-3: O `AlarmReceiver` reconstrói o estado sem confiar em memória
No disparo o app pode ter sido congelado/morto, então o receiver **relê 6 h** do `UsageStatsReader`,
recomputa o estado pelo `EpisodeEngine` (mesmo replay do cold-start), conta os avisos já disparados
e checa a `AlertPolicy`. Só então posta ou reagenda (D22).
- **avisosDoEpisódio contados da tabela `alert_outcome` (revisto no smoke de 2026-08-09):** a
  ideia original era derivar das `NOTIFICATION_INTERRUPTION` próprias no UsageStats, mas o smoke
  no aparelho mostrou que (a) a notificação **fixa do FGS** também gera `NOTIFICATION_INTERRUPTION`
  (inflava a contagem → teto falso) e (b) `UsageEvents.Event.getNotificationChannelId()` **não é
  API pública** (não dá pra filtrar por canal). Como nós **já persistimos** cada aviso na
  `alert_outcome` (F7), contamos de lá — autoritativo, limpo, e ainda robusto a morte de processo
  (Room). Não reintroduz estado frágil de episódio (D24 é sobre o episódio aberto, não sobre o
  registro permanente de avisos).

### D-4: `EpisodeStateHolder` @Singleton `StateFlow<EpisodeState>` (fecha a Q-A)
O serviço escreve o estado a cada tick; a UI lê depois (opção 1 da Q-A). O receiver **não** depende
dele (usa replay) — o holder é só pra UI ao vivo, tolerante a estar frio.

### D-5: Plataforma atrás de interface (G5), impl reusa o probe
`AlarmScheduler { scheduleAt(elapsedMs); cancel() }` e `Notifier { ensureChannels(); postAlert(...); ongoing(...) }`
são interfaces; os impls fazem as chamadas exatas já validadas no probe. `PendingIntent` com
`FLAG_IMMUTABLE`, requestCode único por ação, `USE_EXACT_ALARM`, tipo `ELAPSED_REALTIME_WAKEUP`.

### D-6: Outcome no Room v2 (migração additiva)
Nova tabela `alert_outcome(id, firedAt, appLabel, response?, respondedAt?)`. Postar o aviso cria a
linha (firedAt); o toque no botão atualiza `response`. Room sobe pra versão 2 com uma `Migration`
que só adiciona a tabela (sem tocar em `episode`).
- **Alternativa recusada:** `fallbackToDestructiveMigration` — perderia histórico.

### D-7: FGS inicia de contexto de foreground; religa no boot
Start da `MainActivity` (como o probe; no F1 sem onboarding) e do `BootReceiver` (allowlist de
bateria permite, G3). Nunca de `am broadcast`/receiver comum (barrado, medido). Service e receivers
são `@AndroidEntryPoint`.

## Risks / Trade-offs

- **[Tick sob freeze]** o loop de 45 s pode pausar em Doze profundo. Mitigação: o alarme exato faz o
  disparo (não depende do tick); o `PAUSED` atrasado é coberto pela releitura do D22.
- **[Contar avisos próprios]** depende das `NOTIFICATION_INTERRUPTION` próprias aparecerem no
  UsageStats — observado no aparelho (PRODUTO F7). Se faltarem numa janela, o dobro poderia repetir;
  mitigação: janela de replay generosa (6 h) + o teto diário (D5) limita o pior caso.
- **[Migração Room v2]** additiva e simples, mas precisa de teste de migração. Mitigação: `MigrationTestHelper`
  em androidTest, OU aceitar o risco no F1 (banco novo, sem dado a preservar ainda) e validar manualmente.
- **[Glue não unit-testado]** FGS/alarme/notificação. Mitigação: já validados no probe; aqui valida-se
  no aparelho de novo com o app real. O que é lógica (`AlarmPlanner`, outcome) tem teste.

## Migration Plan

Room 1 → 2: `Migration(1,2)` que executa `CREATE TABLE alert_outcome (...)`. Sem perda. No F1 o banco
ainda não tem dado real, então o risco é baixo; a migração fica escrita pra quando tiver.

## Open Questions

- **Intervalo do tick:** 45 s é o palpite (GAPS G2 diz que a fonte é fresca em ~1 s). Calibrável depois;
  não bloqueia.
- **Texto do aviso (F4):** no F1 é uma frase à mão (D8) montada no `Notifier` a partir de app + minutos.
  Sem geração no dispositivo (R16 fechado). Fica simples aqui.
