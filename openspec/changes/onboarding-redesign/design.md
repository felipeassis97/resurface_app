## Context

Já existe um onboarding funcional porém mínimo: `WelcomeScreen` + `PermissionsScreen` (lista em scroll único), roteado por `computeStartRoute(state, allRequiredGranted)`. A infra de dados está pronta (`OnboardingRepository` com `consent`/`completed`, `ProfileRepository`, `ConfigRepository`, `PermissionChecker` ao vivo). Falta: fluxo paginado (um conceito por tela), coleta de perfil, gating "visto uma vez" de verdade, e o design novo (âmbar + Bricolage/Hanken/Geist, já aplicado no tema).

Restrições de plataforma (targetSdk 36): edge-to-edge obrigatório (tratar insets), predictive back ligado, orientação/tamanho livres. Alvo único: SM-A536E / One UI 8.

## Goals / Non-Goals

**Goals:**
- Fluxo paginado, um conceito por tela, com progresso e volta (predictive back).
- Explicar o **porquê** de cada permissão no tom do produto, tela por tela.
- Coletar tom + hobbies + limite (obrigatórios) dentro do fluxo.
- "Visto uma vez": concluído → nunca reabre; permissão revogada depois → estado de recuperação separado.
- Reaproveitar repos, `PermissionChecker` e a pureza de `computeStartRoute`.
- Aplicar a direção visual nova, respeitando P1/P5/P6 (sem culpa, sem pressão, calmo).

**Non-Goals:**
- Janela de horário (schedule) no onboarding — fica em Ajustes.
- Geração de mensagem / qualquer coisa de runtime do serviço.
- Animações elaboradas; motion é sutil e respeita reduced-motion.
- Mudança em permissões do manifesto (nenhuma nova).

## Decisions

### 1. Sequência de passos (um conceito por tela)
Substituir o enum de 2 passos por uma sequência ordenada que dirige um `HorizontalPager`. Ordem (segue PRODUTO §5.1):

```
WELCOME → USAGE → NOTIFICATIONS → BATTERY → TONE → HOBBIES → LIMIT → ACCESSIBILITY(opcional) → DONE
  o quê     obrig.     obrig.       adiável   perfil  perfil   perfil    opcional/pulável
```

Cada passo é um composable próprio; um modelo `OnboardingStep` (ordenado) descreve título/tipo. Progresso = índice na sequência (a11y conta como passo, mas pulável). O progresso é legítimo como estrutura numerada — **é** uma sequência real (structure-is-information).

_Alternativa descartada:_ manter lista única em scroll — não é "tela por tela" e mistura conceitos.

### 2. Gate por conclusão + recuperação
`computeStartRoute` passa a receber `onboardingCompleted` além do status ao vivo, e a retornar 3 rotas:

```kotlin
sealed interface StartRoute { Loading; data class Onboarding(step); Main; PermissionRecovery }

fun computeStartRoute(state, allRequiredGranted): StartRoute = when {
    !state.onboardingCompleted -> Onboarding(firstPendingStep(state, liveStatus))
    !allRequiredGranted        -> PermissionRecovery      // concluído, mas revogou depois
    else                       -> Main
}
```

Mantém-se puro e testável. `firstPendingStep` deriva o passo inicial de `consent` + status ao vivo (sem persistir posição do pager — ver risco).

_Alternativa descartada:_ rotar por permissão ao vivo (atual) — reabre onboarding e viola "visto uma vez".

### 3. Recuperação reusa o componente de permissão
`PermissionRecovery` é uma tela única que reaproveita o mesmo card/tela de permissão do fluxo, mostrando só a(s) obrigatória(s) faltante(s) e o caminho de concessão. Sem consentimento, sem perfil. Some sozinha quando `allRequiredGranted` volta a ser true (reavaliado no resume).

### 4. `OnboardingViewModel` separado do `AppViewModel`
`AppViewModel` continua só o gate (rota + status). Um `OnboardingViewModel` (Hilt) novo dono de: posição do pager (`rememberSaveable` + estado), rascunho do perfil e write-through nos repos (`setTone`/`setHobbies`/`setLimit`), e `completeOnboarding()`. Separa "para onde vou" de "o que estou preenchendo".

### 5. Validação do perfil (obrigatório)
"Concluir" só habilita com: tom escolhido **e** ≥1 hobby (marcado ou campo livre) **e** limite ∈ 10–60. Escrita é write-through (persiste conforme responde), então processo morto não perde perfil. O `LIMIT` usa `ConfigRepository.setLimit` (já valida a faixa).

### 6. Design visual (frontend-design)
Sistema já definido: âmbar único, Bricolage (voz), Hanken (interface), Geist Mono (números). Estrutura de uma tela de passo:

```
┌───────────────────────────────────────────┐
│  ▁▁▁ ▔▔▔ ▁▁▁ ▁▁▁ ▁▁▁   (progresso, âmbar)  │  ← barra/segmentos = passos
│                                            │
│   ◍  ícone em círculo âmbar-fraco          │  ← 1 marca visual, contida
│                                            │
│   Título curto            (Bricolage 700)  │  ← a voz: "por que preciso disso"
│   Explicação em 1–2 frases (Hanken)        │  ← do lado do usuário, calmo
│                                            │
│   [ status ao vivo: ✓ concedido / • falta ]│  ← chip só em telas de permissão
│                                            │
│                                            │
│   ┌──────────────────────────────────┐     │
│   │        Ação primária (âmbar)      │     │  ← "Conceder" / "Permitir" / "Avançar"
│   └──────────────────────────────────┘     │
│           Pular / Depois (texto)           │  ← só onde é adiável/opcional
└───────────────────────────────────────────┘
```

- **Welcome** = tese do produto na voz (Bricolage grande) + promessa de privacidade (muted) + "Começar". É o hero.
- **Permissão**: ícone contido, o porquê em 1–2 frases, chip de status ao vivo, ação primária; avança quando concedida (ou "depois" na bateria).
- **Perfil/tom**: 3 opções como cards selecionáveis, cada uma com exemplo de mensagem no tom.
- **Perfil/hobbies**: chips de múltipla escolha + campo livre.
- **Perfil/limite**: número grande em **Geist Mono** + slider/stepper (10–60), default 20.
- **Conclusão**: confirmação curta, some pro app (P6).
- Insets edge-to-edge tratados; transição de página sutil (slide+fade) com `reduced-motion` respeitado; foco de teclado visível.
- Microcópia base vem do PRODUTO §5.1 (reusar as frases já validadas).

### 7. Sistema de motion ("Surfacing")
Tese única: o conteúdo **emerge** e o app **recolhe** ao fim (P6). Rico por craft (mola, stagger, continuidade), não por volume. Consultar `frontend-design`; estender `ResurfaceMotion` (linguagem tidal existente — `tidalSpring`, easings Emphasized, breathing loop), sem easings soltos.

Elementos:

```
1. STAGGER por tela      título → corpo → ação sobem+fade, ~50ms offset (tidalSpring)
2. TRANSIÇÃO de página   slide com mola + parallax leve do ícone (profundidade), não tween linear
3. PROGRESSO = ponteiro   fill âmbar flui pro próximo segmento com mola (a "mão do relógio")
4. PERMISSÃO concedida    chip pending→granted: checkmark desenha + 1 pulso âmbar (breathing). Confirmação, não festa (P5)
5. LIMITE (Geist Mono)    dígitos rolam tipo odômetro conforme o slider (relógio/contador, tabular = sem jitter)
6. TOM cards             seleção = scale mola + borda âmbar desenhando
7. CONCLUSÃO             onboarding recolhe (fade+sink) e o contador vivo emerge → continuidade pro app (P6)
8. AMBIENTE (opcional)   glow respirando lento atrás do hero (BreatheInhale/Exhale). Off no reduced-motion
```

Guardrails:
- **Reduced-motion** (piso de qualidade): helper que lê o animator duration scale do sistema (0 → desliga). Transições viram instantâneo/crossfade; o breathing vira glow estático. Um único ponto de decisão, consumido por todos os passos.
- **Sem motion de gamificação** (P5): nada de confetti, mascote, bounce de streak, "subiu de nível".
- **Orçamento contido**: uma assinatura orquestrada (hero + progresso-relógio) + micro-interações disciplinadas. Nada espalhado (aviso do frontend-design: excesso lê como IA).
- **60fps no A53**: só `graphicsLayer`/alpha/translation; evitar recomposição por frame (usar `Animatable`/`updateTransition`, não estado recomposto).

_Alternativa descartada:_ transições padrão do NavHost/tween linear — funcionam, mas não entregam o "moderno e rico" pedido nem a continuidade da marca.

## Risks / Trade-offs

- **Posição do pager perdida em process death** → não persistimos posição; `firstPendingStep` re-deriva de consent + status ao vivo (perfil já foi write-through). Custo: reinício raro cai no primeiro passo pendente. Aceito.
- **Hobbies obrigatório contraria "pulável" do F1** → decisão explícita do dono (tudo obrigatório). Documentado; a composição de mensagem continua tolerando vazio (para remoção posterior nos Ajustes).
- **Restricted-settings da acessibilidade (sideload) não é automatizável** → instruímos manualmente; o status de a11y ainda é lido ao vivo. Sem detecção do toggle de "config restritas".
- **"Apps que nunca dormem" é caminho manual do Samsung** → instrução + screenshot; não dá pra verificar programaticamente. Bateria é adiável, não trava.
- **BREAKING no gate** → `computeStartRoute` muda de assinatura; atualizar chamadas e testes. Sem migração de dados (chaves do DataStore intactas; `completed` passa a ser consumido).
- **Glow ambiente é loop infinito** → único motion contínuo (custo de CPU/bateria enquanto a tela está aberta). Numa tela vista 1x é aceitável; fica **opcional** e desligado no reduced-motion. Se pesar no A53, cortar sem impacto no resto.
- **Motion virar "demais"** → orçamento contido + uma assinatura só; revisar contra o frontend-design ("excesso lê como IA") antes de fechar.

## Migration Plan

1. Adicionar `PermissionRecovery` a `StartRoute` e a nova assinatura de `computeStartRoute` (+ testes).
2. Introduzir a sequência `OnboardingStep` ordenada e o `OnboardingViewModel`.
3. Reescrever `OnboardingFlow` como pager com as telas por passo + perfil + conclusão.
4. Ligar `ResurfaceApp` às 3 rotas (Onboarding/Main/Recovery).
5. `completeOnboarding()` passa a exigir obrigatórias + perfil e a marcar `completed`.
6. Remover a `PermissionsScreen`/`WelcomeScreen` antigas.

Rollback: reverter os arquivos de `ui/onboarding/*`, `ui/StartRoute.kt`, `ui/AppViewModel.kt`, `ui/ResurfaceApp.kt`. DataStore não muda de forma.

## Open Questions

- Conjunto de ícones/ilustração por permissão — usar Material Icons (já no projeto) ou um traço custom? (Default: Material Icons, contido em círculo âmbar-fraco.)
- ~~Limite: slider, stepper, ou ambos?~~ **Decidido:** slider com número Geist Mono grande rolando (odômetro), ver motion §7.5.
- Glow ambiente no hero: manter ou cortar? (Default: manter, opcional + off no reduced-motion; cortar se pesar no A53.)
- A recuperação deve oferecer também religar a acessibilidade? (Default: não — só obrigatórias; a11y é opcional.)
