## ADDED Requirements

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
