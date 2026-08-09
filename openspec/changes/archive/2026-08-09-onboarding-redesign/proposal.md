## Why

O onboarding atual existe mas é mínimo: uma tela de consentimento e uma lista de permissões em scroll único, sem coletar o perfil e sem design. Ele também não é "visto uma vez" — o gate reabre o passo de permissões toda vez que uma obrigatória falta, e as perguntas do perfil (tom, hobbies, limite) nunca são feitas. Este é o primeiro contato do usuário com um app que pede permissões incomuns (acesso ao uso, notificações, bateria, acessibilidade); ele precisa explicar **por que** cada uma existe, tela por tela, no tom calmo do produto (P1/P5/P6), e sair do caminho pra sempre depois de concluído.

## What Changes

- Redesenhar o onboarding como um **fluxo paginado, um conceito por tela** (welcome → cada permissão → perfil → conclusão), com indicador de progresso, aplicando a direção visual nova (âmbar + Bricolage/Hanken/Geist).
- **Animações e transições ("moderno e rico")** com uma tese de motion única — **"Surfacing"**: o conteúdo emerge, o app recolhe ao fim (P6). Rico por craft (física de mola, stagger, continuidade), não por volume. Elementos: stagger de entrada por tela; transição de página com mola + parallax do ícone; progresso âmbar que flui como ponteiro de relógio; confirmação de permissão concedida (checkmark + pulso âmbar, **confirmação não comemoração** — P5); dígitos do limite rolando em Geist Mono (odômetro/relógio); seleção de tom com mola + borda; conclusão que recolhe o onboarding e faz o contador vivo emergir. Consultar o `frontend-design` e as skills de design; estender o `ResurfaceMotion` (linguagem tidal existente), sem easings soltos.
- **Acessibilidade de motion**: respeitar reduced-motion (animator duration scale = 0 → transições viram instantâneo/crossfade; ambiente respirando → glow estático). Sem motion de gamificação (confetti/streak/mascote) por P5. Orçamento de motion contido: uma assinatura orquestrada + micro-interações disciplinadas.
- **Coletar o perfil no onboarding**: tom (direto/gentil/bem-humorado), hobbies (múltipla escolha + campo livre) e limite de minutos (10–60, default 20). As três respostas são **obrigatórias** para concluir.
- **"Visto uma vez" de verdade**: o gate passa a rotear por `onboardingCompleted`. Concluído uma vez, o onboarding **nunca reabre**. **BREAKING** (comportamento do gate): hoje o gate rota por permissão ao vivo; passa a rotar por conclusão persistida.
- **Estado de recuperação pós-onboarding**: se uma permissão obrigatória for revogada depois, o app mostra um estado dedicado ("permissão necessária") dentro do app — não o onboarding.
- Manter a acessibilidade como passo **opcional e pulável** (D15) e o passo de bateria/Samsung (D23), agora cada um em sua própria tela.
- Reaproveitar toda a infraestrutura existente: `OnboardingRepository`, `ProfileRepository`, `ConfigRepository`, `PermissionChecker`, `computeStartRoute`.

## Capabilities

### New Capabilities

_(nenhuma — o redesenho recai sobre capabilities existentes)_

### Modified Capabilities

- `onboarding-gate`: rotear por `onboardingCompleted` (once-only) em vez de só permissão ao vivo; adicionar estado de recuperação quando uma obrigatória falta após a conclusão.
- `permission-flow`: fluxo paginado (um conceito por tela) com progresso; o perfil passa a fazer parte do fluxo; concluir requer obrigatórias concedidas **e** perfil respondido, e marca `onboardingCompleted`.
- `user-profile`: tom, hobbies e limite são coletados durante o onboarding e são **obrigatórios** para concluir (continuam editáveis depois nos Ajustes).

## Impact

- **UI**: `ui/onboarding/*` (reescrita: pager, telas por passo, telas de perfil, tela de conclusão), `ui/StartRoute.kt` (gate por `onboardingCompleted` + rota de recuperação), `ui/ResurfaceApp.kt`, `ui/AppViewModel.kt`.
- **Data**: `OnboardingRepository` (usar `completed` no gate), `ProfileRepository.setTone/setHobbies`, `ConfigRepository.setLimit` (validação 10–60 já existe).
- **Permissões**: `PermissionChecker` (sem mudança de API; consumido pelo novo fluxo). Nenhuma permissão nova no manifesto.
- **Tema/Motion**: consome a paleta/tipografia novas; **estende `ui/theme/Motion.kt` (`ResurfaceMotion`)** com specs de entrada/transição/stagger e um helper de reduced-motion. Sem novas dependências (Compose animation já disponível).
- **Testes**: `computeStartRoute` (novos casos once-only + recuperação), validação do limite, e a máquina de passos do onboarding.
