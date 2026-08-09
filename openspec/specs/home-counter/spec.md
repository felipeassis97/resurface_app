# home-counter Specification

## Purpose
TBD - created by archiving change home-counter-ui. Update Purpose after archive.
## Requirements
### Requirement: Contador vivo na tela inicial
A Home SHALL mostrar os minutos acumulados do episódio corrente, atualizando continuamente
(a cada ~1 s) enquanto o estado é DENTRO, a partir do `EpisodeStateHolder`.

#### Scenario: Minutos sobem enquanto dentro
- **WHEN** o estado é DENTRO com 12 min 30 s acumulados no instante da leitura
- **THEN** o UiState da Home reporta 12 minutos (arredondado pra baixo) e o app em foco

#### Scenario: Atualiza com o tempo passando
- **WHEN** o estado segue DENTRO e o relógio avança 1 minuto sem novo evento
- **THEN** os minutos exibidos aumentam em 1, sem depender de um novo tick do serviço

### Requirement: Estado de repouso quando fora
Quando o estado é FORA, a Home SHALL mostrar um estado calmo de repouso (sem contador correndo,
sem número de alarme), coerente com o P6 (silêncio é o padrão).

#### Scenario: Fora mostra repouso
- **WHEN** o estado é FORA (acumulado 0)
- **THEN** o UiState indica repouso (não-ativo), sem minutos correndo

### Requirement: Indicar pausa por hoje
A Home SHALL indicar quando "pausar por hoje" está ativo, para o usuário saber que não haverá avisos.

#### Scenario: Pausado é sinalizado
- **WHEN** "pausar por hoje" está ativo
- **THEN** o UiState marca `pausedToday = true`

