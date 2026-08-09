## ADDED Requirements

### Requirement: Ler eventos de primeiro plano dos apps-alvo
O sistema SHALL consultar o `UsageStatsManager` por eventos numa janela `[de, até]` e
devolver os `UsageEvent` de domínio dos apps-alvo, fora da main thread. A consulta SHALL usar
o stream de eventos (`queryEvents`), NÃO estatísticas agregadas, pois só o stream é fresco em
tempo real (GAPS G2).

#### Scenario: Consulta devolve eventos dos alvos
- **WHEN** há um `ACTIVITY_RESUMED` do Instagram e um `ACTIVITY_PAUSED` do TikTok na janela consultada
- **THEN** o reader devolve um `UsageEvent.Enter(instagram)` e um `UsageEvent.Leave(tiktok)` com os timestamps corretos

#### Scenario: Ordena por tempo
- **WHEN** os eventos chegam da API
- **THEN** o reader os devolve em ordem crescente de timestamp (o motor assume ordem)

### Requirement: Traduzir eventos crus e descartar ruído na fronteira
O `UsageEventMapper` SHALL traduzir `ACTIVITY_RESUMED` → `Enter`, `ACTIVITY_PAUSED` → `Leave` e
`SCREEN_NON_INTERACTIVE` → `ScreenOff`. Eventos irrelevantes ou de apps não-alvo SHALL ser
descartados na fronteira (retornando null), para o domínio nunca ver ruído (G1).

#### Scenario: Evento de app não-alvo é descartado
- **WHEN** chega um `ACTIVITY_RESUMED` de um app fora da lista de alvos
- **THEN** o mapper retorna null e o evento não entra no stream de domínio

#### Scenario: Tipo irrelevante é descartado
- **WHEN** chega um evento de um tipo que não é RESUMED, PAUSED nem SCREEN_NON_INTERACTIVE
- **THEN** o mapper retorna null

### Requirement: Detectar a permissão de acesso ao uso ao vivo
O sistema SHALL reportar se a permissão de acesso ao uso está concedida, lendo o estado
**ao vivo** do OS via `AppOpsManager` (`OPSTR_GET_USAGE_STATS`), nunca de um flag persistido (G3),
porque o usuário pode revogar nas configurações a qualquer momento.

#### Scenario: Concedida
- **WHEN** o app tem acesso ao uso concedido no sistema
- **THEN** o reader reporta `hasUsageAccess() == true`

#### Scenario: Revogada fora do app
- **WHEN** o usuário revoga o acesso ao uso nas configurações do sistema
- **THEN** a próxima chamada a `hasUsageAccess()` reporta `false` sem precisar reabrir o app
