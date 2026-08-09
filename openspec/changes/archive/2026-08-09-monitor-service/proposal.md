## Why

O `episode-core` (lógica) e o `usage-data-layer` (leitura + persistência) existem, mas nada os
**roda**. Falta o serviço que mantém o contador vivo, alimenta o `EpisodeEngine` com o que o
`UsageStatsReader` lê, arquiva os episódios fechados, e — no minuto certo — dispara o aviso com
os dois botões. É o loop que fecha o F1 e torna o H1 testável. Todos os mecanismos de plataforma
já foram validados no aparelho pelo probe (`GAPS.md` G1/G3/G4/G5).

## What Changes

- Novo `service/MonitorService`: FGS tipo `specialUse` (D20) que mantém a notificação fixa,
  faz um tick leve (~45 s), lê a janela do `UsageStatsReader`, roda o `EpisodeEngine` pra manter
  o `EpisodeState`, arquiva `ClosedEpisode` no `EpisodeRepository`, e chama `reagendarAlarme` (D24).
- Novo `data/alarm/AlarmScheduler` (interface + impl): `setExactAndAllowWhileIdle` no instante do
  cruzamento (D22), + `cancel`. A conta de "quando disparar" é um **`AlarmPlanner` puro** (testável).
- Novo `service/AlarmReceiver`: no disparo, faz **acordar-pra-conferir** (D22) — relê o
  `UsageStatsReader`, recomputa via `EpisodeEngine`, checa a `AlertPolicy`, e **posta** ou reagenda.
- Novo `data/notification/Notifier` (interface + impl + canais): aviso canal `IMPORTANCE_HIGH` com
  os botões `[era hora]`/`[agora não]` (G5, validado), + notificação fixa `IMPORTANCE_LOW` do FGS.
- Novo `service/OutcomeReceiver` + `data/outcome/` (Room): registra a resposta subjetiva ao aviso
  (F7) — o instrumento que mede o H1 e a métrica S2.
- Novo `service/BootReceiver`: religa o FGS após reboot/atualização (D24/G3), sob as condições
  medidas (desbloqueio + isenção de bateria).
- Novo `service/EpisodeStateHolder` (@Singleton, `StateFlow<EpisodeState>`): fonte única do estado
  ao vivo, escrita pelo serviço e lida pela UI depois (fecha a Q-A com a opção do holder).
- Manifesto: declara o `<service>` specialUse + a `<property>`, e os três receivers.

## Capabilities

### New Capabilities
- `foreground-monitor`: o serviço em primeiro plano que mantém o contador vivo — tick, manutenção
  do estado do episódio a partir do `UsageStatsReader`, arquivamento dos fechados, e religamento
  após reboot.
- `alert-delivery`: agendar o alarme exato no instante do cruzamento, acordar-pra-conferir no
  disparo (relê e recomputa antes de postar), e postar o aviso heads-up com os dois botões.
- `alert-outcome`: registrar a resposta ao aviso (tocou "era hora"/"agora não" ou ignorou) para
  medir o H1.

### Modified Capabilities
<!-- Nenhuma — consome episode-tracking/alert-policy/usage-reading/episode-storage/config-store sem alterá-los. -->

## Impact

- **Código novo:** `service/**`, `data/alarm/**`, `data/notification/**`, `data/outcome/**`, um
  `AlarmPlanner` puro em `domain/`, módulos Hilt, e testes (do planner + do outcome repo).
- **Manifesto:** `<service android:foregroundServiceType="specialUse">` + `<property>` + receivers
  (`AlarmReceiver`, `OutcomeReceiver`, `BootReceiver`). Permissões já declaradas (FGS, boot, exact alarm).
- **Room:** nova tabela de outcome → bump do schema pra versão 2 (migração simples, additiva).
- **Consome:** `UsageStatsReader`, `EpisodeEngine`, `AlertPolicy`, `EpisodeRepository`, `ConfigRepository`.
- **Referência:** o probe validado em `scratchpad/probe_reference/` (as chamadas de FGS/alarme/canal).
- **Habilita** o `home-counter-ui`: a Home lê o `EpisodeStateHolder` e o histórico/outcomes.
