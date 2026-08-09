# app-settings Specification

## Purpose
TBD - created by archiving change home-counter-ui. Update Purpose after archive.
## Requirements
### Requirement: Trocar o limite de minutos
Os ajustes SHALL deixar o usuário ver e trocar o limite (10–60), gravando no `ConfigRepository`.
Um valor fora da faixa NÃO SHALL ser aplicado.

#### Scenario: Trocar dentro da faixa
- **WHEN** o usuário define o limite pra 30
- **THEN** o limite persistido passa a 30 e o UiState reflete 30

#### Scenario: Fora da faixa não aplica
- **WHEN** o usuário tenta um limite fora de 10–60
- **THEN** o valor persistido não muda

### Requirement: Pausar por hoje
Os ajustes SHALL deixar ativar "pausar por hoje" (D11), gravando no `ConfigRepository`. O estado
exibido SHALL refletir se está pausado agora.

#### Scenario: Ativar pausa
- **WHEN** o usuário ativa "pausar por hoje"
- **THEN** o `ConfigRepository` registra a pausa e o UiState mostra pausado ativo

