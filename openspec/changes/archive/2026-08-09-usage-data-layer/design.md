## Context

Segue o `episode-core`. Constrói a camada `data/` que alimenta e persiste o domínio, sob as
regras do `ENGENHARIA.md` (G5 plataforma atrás de interface, G9 dispatcher injetado, G11 teste,
G3 permissão ao vivo). As APIs de plataforma já foram validadas no aparelho (`GAPS.md`): a
freshness de ~1 s do `queryEvents` (G2) e o padrão de detecção de permissão (G7).

## Goals / Non-Goals

**Goals:**
- `UsageStatsReader` (interface) + impl que lê `queryEvents` filtrado nos 2 alvos, fora da main.
- `UsageEventMapper` **puro** que traduz o evento cru → `UsageEvent` e descarta ruído.
- `EpisodeRepository` (Room) que arquiva `ClosedEpisode` e expõe histórico como `Flow`.
- `ConfigRepository` (DataStore) com limite (10–60) e "pausar por hoje" até a meia-noite.
- Detecção ao vivo do acesso ao uso via `AppOpsManager`.
- Testabilidade sem Robolectric (ver decisões).

**Non-Goals:**
- Alarme, notificação, FGS (change `monitor-service`).
- UI / ViewModel. Onboarding. Contagem de vídeos (acessibilidade).
- Migração de schema (é o v1, versão 1 do banco).

## Decisions

### D-1: Plataforma atrás de interface, mapper puro separado do impl
`interface UsageStatsReader { suspend fun events(from,to): List<UsageEvent>; fun hasUsageAccess(): Boolean }`.
O impl (`UsageStatsReaderImpl @Inject constructor(@ApplicationContext ctx, mapper, @IoDispatcher io)`)
faz o IPC; o `UsageEventMapper` (função pura) faz a tradução.
- **Por quê:** a lógica suja de tradução fica testável em JVM (G11); o motor nunca vê Android (G1/G5).

### D-2: Testar repos com FAKES, não Robolectric
- `UsageEventMapper`: JUnit puro.
- `EpisodeRepository`: o `EpisodeDao` é interface → teste usa um **fake in-memory** do DAO. A
  lógica testada é o mapeamento `EpisodeEntity ↔ ClosedEpisode` e o comportamento do repositório.
- `ConfigRepository`: teste com um `DataStore<Preferences>` **temporário** (`PreferenceDataStoreFactory`
  em `@TempDir` + `runTest`), sem Android runtime.
- **Por quê:** mantém os testes rápidos e em JVM. O impl real do DAO é responsabilidade do Room
  (não re-testamos o framework). **Alternativa recusada:** Robolectric — mais lento, mais dependência.

### D-3: `IoDispatcher` injetado via qualifier Hilt
Novo `@Qualifier IoDispatcher` + módulo `DispatchersModule` provendo `Dispatchers.IO`. Repos e o
reader recebem o dispatcher, nunca o hard-codam (G9).
- **Por quê:** teste determinístico com `StandardTestDispatcher`.

### D-4: `queryEvents` filtrado, sem agregados; janela por parâmetro
O reader usa `queryEvents(from,to)` e, no Android 16, pode usar o `filter_based_event_query_api`
pra pedir só os 2 packages e os tipos que interessam. O chamador passa a janela (6 h no
cold-start, curto no tick). NUNCA `queryUsageStats` agregado pro tempo real (GAPS G2).
- **Por quê:** frescor de ~1 s medido; agregado é batelado.

### D-5: Episódio no Room — apps como string simples
`EpisodeEntity(id, startedAt, endedAt, accumulatedMs, apps: String)` onde `apps` = pacotes
juntados por vírgula (2 apps, conjunto pequeno). Mapeamento `Set<String> ↔ String` no boundary.
- **Por quê:** evita tabela de junção pra um conjunto de 2. Simples, suficiente pro F1.
- **Alternativa recusada:** `@TypeConverter` de `Set` — overkill agora.

### D-6: "Pausar por hoje" = marco de meia-noite + relógio injetado
Grava `pausedUntilMillis` = próxima meia-noite ao ativar. `isPausedToday(now)` = `pausedUntil > now`.
A meia-noite é calculada com o fuso do dispositivo, num helper com relógio injetado.
- **Por quê:** puro o suficiente pra testar a expiração sem esperar o dia virar. Casa com a
  `AlertPolicy` que recebe `pausedToday` booleano (D24 do `episode-core`).

## Risks / Trade-offs

- **[DataStore em teste JVM]** `datastore-preferences` (Android) x `-core` (JVM). Mitigação: usar a
  factory de core com arquivo temporário; se não rodar em JVM puro, mover o teste do ConfigRepository
  pra `androidTest`. Decidir na task ao escrever o primeiro teste.
- **[Ordem/frescor do queryEvents]** eventos podem vir com leve atraso ou empatados em ms.
  Mitigação: o reader ordena por timestamp; o motor já trata `Enter` sem `Leave` (episode-core).
- **[Meia-noite/fuso]** virada de dia depende do fuso e de horário de verão. Mitigação: helper
  isolado com relógio+zona injetados e teste explícito da virada.

## Migration Plan

Banco Room nasce na versão 1 — sem migração. Se o schema mudar depois, bump + `Migration`.

## Open Questions

- **Q — eventos de notificação própria (F7):** o reader vai precisar expor
  `NOTIFICATION_INTERRUPTION`/`SEEN` do próprio app pro registro de outcome. Fica pro
  `monitor-service` (F7), não aqui — este change lê só o que alimenta o contador.
- Módulo Hilt: um `DataModule` só, ou um por sub-pacote? Proposta: um `DataModule` com os `@Binds`
  + um `DatabaseModule` com o `@Provides` do Room. Decidir ao escrever.
