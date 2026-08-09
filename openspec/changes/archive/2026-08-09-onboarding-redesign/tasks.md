## 1. Gate e roteamento (once-only + recuperação)

- [x] 1.1 Adicionar `PermissionRecovery` a `StartRoute` (sealed interface em `ui/StartRoute.kt`)
- [x] 1.2 Mudar `computeStartRoute` pra receber `onboardingCompleted` e retornar Onboarding/Main/PermissionRecovery conforme design §2 (mantendo função pura)
- [x] 1.3 Adicionar `firstPendingStep(consent, liveStatus)` puro que deriva o passo inicial do onboarding
- [x] 1.4 Atualizar `AppViewModel.refresh()` pra passar `onboardingCompleted` (de `OnboardingRepository.state`) ao gate
- [x] 1.5 Testes unitários de `computeStartRoute`: não concluído→onboarding; concluído+ok→Main; concluído+falta→recovery; e de `firstPendingStep`

## 2. Modelo de passos e ViewModel

- [x] 2.1 Reescrever `OnboardingStep` como sequência ordenada (WELCOME, USAGE, NOTIFICATIONS, BATTERY, TONE, HOBBIES, LIMIT, ACCESSIBILITY, DONE) com metadados (tipo/obrigatório/opcional)
- [x] 2.2 Criar `OnboardingViewModel` (Hilt): estado do pager, rascunho de perfil, write-through em `ProfileRepository`/`ConfigRepository`, e `completeOnboarding()`
- [x] 2.3 Regra de validação do perfil: tom escolhido + ≥1 hobby + limite ∈ 10–60 habilita conclusão
- [x] 2.4 `completeOnboarding()` exige obrigatórias concedidas + perfil válido e marca `onboarding.setCompleted(true)`

## 3. Componentes visuais compartilhados (design novo)

- [x] 3.1 `OnboardingScaffold`: layout de passo (indicador de progresso, área de conteúdo, insets edge-to-edge, ação primária âmbar + ação secundária de texto)
- [x] 3.2 Indicador de progresso (segmentos/dots por passo) usando o âmbar do tema
- [x] 3.3 Transição de página sutil (slide+fade) respeitando `reduced-motion`; foco de teclado visível
- [x] 3.4 Componente de tela de permissão reutilizável (ícone contido, porquê, chip de status ao vivo, ação) — usado no fluxo e na recuperação

## 4. Telas de permissão (uma por conceito)

- [x] 4.1 WELCOME: tese do produto (Bricolage) + promessa de privacidade (P4) + "Começar" (grava consentimento)
- [x] 4.2 USAGE: explicação + abre `ACTION_USAGE_ACCESS_SETTINGS`; avança quando concedido (status ao vivo)
- [x] 4.3 NOTIFICATIONS: explicação + diálogo runtime (Android 13+); avança quando concedido
- [x] 4.4 BATTERY: oferta de isenção + instrução do passo "apps que nunca dormem" (D23); adiável ("continuar")
- [x] 4.5 ACCESSIBILITY (após o perfil): opcional, explica restricted-settings em sideload; "ligar" ou "pular"

## 5. Telas de perfil (obrigatórias)

- [x] 5.1 TONE: 3 cards selecionáveis (direto/gentil/bem-humorado) com exemplo de mensagem; persiste via `setTone`
- [x] 5.2 HOBBIES: chips múltipla escolha + campo livre; exige ≥1; persiste via `setHobbies`
- [x] 5.3 LIMIT: número grande em Geist Mono + slider/stepper 10–60 (default 20); persiste via `setLimit`

## 6. Conclusão e recuperação

- [x] 6.1 DONE: confirmação curta; ao concluir, o gate leva pro app (P6: some)
- [x] 6.2 Tela `PermissionRecovery`: reusa o componente de permissão pra a(s) obrigatória(s) faltante(s); sem consentimento/perfil

## 7. Ligação e limpeza

- [x] 7.1 Reescrever `OnboardingFlow` como `HorizontalPager` sobre a sequência de passos, dirigido pelo `OnboardingViewModel`
- [x] 7.2 Ligar `ResurfaceApp` às 3 rotas (Onboarding / Main / PermissionRecovery), reavaliando no resume (`LifecycleResumeEffect`)
- [x] 7.3 Remover `WelcomeScreen`/`PermissionsScreen` antigas e código morto
- [x] 7.4 Conferir strings/microcópia contra PRODUTO §5.1 (tom calmo, do lado do usuário, sem culpa)

## 8. Motion (Surfacing)

- [x] 8.1 Estender `ResurfaceMotion`: specs de entrada (stagger), transição de página, e um helper `rememberReducedMotion()` que lê o animator duration scale (0 → desliga)
- [x] 8.2 Stagger de entrada no `OnboardingScaffold` (título → corpo → ação, ~50ms offset, `tidalSpring`)
- [x] 8.3 Transição de página no pager: slide com mola + parallax leve do ícone (profundidade)
- [x] 8.4 Indicador de progresso animado: fill âmbar fluindo pro próximo segmento (ponteiro de relógio)
- [x] 8.5 Confirmação de permissão concedida: chip pending→granted com checkmark desenhando + 1 pulso âmbar (breathing) — sem comemoração (P5)
- [x] 8.6 Limite: dígitos rolando (odômetro) em Geist Mono conforme o slider, tabular (sem jitter)
- [x] 8.7 Seleção de tom: scale com mola + borda âmbar desenhando
- [x] 8.8 Conclusão: recolher o onboarding (fade+sink) e fazer o contador vivo emergir (continuidade pro app, P6)
- [ ] 8.9 (Opcional) Glow ambiente respirando atrás do hero (`BreatheInhale/Exhale`); off no reduced-motion — **deferido** (loop infinito; avaliar custo no A53 antes)
- [x] 8.10 Reduced-motion: todas as animações acima caem pra instantâneo/crossfade quando desligado; revisar orçamento contra o frontend-design

## 9. Verificação

- [x] 9.1 `./gradlew :app:compileDebugKotlin` e `:app:testDebugUnitTest` passam
- [ ] 9.2 Rodar no aparelho/emulador: fluxo completo tela-a-tela em light e dark, edge-to-edge e predictive back ok — **manual (device)**
- [ ] 9.3 Confirmar once-only: concluir, revogar uma obrigatória, reabrir → cai em recuperação (não no onboarding); reconceder → app — **manual (device)**
- [ ] 9.4 Confirmar perfil persistido aparece nos Ajustes após o onboarding — **manual (device)**
- [ ] 9.5 Motion: verificar 60fps no fluxo e o fallback de reduced-motion (animator duration scale = 0) em todas as telas — **manual (device)**
