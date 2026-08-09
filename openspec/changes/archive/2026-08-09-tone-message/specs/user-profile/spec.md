## ADDED Requirements

### Requirement: Escolher e persistir o tom
O sistema SHALL deixar o usuário escolher um tom entre Direto, Gentil e Bem-humorado, e persistir a
escolha. O padrão SHALL ser Gentil quando nada foi escolhido.

#### Scenario: Escolher o tom
- **WHEN** o usuário escolhe "Bem-humorado"
- **THEN** o tom persistido passa a Bem-humorado e o aviso passa a usar esse tom

#### Scenario: Padrão sem escolha
- **WHEN** nenhum tom foi escolhido ainda
- **THEN** o tom é Gentil

### Requirement: Hobbies opcionais pra textura
O sistema SHALL deixar o usuário escolher hobbies (múltipla escolha + campo livre), persistidos, para
dar textura à mensagem. Os hobbies NÃO SHALL virar cobrança ("você devia estar lendo" é proibido, P5).

#### Scenario: Escolher hobbies
- **WHEN** o usuário marca "Ler" e "Exercício"
- **THEN** esses hobbies ficam disponíveis pra dar textura à mensagem

#### Scenario: Sem hobbies funciona
- **WHEN** o usuário não escolhe nenhum hobby
- **THEN** a mensagem é composta só com tom + momento, sem referência a hobby

### Requirement: Editar o perfil depois
O sistema SHALL permitir alterar o tom e os hobbies a qualquer momento nos Ajustes, além do onboarding.

#### Scenario: Trocar o tom nos ajustes
- **WHEN** o usuário troca o tom nos Ajustes
- **THEN** os avisos seguintes usam o novo tom
