## MODIFIED Requirements

### Requirement: Escolher e persistir o tom
O sistema SHALL deixar o usuário escolher um tom entre Direto, Gentil e Bem-humorado, e persistir a escolha. No **onboarding**, escolher um tom SHALL ser obrigatório pra concluir (Gentil vem pré-selecionado como sugestão e conta como escolha ao confirmar). Na ausência de qualquer valor persistido, o padrão SHALL ser Gentil.

#### Scenario: Escolher o tom
- **WHEN** o usuário escolhe "Bem-humorado"
- **THEN** o tom persistido passa a Bem-humorado e o aviso passa a usar esse tom

#### Scenario: Padrão sem escolha
- **WHEN** nenhum tom foi persistido ainda
- **THEN** o tom é Gentil

### Requirement: Hobbies opcionais pra textura
O sistema SHALL deixar o usuário escolher hobbies (múltipla escolha + campo livre), persistidos, para dar textura à mensagem. Os hobbies NÃO SHALL virar cobrança ("você devia estar lendo" é proibido, P5). No **onboarding**, o usuário SHALL escolher ao menos um hobby (marcado ou campo livre) pra concluir; depois, nos Ajustes, os hobbies podem ser editados ou removidos, e a composição da mensagem SHALL funcionar mesmo sem nenhum.

#### Scenario: Escolher hobbies no onboarding
- **WHEN** o usuário marca "Ler" e "Exercício" no onboarding
- **THEN** esses hobbies ficam persistidos e a conclusão fica disponível

#### Scenario: Sem hobbies funciona depois
- **WHEN** o usuário remove todos os hobbies nos Ajustes
- **THEN** a mensagem é composta só com tom + momento, sem referência a hobby
