## 1. AlarmPlanner (puro, test-first — D-2)

- [x] 1.1 Escrever `AlarmPlannerTest` — 12 min/limite 20 → +8 min; pausado/teto/não-DENTRO → null; segundo aviso usa o dobro
- [x] 1.2 Implementar `domain/AlarmPlanner.kt` — `nextFireDelayMs(state, config, alertsFired, pausedToday, todayAlertCount, now): Long?`; puro; comentário por método (G12)
- [x] 1.3 Rodar testes — §1 verde

## 2. Notificações próprias na leitura (D-3/D24)

- [x] 2.1 Estender `UsageStatsReader` — `suspend fun ownAlertTimestamps(from,to): List<Long>` (NOTIFICATION_INTERRUPTION do próprio package)
- [x] 2.2 Implementar no `UsageStatsReaderImpl`; helper puro pra contar quantas caem em `[episodeStartedAt, now]`

## 3. Agendamento e disparo (alert-delivery, glue)

- [x] 3.1 Criar `data/alarm/AlarmScheduler.kt` (interface) — `fun scheduleAt(elapsedRealtimeMs: Long)` + `fun cancel()`
- [x] 3.2 Implementar `AlarmSchedulerImpl` — `setExactAndAllowWhileIdle(ELAPSED_REALTIME_WAKEUP, …)`, `PendingIntent` `FLAG_IMMUTABLE` pro `AlarmReceiver` (reusa o probe)
- [x] 3.3 Criar `service/AlarmReceiver.kt` — `@AndroidEntryPoint`; acordar-pra-conferir: replay 6 h → `EpisodeEngine` → conta avisos → `AlertPolicy`; posta via `Notifier` ou reagenda (D22)

## 4. Notificação (Notifier, glue)

- [x] 4.1 Criar `data/notification/Notifier.kt` (interface) — `ensureChannels()`, `postAlert(appLabel, minutes, alertId)`, `ongoing(text): Notification`
- [x] 4.2 Implementar `NotifierImpl` — canal HIGH (aviso + 2 botões via `PendingIntent`→`OutcomeReceiver`, requestCode único) + canal LOW (fixa do FGS); frase à mão (D8)

## 5. Outcome (alert-outcome, test-first — D-6)

- [x] 5.1 Criar `data/outcome/AlertOutcomeEntity.kt` (`@Entity`) + `AlertOutcomeDao` (insert/updateResponse/observe)
- [x] 5.2 Subir `ResurfaceDatabase` pra versão 2 + `Migration(1,2)` que cria a tabela (additiva, D-6)
- [x] 5.3 Escrever `OutcomeRepositoryTest` — com fake DAO: registrar aviso cria linha; toque grava a resposta; sem toque fica "sem resposta"
- [x] 5.4 Implementar `data/outcome/OutcomeRepository.kt` — `recordFired(...)`, `recordResponse(alertId, response)`; `@IoDispatcher`
- [x] 5.5 Criar `service/OutcomeReceiver.kt` — `@AndroidEntryPoint`; lê a ação/extra e grava a resposta (F7)

## 6. Serviço e estado (foreground-monitor, glue)

- [x] 6.1 Criar `service/EpisodeStateHolder.kt` — `@Singleton` com `StateFlow<EpisodeState>` (Q-A opção 1)
- [x] 6.2 Criar `service/MonitorService.kt` — FGS `specialUse`; tick de ~45 s (Reader→Engine→arquiva→holder→`reagendarAlarme`); cold-start faz replay 6 h; comentário por método (G12)
- [x] 6.3 Criar `service/BootReceiver.kt` — `@AndroidEntryPoint`; religa o `MonitorService` no `BOOT_COMPLETED`/`MY_PACKAGE_REPLACED` (D24/G3)
- [x] 6.4 Iniciar o `MonitorService` da `MainActivity.onCreate` (contexto de foreground, D-7)

## 7. Manifesto, wiring e fechamento

- [x] 7.1 Manifesto — `<service specialUse>` + `<property PROPERTY_SPECIAL_USE_FGS_SUBTYPE>` + `AlarmReceiver`/`OutcomeReceiver`/`BootReceiver`
- [x] 7.2 Módulos Hilt — `@Binds` de `AlarmScheduler`/`Notifier`→impls; providers do `AlarmManager`/`NotificationManager` se preciso
- [x] 7.3 `./gradlew :app:testDebugUnitTest` verde (planner + outcome) e `assembleDebug` limpo
- [x] 7.4 Checklist §13 (G1 no planner, G5 interfaces, G9, G11, G12); nota: glue (FGS/alarme/notif) validado no aparelho, não em unit test
- [x] 7.5 Validar no aparelho (adb): FGS sobe, aviso dispara no limite, botões gravam outcome — usando o harness do probe (`GAPS.md`)
