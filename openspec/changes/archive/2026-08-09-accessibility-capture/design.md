## Context

A fonte de COMPORTAMENTO (F5), separada da fonte de TEMPO (D13 = UsageStats). Já validada no
aparelho (`REELS.md`/`TIKTOK.md`): regra de vídeo `type=4096·ViewPager·dy≠0·dx=0·gap>0,5s`
(gabarito 16=16), hesitação por `dy<0`, superfície por classe. O serviço de acessibilidade é
privilegiado e sobreviveu ao Freecess nos testes. Este change é **aditivo e desligável** (D15):
não toca no contador, aviso ou outcome.

## Goals / Non-Goals

**Goals:**
- `SwipeDetector` **puro** que aplica a regra validada (vídeo, hesitação, superfície), testado
  contra as assinaturas reais dos logs.
- `AccessibilityService` filtrado nos 2 alvos que agrupa deslizes ao vivo e persiste os detectados.
- Tabela `behavior_event` no Room; correlação com episódios é query-time (no `insights-dashboard`).
- Permissão de acessibilidade detectada ao vivo; app inteiro funciona sem ela (D15).

**Non-Goals:**
- Alterar tempo/episódio/aviso (D13/F5). Reels vs feed NO AVISO (fica só no dado, limitação #2).
- Mão-vs-mesa (bloqueado por L1, logger não expõe acelerômetro). Ritmo de varredura (depois).
- A UI que mostra isso (é o `insights-dashboard`).

## Decisions

### D-1: `SwipeDetector` puro (classifica grupo + detecta lista), service faz o agrupamento ao vivo
`fun detect(events: List<ScrollEvent>): List<DetectedSwipe>` = separa por gap > 0,5 s e classifica
cada grupo com `classifyGroup(group): DetectedSwipe?`. Um grupo vira vídeo se todos os eventos são
`ViewPager`, `dx=0`, `dy≠0`; `hesitated` se algum `dy<0`. `RecyclerView` ou `dx≠0` → null (não conta).
`ScrollEvent(className, dy, dx, timestamp, pkg)`. O `AccessibilityService` mantém o grupo corrente e
faz flush quando o gap passa de 0,5 s (usando `classifyGroup`), persistindo os vídeos.
- **Por quê:** a lógica arriscada fica testável em JVM contra os números reais dos logs (G1/G11);
  o service é fino.

### D-2: `behavior_event` no Room — uma linha por deslize detectado
`BehaviorEventEntity(id, timestamp, pkg, surface, hesitated)`. `videos` = nº de linhas; `hesitações`
= linhas com `hesitated`; superfície agrupada. A correlação com episódios é por intervalo de tempo
(no dashboard), sem FK. Schema v3→v4 (migração additiva).
- **Alternativa recusada:** ligar cada evento a um episódio na escrita — acopla as duas fontes; a
  correlação por tempo é suficiente e desacoplada.

### D-3: Totalmente desacoplado do tempo (D13/F5)
O `AccessibilityService` **não** conhece `MonitorService`, `EpisodeEngine`, alarme nem UsageStats.
Só escuta scroll e grava comportamento. Rodam em paralelo, independentes. Se a a11y quebrar por
update do IG/TikTok, o contador de tempo segue intacto (limitação #6).

### D-4: Config do serviço = a validada nos testes
`accessibility_service_config.xml`: `packageNames` = os 2 alvos; `eventTypes` =
`typeViewScrolled|typeWindowStateChanged`; `notificationTimeout=16`; sem `canRetrieveWindowContent`
(capabilities=0). Ignora `TYPE_WINDOW_CONTENT_CHANGED` (2048, 93% de ruído) não assinando.

### D-5: Acessibilidade é OPCIONAL no modelo de permissão (D15)
Adicionar `AppPermission.ACCESSIBILITY` como valor, mas **fora** de `required` (o app não a exige pra
funcionar). `PermissionChecker` ganha a checagem ao vivo (via `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`,
padrão do `resurface_old`).

## Risks / Trade-offs

- **[Fragilidade a updates]** IG/TikTok podem mudar as classes/eventos → detecção quebra. Mitigação:
  é o extra (não o produto); limitação #6 já declarada; o contador de tempo não depende disto.
- **[Volume/bateria]** o IG gera ~23 ev/s deslizando. Mitigação: não assinar `type=2048` (93% do
  volume); agrupar e persistir só o detectado, não cada evento cru.
- **[Restricted settings]** ligar a11y em app sideload exige o passo extra (Android 13+). Mitigação:
  tratado no `onboarding` (change 3); no dev, conceder via adb.
- **[Dois componentes de fundo]** a11y service + FGS. Ambos já validados vivos no aparelho. Sem
  interação entre eles.

## Migration Plan

Room v3→v4: `CREATE TABLE behavior_event (...)`. Additiva, sem perda.

## Open Questions

- Guardar o `dy` bruto pra análise fina (ritmo de varredura, magnitude) ou só o booleano de
  hesitação? Proposta: guardar `hesitated` + `surface` no F1-finish; `dy` cru é fácil de adicionar
  depois se o mestrado precisar da magnitude.
