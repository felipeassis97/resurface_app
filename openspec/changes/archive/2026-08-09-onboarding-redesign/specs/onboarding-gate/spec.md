## MODIFIED Requirements

### Requirement: Rotear pelo consentimento e permissões ao vivo
Na abertura, o gate SHALL rotear a partir da **conclusão persistida do onboarding** (`onboardingCompleted`) combinada com o status **ao vivo** das obrigatórias (acesso ao uso + notificações):
- onboarding **não** concluído → onboarding, a partir do passo pendente (welcome se ainda não houve consentimento; senão o próximo passo do fluxo);
- concluído **e** todas as obrigatórias concedidas → app principal;
- concluído **mas** faltando uma obrigatória → **estado de recuperação** (não reabre o onboarding).

O onboarding, uma vez concluído, NÃO SHALL reabrir.

#### Scenario: Primeira abertura vai pro onboarding
- **WHEN** o onboarding nunca foi concluído
- **THEN** o gate roteia pro onboarding a partir do passo pendente

#### Scenario: Concluído com tudo ok vai pro app
- **WHEN** o onboarding foi concluído e todas as obrigatórias estão concedidas
- **THEN** o gate roteia pro app principal

#### Scenario: Concluído mas obrigatória revogada vai pra recuperação
- **WHEN** o onboarding foi concluído mas o acesso ao uso foi revogado depois
- **THEN** o gate roteia pro estado de recuperação, sem reabrir o onboarding

#### Scenario: Durante o onboarding, faltando obrigatória, segue no fluxo
- **WHEN** o onboarding ainda não foi concluído e falta uma obrigatória
- **THEN** o gate mantém o usuário no fluxo de onboarding no passo pendente

### Requirement: Reavaliar ao retornar
Como conceder uma permissão especial acontece numa tela do sistema (sem callback), o gate SHALL reavaliar o status ao vivo a cada retorno ao app (resume) — tanto durante o onboarding quanto no estado de recuperação.

#### Scenario: Concedeu nas configs e voltou
- **WHEN** o usuário concede uma obrigatória na tela do sistema e volta pro app
- **THEN** o gate reavalia e, se tudo ok, avança (pro próximo passo no onboarding, ou pro app a partir da recuperação) sem ação extra

## ADDED Requirements

### Requirement: Estado de recuperação de permissão obrigatória
Depois de concluído o onboarding, se uma obrigatória faltar, o app SHALL mostrar um estado dedicado de recuperação que explica qual permissão falta e leva o usuário a concedê-la. Esse estado NÃO SHALL reabrir o onboarding nem repetir a coleta de perfil ou consentimento.

#### Scenario: Falta obrigatória mostra recuperação
- **WHEN** o onboarding está concluído e as notificações foram desativadas
- **THEN** o app mostra o estado de recuperação apontando a permissão faltante

#### Scenario: Reconceder volta pro app
- **WHEN** o usuário reconcede a permissão faltante e volta pro app
- **THEN** o app sai da recuperação e vai pro app principal, sem perfil nem consentimento de novo
