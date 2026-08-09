# episode-storage Specification

## Purpose
TBD - created by archiving change usage-data-layer. Update Purpose after archive.
## Requirements
### Requirement: Arquivar episódio fechado
O sistema SHALL persistir cada `ClosedEpisode` no dispositivo (Room), guardando início, fim,
acumulado e apps envolvidos. A escrita SHALL ser `suspend` e fora da main thread.

#### Scenario: Episódio fechado vira linha
- **WHEN** o motor emite um `ClosedEpisode` e ele é arquivado
- **THEN** o episódio passa a existir no armazenamento com os mesmos início, fim, acumulado e apps

### Requirement: Expor o histórico como fluxo observável
O sistema SHALL expor os episódios arquivados como um `Flow`, do mais recente pro mais antigo,
re-emitindo quando um novo episódio é arquivado.

#### Scenario: Novo episódio re-emite
- **WHEN** um coletor observa o histórico e um novo episódio é arquivado
- **THEN** o coletor recebe a lista atualizada com o novo episódio no topo

### Requirement: Arquivamento idempotente
Arquivar o mesmo episódio fechado mais de uma vez NÃO SHALL criar linhas duplicadas — o
episódio é identificado unicamente pelo seu início (`startedAt`). Isto é necessário porque o
serviço reconstrói o estado por replay a cada tick (D24) e re-emite os fechados da janela.

#### Scenario: Arquivar duas vezes mantém uma linha
- **WHEN** o mesmo `ClosedEpisode` é arquivado duas vezes
- **THEN** o histórico contém apenas uma linha para aquele episódio

### Requirement: Retenção ilimitada por arquitetura
O armazenamento SHALL reter os episódios indefinidamente, independentemente da janela curta do
`UsageStatsManager` — o dado, uma vez fechado e arquivado, é permanente (D24).

#### Scenario: Sobrevive à janela da API
- **WHEN** um episódio foi arquivado há mais tempo do que a retenção de eventos do sistema
- **THEN** o episódio continua disponível no histórico do app

