## MODIFIED Requirements

### Requirement: Coletar o perfil no fluxo
O fluxo SHALL coletar, em telas próprias, o **nome + tom** (na mesma tela), os **hobbies** (múltipla escolha + campo livre) e o **limite de minutos** (10–60, padrão 20), persistindo cada resposta. As respostas SHALL ser obrigatórias pra concluir: um nome informado, um tom escolhido, ao menos um hobby, e um limite dentro da faixa.

#### Scenario: Nome e tom no mesmo passo
- **WHEN** o passo de perfil aparece
- **THEN** ele pede o nome (campo de texto) e o tom (opções) na mesma tela

#### Scenario: Perfil incompleto bloqueia a conclusão
- **WHEN** falta o nome, o tom, os hobbies ou o limite
- **THEN** a conclusão fica indisponível até as respostas estarem completas

#### Scenario: Perfil completo permite concluir
- **WHEN** nome, tom, ao menos um hobby e um limite válido foram respondidos
- **THEN** o fluxo permite avançar pra conclusão

### Requirement: Bateria e passo do Samsung
O fluxo SHALL oferecer, em sua própria tela, a isenção de otimização de bateria e explicar o passo manual de "apps que nunca dormem" (D23). A ação SHALL abrir de fato a tela do sistema: tentar o pedido direto de isenção e, em falha, cair na tela de lista de otimização de bateria. O passo SHALL refletir o estado atual (já isento → seguir) e SHALL ser adiável sem travar.

#### Scenario: Abrir a isenção funciona
- **WHEN** o usuário toca em pedir a isenção de bateria
- **THEN** o app abre a tela do sistema correspondente (pedido direto ou, em falha, a lista de otimização)

#### Scenario: Já isento reflete o estado
- **WHEN** o app já está isento da otimização de bateria
- **THEN** o passo mostra o estado concedido e permite continuar

#### Scenario: Adiar não trava
- **WHEN** o usuário escolhe adiar o passo de bateria
- **THEN** o fluxo avança normalmente

### Requirement: Acessibilidade opcional e pulável
O fluxo SHALL oferecer ligar a acessibilidade em sua própria tela, após o perfil, como passo opcional, explicando o passo de restricted settings em sideload, e permitir pular. Ao retornar das configurações com a acessibilidade **ligada**, o passo SHALL refletir o novo estado (ação passa a "continuar"), sem exigir reiniciar o app.

#### Scenario: Ligar e voltar reflete o estado
- **WHEN** o usuário liga a acessibilidade nas configurações e volta pro app
- **THEN** o passo mostra o estado ligado e a ação vira "continuar"

#### Scenario: Pular permite concluir
- **WHEN** o usuário pula o passo de acessibilidade
- **THEN** o fluxo segue pra conclusão (o app funciona sem o dado de comportamento)

## ADDED Requirements

### Requirement: Copy do onboarding em inglês, sem travessões
Todo o texto visível do onboarding SHALL estar em inglês e SHALL evitar travessões (`—`), usando pontuação simples, pra soar humano.

#### Scenario: Texto em inglês e limpo
- **WHEN** qualquer tela do onboarding é exibida
- **THEN** o texto está em inglês e não usa travessões
