## Why

Ajustes de acabamento no onboarding e no app, a partir de uso real: o app deve saber o nome da pessoa e falar com ela pelo nome; a interface e as mensagens devem estar todas em inglês (consistência — hoje é PT/EN misto); dois passos do onboarding têm bug (bateria não abre, acessibilidade não reflete que foi ligada); e o texto deve soar humano (travessões `—` denunciam texto de IA).

## What Changes

- **Coletar o nome** no onboarding, no **mesmo passo do tom** (campo de texto + escolha de tom). Nome é **obrigatório** pra concluir. Persistido junto do perfil.
- **Saudação na home**: a top bar deixa de mostrar "Resurface" e passa a mostrar **"Hi, {name}"** (fallback "Hi" se vazio).
- **Nome em tudo**: além da saudação, as **mensagens do aviso** passam a usar o nome (slot `{name}` nos templates + prompt).
- **App todo em inglês**: onboarding, ajustes, dashboard, mensagens à mão (`MessageTemplates`), **prompt do Gemini**, e a notificação (nomes de canal + botões "right time"/"not now"). As **chaves** de resposta guardadas (`era_hora`/`agora_nao`) não mudam — só o texto exibido. Comentários de código e docs seguem em português.
- **Sem travessões** (`—`) no texto de usuário — reescrever a copy com pontuação simples. A regra "no dashes" já existe no prompt; passa a inglês.
- **Fix bateria** (Ajuste 4): o botão de isenção não abre nada. Passar a tentar o pedido direto e, em falha, cair na tela de lista de otimização de bateria; refletir o estado atual (já isento → "Continue").
- **Fix acessibilidade** (Ajuste 5): ao ligar a acessibilidade e voltar, o passo continua com "Ligar/Pular". **Causa-raiz**: `PermissionChecker.statuses()` só inclui as obrigatórias, então a acessibilidade nunca aparece no mapa de status. Passar a reportar todas as permissões; o passo passa a mostrar "Continue" quando ligada.
- Robustez do gate durante o onboarding: o `Crossfade` de rota passa a chavear pelo **tipo** de rota (não pelo passo), pra a mudança de status no resume não fazer o pager pular.

## Capabilities

### New Capabilities

_(nenhuma)_

### Modified Capabilities

- `user-profile`: passa a coletar e persistir o **nome** (obrigatório no onboarding, editável nos ajustes).
- `permission-flow`: nome no passo do tom (obrigatório); correção do passo de bateria (abre de fato + reflete estado); correção do passo de acessibilidade (reflete que foi ligada); copy do onboarding em inglês sem travessões.
- `dashboard`: saudação personalizada na top bar ("Hi, {name}").
- `message-composition`: mensagens em inglês e personalizadas com o nome, mantendo P2/P5, sem travessões; prompt do Gemini em inglês.
- `app-settings`: editar o nome; copy dos ajustes em inglês.

## Impact

- **Perfil/dados**: `domain/model/Profile` (+`name`), `data/profile/ProfileRepository` (+chave `profile_name`).
- **Onboarding**: `ui/onboarding/ProfileSteps` (TextField de nome no step do tom + validação), `OnboardingFlow` (bateria com fallback + `PowerManager`; acessibilidade reflete estado), `OnboardingViewModel` (name write-through + validação incluindo nome).
- **Permissão**: `permission/PermissionChecker.statuses()` sobre `AppPermission.entries` (a11y no mapa).
- **Gate**: `ui/ResurfaceApp` (Crossfade por tipo de rota).
- **Dashboard**: `DashboardViewModel` (+name), `DashboardScreen` (saudação).
- **Mensagens**: `domain/MessageTemplates` (inglês + `{name}`), `domain/TemplateComposer` + `domain/model/Moment` (name), `data/generation/CloudMessageGenerator` (prompt inglês + name).
- **Notificação**: `data/notification/NotifierImpl` (canais + botões em inglês).
- **Ajustes**: `ui/screens/settings/*` (inglês + editar nome).
- **Testes**: `TemplateComposerTest`/`MessageGuardTest` (inglês/nome), previews.
