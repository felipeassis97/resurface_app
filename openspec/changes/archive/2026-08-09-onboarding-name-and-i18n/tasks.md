## 1. Nome no perfil

- [x] 1.1 `Profile` (+`name: String = ""`)
- [x] 1.2 `ProfileRepository`: chave `profile_name`, `setName`, incluir no flow `profile`
- [x] 1.3 `OnboardingViewModel`: `setName` write-through; `OnboardingProfile.name` + `isValid` exige `name.isNotBlank()`

## 2. Passo nome + tom

- [x] 2.1 `ProfileSteps.ToneStep`: `OutlinedTextField` de nome no topo (obrigatório), acima dos cards de tom
- [x] 2.2 `OnboardingFlow`: passar nome/`onName` + `primaryEnabled` do step do tom = nome preenchido

## 3. Fix acessibilidade (causa-raiz) + gate

- [x] 3.1 `PermissionChecker.statuses()` sobre `AppPermission.entries` (a11y entra no mapa)
- [x] 3.2 Passo ACCESSIBILITY reflete estado ligado no resume (label vira "Continue")
- [x] 3.3 `ResurfaceApp`: `Crossfade` chaveado por tipo de rota (não pelo step) pra não pular o pager

## 4. Fix bateria

- [x] 4.1 Passo BATTERY: `try` no pedido direto → `catch` abre `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`
- [x] 4.2 Ler `PowerManager.isIgnoringBatteryOptimizations` → quando já isento, ação vira "Continue"

## 5. Saudação na home

- [x] 5.1 `DashboardViewModel`/`DashboardUiState`: expor `name` (de `ProfileRepository.profile`)
- [x] 5.2 `DashboardScreen`: top bar mostra "Hi, {name}" (fallback "Hi")

## 6. Nome nas mensagens

- [x] 6.1 `TemplateComposer.fill`: trocar `{name}` por `profile.name`
- [x] 6.2 `MessageTemplates`: pools em inglês + variantes com `{name}` (mantendo variantes sem nome)
- [x] 6.3 `CloudMessageGenerator.buildPrompt`: prompt em inglês, incluir o nome, regra "no dashes"

## 7. Inglês no resto do app + sem travessões

- [x] 7.1 Onboarding (`OnboardingFlow`, `ProfileSteps`, `PermissionRecoveryScreen`): copy em inglês, sem `—`
- [x] 7.2 Ajustes (`SettingsScreen` + `HOBBIES`/`TONES`): inglês, sem `—`, + editar nome
- [x] 7.3 Notificação (`NotifierImpl`): canais + botões em inglês ("right time"/"not now"); chaves `era_hora`/`agora_nao` intactas
- [x] 7.4 Grep final por acentos em literais de UI e por `—` em texto de usuário → zero

## 8. Verificação

- [x] 8.1 `./gradlew :app:compileDebugKotlin` e `:app:testDebugUnitTest` passam (atualizar testes de composer/guard se preciso)
- [ ] 8.2 Device: onboarding pede nome; bateria abre; acessibilidade vira "Continue" ao voltar ligada — **manual**
- [ ] 8.3 Device: home mostra "Hi, {name}"; aviso usa o nome; tudo em inglês sem travessões — **manual**
