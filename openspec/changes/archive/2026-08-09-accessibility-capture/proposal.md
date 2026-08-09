## Why

O app é a ferramenta do mestrado do autor: ele usa redes normalmente e acompanha a própria
evolução. O contador de tempo já roda, mas o dado mais rico — **quantos vídeos**, **hesitação**
(deslize iniciado e revertido), **Reels vs feed** — precisa do serviço de acessibilidade (D15/F5).
As regras de detecção já foram **validadas no aparelho** (`REELS.md`/`TIKTOK.md`: 16 vídeos à mão =
16 detectados, erro zero; hesitação com assinatura própria; classes distinguem superfície). Isto
liga essa fonte, de forma **puramente aditiva** — sem tocar no contador de tempo (D13).

## What Changes

- Novo `AccessibilityService` filtrado nos 2 alvos, escutando `TYPE_VIEW_SCROLLED` (4096) e
  `TYPE_WINDOW_STATE_CHANGED` (32); descarta o ruído (`TYPE_WINDOW_CONTENT_CHANGED` = 93%).
- `SwipeDetector` **puro**: aplica a regra validada — vídeo = grupo de `type=4096` `class=ViewPager`
  `dy≠0` `dx=0`, agrupado por gap > 0,5 s; hesitação = `dy` negativo na rajada; superfície =
  `ViewPager` (Reels) vs `RecyclerView` (feed/comentários); `dx≠0` = navegação lateral (não conta).
- Grava **eventos de comportamento** no Room (timestamp, tipo: vídeo/hesitação, superfície, package) —
  o Insights correlaciona por tempo com os episódios depois. NÃO altera o tempo/episódio (D13/F5).
- Detecção da permissão de acessibilidade ao vivo (já parcialmente no `PermissionChecker`, estender).
- Manifesto: declara o `<service>` de acessibilidade + o `accessibility_service_config.xml`.
- Testes: `SwipeDetector` (puro, contra as assinaturas reais dos logs) + `BehaviorRepository` (fake DAO).

## Capabilities

### New Capabilities
- `behavior-capture`: detectar e registrar o comportamento de rolagem nos apps-alvo via
  acessibilidade — contagem de vídeos, hesitação (deslize revertido) e superfície (Reels vs feed) —
  como fonte OPCIONAL e aditiva, sem afetar a contagem de tempo.

### Modified Capabilities
<!-- Nenhuma — o contador de tempo (episode-tracking) não muda; esta é uma fonte separada (D13/F5). -->

## Impact

- **Código novo:** `service/ResurfaceAccessibilityService`, `domain/SwipeDetector` (puro),
  `data/behavior/` (Room: `BehaviorEventEntity`/Dao/`BehaviorRepository`), extensão do
  `PermissionChecker` + `AppPermission` (ACCESSIBILITY), `res/xml/accessibility_service_config.xml`.
- **Room:** nova tabela `behavior_event` → schema v3→v4 (migração additiva).
- **Manifesto:** `<service>` `BIND_ACCESSIBILITY_SERVICE` + intent-filter + meta-data de config.
- **Consome:** os modelos de domínio existentes; produz dado que o `insights-dashboard` vai ler.
- **Não afeta** o contador de tempo, o aviso, nem o outcome — é aditivo e desligável (D15).
- **Fragilidade declarada:** updates do IG/TikTok podem quebrar a detecção de comportamento (não o
  tempo). Aceitável — é o extra (limitação #6 do `PRODUTO.md`).
