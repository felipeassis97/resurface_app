## Context

Onboarding e app já existem (changes anteriores). Ajustes de acabamento a partir de uso real: coletar/usar o nome, unificar idioma em inglês, limpar a copy (sem travessões) e corrigir dois passos com bug (bateria, acessibilidade). O `TemplateComposer.fill` já troca slots `{min}/{app}/{hobby}` e recebe o `Profile`; o prompt do Gemini está em `CloudMessageGenerator`. `PermissionChecker.statuses()` hoje só cobre as obrigatórias.

## Goals / Non-Goals

**Goals:**
- Nome coletado (obrigatório) no passo do tom, persistido, usado na saudação e nas mensagens.
- App inteiro em inglês (UI + templates + prompt + notificação), chaves internas intactas.
- Copy sem travessões.
- Bateria abre de fato + reflete estado; acessibilidade reflete quando ligada.

**Non-Goals:**
- Localização multi-idioma (é inglês fixo, não i18n com resources por locale).
- Mudar a lógica de decisão de aviso, o serviço, o schema do Room.
- Traduzir comentários de código e docs (seguem PT).

## Decisions

### 1. Nome no modelo e no perfil
`Profile` ganha `name: String = ""`. `ProfileRepository` +chave `profile_name` (`setName`, e no `profile` flow). `OnboardingProfile`/validação incluem `name`; `isValid` passa a exigir `name.isNotBlank()` além de `hasHobby`.

### 2. Passo nome+tom
O step TONE ganha um `OutlinedTextField` de nome no topo, acima dos cards de tom. Sem passo novo (mantém a sequência). `primaryEnabled` do step = nome preenchido (tom já tem default). `OnboardingViewModel.setName` write-through.

### 3. Nome nas mensagens
`TemplateComposer.fill` passa a trocar `{name}` por `profile.name`. Os 3 pools de `MessageTemplates` ganham variantes com `{name}` (em inglês). `CloudMessageGenerator.buildPrompt` recebe o nome e o inclui (prompt em inglês). Sem nome, `{name}` vira vazio e os templates sem nome são escolhidos naturalmente (como já ocorre com `{hobby}`).

### 4. Saudação na home
`DashboardViewModel` expõe o nome (de `ProfileRepository.profile`) no `DashboardUiState`. `DashboardScreen` troca o título "Resurface" por `"Hi, ${name.ifBlank { "" }}"` → "Hi, Felipe" / "Hi".

### 5. Fix acessibilidade (causa-raiz)
`PermissionChecker.statuses()` passa a mapear **todas** as permissões (`AppPermission.entries`), não só `required`. Assim `statuses[ACCESSIBILITY]` fica correto e o passo, no resume (que já chama `refresh()`), mostra "Continue". `allRequiredGranted()` continua só sobre `required` (gate inalterado).

### 6. Fix bateria
No passo BATTERY: `try` no `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (com `package:`); em `ActivityNotFoundException`/falha, `catch` abre `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` (lista, sempre resolve). Ler `PowerManager.isIgnoringBatteryOptimizations(packageName)` pra, quando já isento, mostrar "Continue" em vez de "Pedir isenção". (Bateria não é obrigatória; o estado é conveniência.)

### 7. Robustez do gate (Crossfade)
`ResurfaceApp` passa a chavear o `Crossfade` por um discriminante de **tipo** (Loading/Onboarding/Main/Recovery), não pelo `StartRoute.Onboarding(step)` inteiro. Assim, `refresh()` no resume (que recomputa `route.step` via `firstPendingStep`) não recria o `OnboardingFlow` nem faz o pager pular; o `step` só define a página inicial.

### 8. Sweep de inglês + sem travessões
Reescrever a copy de usuário em inglês, pontuação simples (sem `—`), em: onboarding (`OnboardingFlow`, `ProfileSteps`, `PermissionRecoveryScreen`), ajustes (`SettingsScreen` + listas `HOBBIES`/`TONES`), `MessageTemplates`, prompt (`CloudMessageGenerator`), notificação (`NotifierImpl`: canais "Counter"/"Alert", título "Resurface" da fixa pode ficar, botões "right time"/"not now"). Guardar chaves `era_hora`/`agora_nao`. `dayLabel`/`responseLabel` já foram pro inglês na change anterior.

## Risks / Trade-offs

- **Nome obrigatório** → primeira execução tem uma fricção a mais; aceito (decisão do dono).
- **Bateria: causa exata não confirmada em device** → o fallback pra tela de lista resolve o "não abre" independentemente da causa; confirmar no aparelho.
- **Sweep de inglês é amplo** → risco de sobrar string PT; mitigar com grep final por acentos em literais de UI.
- **`{name}` em texto gerado** → o guard P2/P5 continua valendo; nome é dado neutro, sem risco de cobrança.
- **Templates com nome vs sem** → manter variantes sem nome no pool pra não forçar nome em todas (e cobrir nome vazio).

## Migration Plan

1. `Profile.name` + `ProfileRepository` (chave + setName).
2. `PermissionChecker.statuses()` sobre entries (fix a11y) + testes.
3. Onboarding: nome no step do tom + validação; bateria (fallback + status); `ResurfaceApp` Crossfade por tipo.
4. Dashboard: saudação.
5. Mensagens: `{name}` no composer + templates (inglês) + prompt (inglês).
6. Notificação + ajustes em inglês + editar nome.
7. Sweep final de inglês/sem travessões + testes.

Rollback: reverter por área; sem migração de dados (nova chave é aditiva; `name` default vazio).

## Open Questions

- Título da notificação fixa: manter "Resurface" ou "Counter"? (Default: manter "Resurface" como marca; corpo/estado em inglês.)
- Editar nome nos ajustes: campo de texto simples no topo do perfil. (Default: sim.)
