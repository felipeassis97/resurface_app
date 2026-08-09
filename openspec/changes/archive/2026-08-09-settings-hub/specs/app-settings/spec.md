## ADDED Requirements

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
