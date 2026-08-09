## Why

O `episode-core` deu o domínio puro, mas ele só sabe processar `UsageEvent`. Nada ainda **lê**
o `UsageStatsManager`, **traduz** os eventos crus pro domínio, **guarda** os episódios fechados,
nem **lê a config** (limite, pausar por hoje). Este change constrói a camada de dados que
alimenta e persiste o domínio — o degrau entre a lógica testada e o serviço que a orquestra.

## What Changes

- Novo pacote `data/usage/`: interface `UsageStatsReader` (contrato, G5) + impl que envolve
  `UsageStatsManager.queryEvents`, filtrando os 2 alvos, e `UsageEventMapper` que traduz
  `UsageEvents.Event` cru → `UsageEvent` do domínio (descartando ruído na fronteira).
- Detecção **ao vivo** da permissão de acesso ao uso via `AppOpsManager` (nunca persistida, G3).
- Novo pacote `data/episode/`: Room (`EpisodeEntity`, `EpisodeDao`, `ResurfaceDatabase`) +
  `EpisodeRepository` que arquiva `ClosedEpisode` e expõe o histórico como `Flow` (D24: arquivo permanente).
- Novo pacote `data/config/`: `ConfigRepository` (DataStore) — limite (validado 10–60),
  "pausar por hoje" (até a meia-noite), montando o `Config` do domínio.
- Módulos Hilt ligando as interfaces (`@Binds`) e provendo o banco Room (`@Provides`).
- Testes: `UsageEventMapper` (JUnit puro), `EpisodeRepository` (Room in-memory),
  `ConfigRepository` (fake/temp DataStore) — conforme G11.
- Nenhuma UI, nenhum FGS, nenhum alarme/notificação (próxima change). Sem tocar no `episode-core`.

## Capabilities

### New Capabilities
- `usage-reading`: ler os eventos de primeiro plano dos apps-alvo do `UsageStatsManager`,
  traduzi-los para o modelo de domínio, e detectar se a permissão de acesso ao uso está concedida.
- `episode-storage`: arquivar episódios fechados no dispositivo e expor o histórico —
  retenção ilimitada por arquitetura (D24), independente da janela curta da API.
- `config-store`: persistir e ler a configuração do usuário (limite de minutos e "pausar por hoje"),
  entregando o `Config` do domínio.

### Modified Capabilities
<!-- Nenhuma — o episode-core não muda; esta camada o consome. -->

## Impact

- **Código novo:** `data/usage/**`, `data/episode/**`, `data/config/**`, `permission/**`,
  `di/` (módulos), e testes correspondentes em `src/test/`.
- **Dependências:** Room + DataStore + Hilt (já no build). Novo: leitura do `UsageStatsManager`
  (permissão `PACKAGE_USAGE_STATS` já declarada no manifesto).
- **Consome** os modelos do `episode-core` (`UsageEvent`, `ClosedEpisode`, `Config`).
- **Habilita** o `monitor-service`: o FGS vai injetar `UsageStatsReader` + `EpisodeRepository`
  + `ConfigRepository`, rodar o `EpisodeEngine`, e (na change seguinte) disparar via Alarm/Notifier.
- **Android 16:** `queryEvents` é a fonte de tempo real (GAPS G2, ~1 s fresco); nada de agregados.
