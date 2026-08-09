## ADDED Requirements

### Requirement: Saudação personalizada na top bar
A top bar do dashboard SHALL saudar a pessoa pelo nome — "Hi, {name}" — em vez de mostrar o nome do app. Sem nome persistido, SHALL usar o fallback "Hi".

#### Scenario: Saudação com o nome
- **WHEN** o nome persistido é "Felipe"
- **THEN** a top bar mostra "Hi, Felipe"

#### Scenario: Sem nome usa fallback
- **WHEN** não há nome persistido
- **THEN** a top bar mostra "Hi"
