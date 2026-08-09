## ADDED Requirements

### Requirement: Nome do usuário
O sistema SHALL coletar e persistir o nome pelo qual a pessoa quer ser chamada. No onboarding o nome SHALL ser obrigatório pra concluir; nos ajustes SHALL ser editável. Sem nome persistido, o padrão é vazio (a saudação cai no fallback).

#### Scenario: Nome obrigatório no onboarding
- **WHEN** o passo de perfil está sem nome preenchido
- **THEN** a conclusão do onboarding fica indisponível até um nome ser informado

#### Scenario: Editar o nome depois
- **WHEN** o usuário troca o nome nos ajustes
- **THEN** o nome persistido muda e passa a valer na saudação e nas mensagens
