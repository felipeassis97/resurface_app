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

### Requirement: Editar o nome
Os ajustes SHALL deixar o usuário ver e trocar o nome, gravando no perfil. O novo nome SHALL valer na saudação da home e nas mensagens.

#### Scenario: Trocar o nome
- **WHEN** o usuário troca o nome nos ajustes
- **THEN** o nome persistido muda e a saudação/mensagens passam a usá-lo

### Requirement: Copy dos ajustes em inglês
Todo o texto visível dos ajustes SHALL estar em inglês, sem travessões (`—`).

#### Scenario: Ajustes em inglês
- **WHEN** a tela de ajustes é exibida
- **THEN** os rótulos e textos estão em inglês e sem travessões

