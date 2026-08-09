# foreground-monitor Specification

## Purpose
TBD - created by archiving change monitor-service. Update Purpose after archive.
## Requirements
### Requirement: Serviço em primeiro plano mantém o contador vivo
O sistema SHALL rodar um foreground service do tipo `specialUse` com uma notificação fixa de
baixa importância enquanto o app está ativo, para o processo sobreviver às restrições de
segundo plano e ao congelamento do One UI (D20, GAPS G4).

#### Scenario: Serviço sobe em primeiro plano
- **WHEN** o app inicia o `MonitorService`
- **THEN** o serviço entra em primeiro plano com a notificação fixa e o tipo `specialUse`, sem exceção

### Requirement: Tick mantém o estado do episódio a partir da leitura
O serviço SHALL, a cada tick, ler a janela de eventos desde a última leitura via
`UsageStatsReader` e alimentá-los ao `EpisodeEngine`, atualizando o estado corrente. Um mesmo
evento NÃO SHALL ser contado duas vezes entre ticks.

#### Scenario: Novo evento avança o estado
- **WHEN** o tick lê um `ACTIVITY_RESUMED` de um alvo desde a última janela
- **THEN** o estado do episódio passa a DENTRO e o acumulado começa a subir

#### Scenario: Sem sobreposição de janelas
- **WHEN** dois ticks consecutivos cobrem janelas adjacentes
- **THEN** nenhum evento é processado duas vezes (a janela seguinte começa onde a anterior terminou)

### Requirement: Arquivar episódios fechados
O serviço SHALL arquivar no `EpisodeRepository` cada `ClosedEpisode` que o motor emitir.

#### Scenario: Episódio fechado é persistido
- **WHEN** o motor fecha um episódio durante o tick (ou no fechamento por tempo)
- **THEN** o serviço o arquiva e ele passa a aparecer no histórico

### Requirement: Reconstruir o estado no cold-start e após reboot
Ao iniciar (cold-start, reboot ou atualização), o serviço SHALL reconstruir o estado do episódio
aberto por replay de uma janela recente (~6 h) do `UsageStatsReader` pelo mesmo `EpisodeEngine`,
sem persistir estado do episódio aberto (D24).

#### Scenario: Volta sozinho após reboot
- **WHEN** o aparelho reinicia, o usuário desbloqueia, e a isenção de bateria está concedida
- **THEN** o `BootReceiver` religa o serviço, que reconstrói o estado por replay, sem o app ser aberto na mão

