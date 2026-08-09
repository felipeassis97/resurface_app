# permission-flow Specification

## Purpose
TBD - created by archiving change onboarding. Update Purpose after archive.
## Requirements
### Requirement: Explicar e registrar o consentimento
A primeira tela SHALL explicar o que o app é e a promessa de privacidade (tudo local, sem conta —
P4), e registrar o consentimento antes de pedir qualquer permissão.

#### Scenario: Consentir avança
- **WHEN** o usuário confirma na tela inicial
- **THEN** o consentimento é registrado e o fluxo avança pras permissões

### Requirement: Conceder as obrigatórias pela UI
O fluxo SHALL levar o usuário a conceder o acesso ao uso (tela do sistema) e as notificações
(diálogo runtime, Android 13+), explicando por que cada uma é necessária.

#### Scenario: Acesso ao uso abre a tela do sistema
- **WHEN** o usuário toca em conceder o acesso ao uso
- **THEN** o app abre a tela de acesso ao uso do sistema

#### Scenario: Notificações pedem no diálogo
- **WHEN** o passo de notificações aparece no Android 13+
- **THEN** o app dispara o diálogo de permissão de notificações

### Requirement: Bateria e passo do Samsung
O fluxo SHALL oferecer a isenção de otimização de bateria e explicar o passo manual de adicionar o
app em "apps que nunca dormem" (D23), já que a isenção padrão não vence a suspensão do One UI.

#### Scenario: Oferecer isenção + instruir Samsung
- **WHEN** o passo de bateria aparece
- **THEN** o app oferece pedir a isenção e mostra a instrução do passo manual do Samsung

### Requirement: Acessibilidade opcional e pulável
O fluxo SHALL oferecer ligar a acessibilidade (dado de comportamento) como passo **opcional**,
explicando o passo de restricted settings em sideload, e permitir pular sem travar (D15).

#### Scenario: Pular a acessibilidade conclui o onboarding
- **WHEN** o usuário pula o passo de acessibilidade
- **THEN** o onboarding conclui e o app é liberado (só sem o dado de comportamento)

