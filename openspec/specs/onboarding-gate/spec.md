# onboarding-gate Specification

## Purpose
TBD - created by archiving change onboarding. Update Purpose after archive.
## Requirements
### Requirement: Rotear pelo consentimento e permissões ao vivo
Na abertura, o sistema SHALL decidir o destino inicial a partir do consentimento persistido e do
status **ao vivo** das permissões obrigatórias (acesso ao uso + notificações): sem consentimento →
onboarding; com consentimento mas faltando obrigatória → onboarding nas permissões; tudo ok → app.

#### Scenario: Primeira abertura vai pro onboarding
- **WHEN** não há consentimento registrado
- **THEN** o gate roteia pra o onboarding (tela inicial)

#### Scenario: Consentido mas sem permissão obrigatória
- **WHEN** há consentimento mas falta o acesso ao uso
- **THEN** o gate roteia pra o onboarding no passo de permissões

#### Scenario: Tudo concedido vai pro app
- **WHEN** há consentimento e todas as obrigatórias estão concedidas
- **THEN** o gate roteia pro app principal

### Requirement: Reavaliar ao retornar
Como conceder uma permissão especial acontece numa tela do sistema (sem callback), o gate SHALL
reavaliar o status ao vivo a cada retorno ao app (resume).

#### Scenario: Concedeu nas configs e voltou
- **WHEN** o usuário concede o acesso ao uso na tela do sistema e volta pro app
- **THEN** o gate reavalia e, se tudo ok, avança pro app sem precisar reabrir

### Requirement: Acessibilidade não bloqueia
A permissão de acessibilidade é opcional (D15) e NÃO SHALL fazer parte do gate — o app é liberado
sem ela; ela só habilita o dado de comportamento.

#### Scenario: Sem acessibilidade, app liberado
- **WHEN** as obrigatórias estão concedidas mas a acessibilidade não
- **THEN** o gate roteia pro app normalmente

