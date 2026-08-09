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
O fluxo SHALL levar o usuário a conceder o acesso ao uso (tela do sistema) e as notificações (diálogo runtime, Android 13+), **cada uma em sua própria tela**, explicando por que é necessária e mostrando o status ao vivo; quando concedida, a tela permite avançar.

#### Scenario: Acesso ao uso abre a tela do sistema
- **WHEN** o usuário toca em conceder o acesso ao uso
- **THEN** o app abre a tela de acesso ao uso do sistema e, ao voltar concedido, permite avançar

#### Scenario: Notificações pedem no diálogo
- **WHEN** o passo de notificações aparece no Android 13+
- **THEN** o app dispara o diálogo de permissão de notificações e, ao conceder, permite avançar

### Requirement: Bateria e passo do Samsung
O fluxo SHALL oferecer, **em sua própria tela**, a isenção de otimização de bateria e explicar o passo manual de adicionar o app em "apps que nunca dormem" (D23), já que a isenção padrão não vence a suspensão do One UI. Este passo SHALL ser adiável ("depois") sem travar o fluxo.

#### Scenario: Oferecer isenção + instruir Samsung
- **WHEN** a tela de bateria aparece
- **THEN** o app oferece pedir a isenção e mostra a instrução do passo manual do Samsung

#### Scenario: Adiar a bateria não trava
- **WHEN** o usuário escolhe "depois" na tela de bateria
- **THEN** o fluxo avança normalmente

### Requirement: Acessibilidade opcional e pulável
O fluxo SHALL oferecer ligar a acessibilidade (dado de comportamento) **em sua própria tela, após o perfil**, como passo **opcional**, explicando o passo de restricted settings em sideload, e permitir pular sem travar (D15).

#### Scenario: Pular a acessibilidade permite concluir
- **WHEN** o usuário pula o passo de acessibilidade
- **THEN** o fluxo segue pra conclusão (o app funciona, só sem o dado de comportamento)

### Requirement: Fluxo paginado, um conceito por tela
O onboarding SHALL ser um fluxo paginado com **um conceito por tela** — welcome, cada permissão em sua própria tela, cada pergunta de perfil em sua própria tela e a conclusão — com indicador de progresso e navegação pra frente/trás compatível com o predictive back (targetSdk 36).

#### Scenario: Avança uma tela por vez
- **WHEN** o usuário completa a ação de uma tela e toca em avançar
- **THEN** o fluxo mostra o próximo conceito, com o progresso atualizado

#### Scenario: Voltar preserva o que já foi feito
- **WHEN** o usuário volta uma tela (botão ou gesto)
- **THEN** o passo anterior reaparece com o estado já respondido/concedido preservado

### Requirement: Coletar o perfil no fluxo
O fluxo SHALL coletar, em telas próprias, o **tom** (direto/gentil/bem-humorado), os **hobbies** (múltipla escolha + campo livre) e o **limite de minutos** (10–60, padrão 20), persistindo cada resposta. As três respostas SHALL ser obrigatórias para concluir: um tom escolhido, ao menos um hobby, e um limite dentro da faixa.

#### Scenario: Perfil incompleto bloqueia a conclusão
- **WHEN** falta escolher o tom, os hobbies ou o limite
- **THEN** a conclusão fica indisponível até as três estarem respondidas

#### Scenario: Perfil completo permite concluir
- **WHEN** tom, ao menos um hobby e um limite válido foram respondidos
- **THEN** o fluxo permite avançar pra conclusão

### Requirement: Concluir marca o onboarding
Concluir SHALL exigir **todas as obrigatórias concedidas** e o **perfil respondido**, e então SHALL marcar `onboardingCompleted`. A acessibilidade, por ser opcional (D15), NÃO SHALL ser exigida pra concluir.

#### Scenario: Tudo pronto conclui e marca
- **WHEN** as obrigatórias estão concedidas e o perfil está respondido
- **THEN** o onboarding é marcado como concluído e o app é liberado

#### Scenario: Falta obrigatória não conclui
- **WHEN** o perfil está respondido mas falta uma obrigatória
- **THEN** o onboarding não conclui e o passo da obrigatória faltante permanece pendente

