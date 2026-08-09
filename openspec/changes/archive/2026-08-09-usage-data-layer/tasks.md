## 1. Infra de dispatchers (G9)

- [x] 1.1 Criar `di/IoDispatcher.kt` — `@Qualifier annotation class IoDispatcher`
- [x] 1.2 Criar `di/DispatchersModule.kt` — `@Provides @IoDispatcher` devolvendo `Dispatchers.IO`

## 2. Leitura de uso (usage-reading, test-first)

- [x] 2.1 Escrever `UsageEventMapperTest` — RESUMED→Enter, PAUSED→Leave, SCREEN_NON_INTERACTIVE→ScreenOff; app não-alvo e tipo irrelevante → null
- [x] 2.2 Implementar `data/usage/UsageEventMapper.kt` — função pura `map(type, pkg, ts, targets): UsageEvent?`; comentário por método (G12)
- [x] 2.3 Criar `data/usage/UsageStatsReader.kt` — interface `suspend fun events(from,to): List<UsageEvent>` + `fun hasUsageAccess(): Boolean`
- [x] 2.4 Implementar `UsageStatsReaderImpl` — `queryEvents` filtrado nos alvos, ordena por ts, mapeia, roda no `@IoDispatcher`; `hasUsageAccess()` via `AppOpsManager.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS)` ao vivo (G3)
- [x] 2.5 Criar `permission/AppPermission.kt` + `permission/PermissionChecker.kt` — acesso ao uso + notificações, status ao vivo (adaptado do resurface_old)

## 3. Armazenamento de episódios (episode-storage, test-first)

- [x] 3.1 Criar `data/episode/EpisodeEntity.kt` — `@Entity` (id, startedAt, endedAt, accumulatedMs, apps:String) + mapeamento `toDomain()/toEntity()` (apps por vírgula, D-5)
- [x] 3.2 Criar `data/episode/EpisodeDao.kt` — `@Insert suspend` + `@Query ... Flow<List<EpisodeEntity>>` (mais recente primeiro)
- [x] 3.3 Criar `data/episode/ResurfaceDatabase.kt` — `@Database(version=1)`
- [x] 3.4 Escrever `EpisodeRepositoryTest` — com **fake in-memory** do `EpisodeDao`: arquivar vira linha; histórico re-emite; mapeamento ida-e-volta preserva os campos
- [x] 3.5 Implementar `data/episode/EpisodeRepository.kt` — `suspend fun archive(ClosedEpisode)` + `val history: Flow<List<ClosedEpisode>>`; `@IoDispatcher`; comentário por método (G12)

## 4. Config (config-store, test-first)

- [x] 4.1 Criar `data/config/MidnightClock.kt` (ou helper) — próxima meia-noite com relógio+zona injetados; teste puro da virada
- [x] 4.2 Escrever `ConfigRepositoryTest` — limite padrão 20; grava 30 lê 30; rejeita 5 (Result.failure, valor intacto); pausar ativo até meia-noite; expira na virada. DataStore temporário (`@TempDir` + `runTest`); se não rodar em JVM, mover pra androidTest (D-2/riscos)
- [x] 4.3 Implementar `data/config/ConfigRepository.kt` — limite (valida 10–60, `Result`), pausar por hoje (marco de meia-noite), `val config: Flow<Config>`; comentário por método (G12)

## 5. Hilt wiring + fechamento

- [x] 5.1 Criar `di/DatabaseModule.kt` — `@Provides @Singleton` do `ResurfaceDatabase` (Room.databaseBuilder) e do `EpisodeDao`
- [x] 5.2 Criar/atualizar `di/DataModule.kt` — `@Binds` de `UsageStatsReader`→impl
- [x] 5.3 `./gradlew :app:testDebugUnitTest` inteiro verde; `compileDebugKotlin` limpo
- [x] 5.4 Checklist §13 do `ENGENHARIA.md` sobre os arquivos novos (G5/G9/G11/G12); grep `Dispatchers.` fora dos módulos
