## Context

Última peça: empacotar o app pra instalar sem adb. Reusa o padrão do `resurface_old` (AppViewModel
+ StartRoute + OnboardingRepository + OnboardingFlow) e o `PermissionChecker` já pronto (uso,
notificações, acessibilidade). Segue o `ENGENHARIA` §6 (ViewModel→StateFlow) e o fluxo do PRODUTO
§5.1. Só decide o que mostrar na abertura + as telas de concessão — não toca no loop de fundo.

## Goals / Non-Goals

**Goals:**
- Gate de launch testável: `StartRoute` derivado de consentimento + permissões ao vivo.
- Telas que levam a conceder cada permissão, com re-checagem no resume.
- App liberado sem acessibilidade (opcional, D15); passo do Samsung explicado (D23).

**Non-Goals:**
- Perfil/tom (F2, não escolhido). Animações elaboradas. Deep-link estável pra tela de suspensão do
  Samsung (não documentado — só instrução com print).
- Automatizar o restricted settings ou o passo do Samsung (não dá; o usuário faz uma vez).

## Decisions

### D-1: `AppViewModel` + `StartRoute` (padrão resurface_old), lógica testável
`sealed interface StartRoute { Loading; Onboarding(step); Main }`. `AppViewModel.refresh()` lê
`onboardingRepository.state` + `permissionChecker.allRequiredGranted()` e computa a rota. A UI
(`ResurfaceApp`) observa e re-chama `refresh()` em cada resume (`LifecycleResumeEffect`).
- **Testável:** o cálculo da rota (dado consentimento + granted) é testado com fakes.

### D-2: `PermissionChecker` já cobre; `required` = uso + notificações
Nada novo no checker — `allRequiredGranted()` já usa `AppPermission.required` (uso + notificações;
acessibilidade fora, D15). O gate usa isso direto.

### D-3: `OnboardingRepository` (DataStore) — consentimento + concluído
Novo repositório pequeno (espelha o do old): `consentGiven`, `onboardingCompleted`. O status de
permissão NÃO é guardado aqui — é sempre ao vivo (G3).

### D-4: Telas dirigem o sistema; re-checam no resume
Cada passo especial abre a tela do sistema via `permissionChecker.settingsIntent(...)`; notificações
usam o launcher de permissão runtime. Ao voltar, o gate reavalia. O passo do Samsung é instrução +
botão que abre a tela de bateria (sem deep-link frágil).

### D-5: FGS sobe ao entrar no Main
Hoje o `MainActivity.onCreate` sobe o FGS sempre. Passa a subir quando o gate roteia pra `Main`
(permissões concedidas) — de contexto de foreground (D-7 do monitor-service). Evita subir sem uso.

## Risks / Trade-offs

- **[Restricted settings]** ligar a11y em sideload trava atrás de "permitir config. restritas". O
  fluxo explica com texto/print; não há como automatizar. É opcional, então não bloqueia.
- **[Samsung sleeping-apps]** não automatizável; instrução + `dontkillmyapp.com/samsung`. O alarme
  exato (D22) já garante o aviso mesmo sem esse passo; ele só melhora a pontualidade do tick.
- **[Re-onboard após limpar dado]** ao limpar dado, consentimento some → onboarding de novo. É o
  comportamento correto (dado local, P4).

## Migration Plan

Não aplicável — só UI + um repositório DataStore novo, sem schema.

## Open Questions

- Mostrar o status ao vivo de cada permissão como checklist no onboarding (verde/cinza)? Proposta:
  sim, simples — cada passo mostra concedido/pendente lido do `PermissionChecker`. Ajuda o usuário a
  ver o que falta. Decidir na task.
