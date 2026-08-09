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

### Requirement: Ajustes como hub com sub-telas
A tela de ajustes SHALL ser um hub que lista linhas navegáveis, cada uma abrindo sua tela focada: Profile (nome + hobbies), Reminders (limite + tom), Schedule (dias + janela), Wristband (pareamento + intensidade) e Debug (apenas em build de debug). Cada sub-tela SHALL ter uma top bar com voltar; o hub SHALL poder voltar pro dashboard.

#### Scenario: Linha abre a sub-tela
- **WHEN** o usuário toca na linha "Reminders"
- **THEN** o app navega pra tela de Reminders (limite + tom)

#### Scenario: Voltar retorna ao hub
- **WHEN** o usuário volta de uma sub-tela
- **THEN** o app retorna ao hub de ajustes

#### Scenario: Debug só em build de debug
- **WHEN** o hub é exibido em build de release
- **THEN** a linha de Debug não aparece

### Requirement: Valor atual nas linhas do hub
Cada linha do hub SHALL mostrar um subtítulo curto com o valor atual da respectiva configuração.

#### Scenario: Subtítulo reflete o valor
- **WHEN** o limite é 20 e o tom é Gentle
- **THEN** a linha "Reminders" mostra um subtítulo como "20 min · Gentle"

### Requirement: Pausar por hoje no hub
O hub SHALL oferecer "pausar por hoje" como ação rápida (F6), sem entrar numa sub-tela.

#### Scenario: Pausar direto no hub
- **WHEN** o usuário ativa "pausar por hoje" no hub
- **THEN** a pausa é registrada e o hub reflete o estado pausado

