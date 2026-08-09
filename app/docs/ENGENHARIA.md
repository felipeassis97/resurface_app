# Resurface — Guia de Engenharia

> Como escrever o código deste app. Golden rules, arquitetura, padrões, tema, navegação,
> Compose e uso correto das bibliotecas e das APIs de plataforma.
>
> **Alvo: Android 16 (API 36).** `compileSdk 36.1 · targetSdk 36 · minSdk 36`. Aparelho
> único (Samsung SM-A536E), sideload, não vai pra Play Store — isso **muda regras**
> (ver §10).
>
> Companheiros: `PRODUTO.md` (o quê + decisões D1–D25) · `NEGOCIO.md` (por quê) ·
> `GAPS.md` (o que foi validado no aparelho). Este doc é o **como**.
>
> Padrão de referência: `../resurface_old` (MVVM feature-slice, Hilt, Room, DataStore).
> Fontes oficiais: developer.android.com, lidas em 2026-08-09 (ver §10/§11).

---

## 0. Como usar este doc

Antes de escrever qualquer arquivo, confira as **Golden Rules** (§1) e a seção da camada
em que você está mexendo. Toda PR/commit passa pelo **checklist final** (§13). Quando o
doc e o código do `resurface_old` divergirem, o doc vence (ele já corrige o que aprendemos).

---

## 1. Golden Rules — inegociáveis

Estas vêm antes de qualquer conveniência. Se o código conflita com uma regra, o código muda.

```
G1  Núcleo puro.        Lógica de domínio (EpisodeEngine, AlertPolicy) é Kotlin puro,
                        SEM import android.*. Testável em JVM, sem aparelho.
G2  Dependência desce.  UI → (Domínio) → Dados. Camada de baixo NUNCA conhece a de cima.
                        Repository não sabe que ViewModel existe.
G3  Uma fonte da verdade. Cada dado tem um dono que expõe tipo IMUTÁVEL. Mutação só por
                        função no dono. Estado de permissão é lido AO VIVO do OS, nunca persistido.
G4  UDF.                Estado desce (imutável), evento sobe (lambda). Nada de ViewModel
                        passado pra dentro de composable filho.
G5  Plataforma atrás de interface. UsageStatsManager, AlarmManager, Notifications e afins
                        vivem atrás de uma interface na camada de dados. O resto depende da abstração.
G6  Imutabilidade por padrão. `val`, `data class`, `copy()`, coleções read-only. `var`
                        mutável só dentro de um escopo bem fechado com justificativa.
G7  Só afirma o que mede (P2). O código nunca infere estado mental. Ver `PRODUTO.md` §2.
G8  Sem estado frágil.  Episódio aberto é derivável do UsageStats (D24) — não persistir.
                        Room só guarda episódio FECHADO. Zero SharedPreferences de estado vivo.
G9  Dispatchers injetados. Nada de `Dispatchers.IO` hard-coded em repo/service — injeta,
                        pra testar. Trabalho pesado/IPC fora da main thread.
G10 Respeita o tema.    Nunca `Color(0x...)` cru numa tela. Sempre role do M3 /
                        `MaterialTheme.colorScheme` / `resurfaceColors` / tokens (§7).
G11 Toda implementação tem teste. Nenhuma classe/função de lógica entra sem teste.
                        EXCEÇÃO: UI visual (composables, telas, preview) NÃO se testa —
                        ViewModel/Repository/domínio/mappers SIM. Ver §12.
G12 Todo método tem comentário. 1–2 linhas dizendo o que o método faz (pode ser em
                        português). Vale pra toda função — pública ou privada. Ver §3.1.
```

---

## 2. Arquitetura

### 2.1 Camadas e direção de dependência

```
┌─────────────────────────────────────────────────────────────┐
│ UI          Composable (stateless) ── ViewModel (UiState)    │  ← camada de apresentação
│                                          │ consome           │
│ DOMÍNIO     EpisodeEngine · AlertPolicy · MessageComposer    │  ← Kotlin PURO (G1)
│             (opcional: UseCase se coordenar vários repos)    │
│                                          │ usa               │
│ DADOS       Repository ── DataSource (Room · DataStore ·     │  ← fonte da verdade
│                            UsageStatsReader · AlarmScheduler ·│
│                            Notifier)  ← plataforma atrás de interface (G5)
└─────────────────────────────────────────────────────────────┘
        setas apontam só pra BAIXO (G2)
```

- **ViewModel** mora na UI. Sobrevive a config change. Consome Repository/serviços. Nunca
  guarda dado de app em Activity/Service — esses morrem sem aviso.
- **Repository** é a porta da camada de dados. Expõe `Flow` imutável, centraliza escrita,
  resolve conflito de fontes, esconde os DataSources. Só ele é exposto pra fora.
- **DataSource** = uma origem (Room OU DataStore OU UsageStats). Nunca injetado direto na UI.
- **Domínio** é puro (G1). O `EpisodeEngine` recebe um stream de eventos tipados e devolve
  estado — não sabe o que é `UsageStatsManager`.

### 2.2 Feature-slice — layout de pacotes

Espelha o `resurface_old`. Cada tela é uma fatia com Screen + ViewModel juntos.

```
com.resurface.resurface/
├─ ResurfaceApplication.kt          @HiltAndroidApp
├─ MainActivity.kt                  @AndroidEntryPoint
├─ di/                              módulos Hilt (@Module/@Provides/@Binds)
├─ domain/                          PURO — EpisodeEngine, AlertPolicy, modelos de domínio
│   ├─ model/                       Episode, EpisodeState, UsageEvent, AlertDecision
│   ├─ EpisodeEngine.kt
│   └─ AlertPolicy.kt
├─ data/                            camada de dados
│   ├─ usage/    UsageStatsReader (interface) + impl                 (plataforma, G5)
│   ├─ alarm/    AlarmScheduler (interface) + impl                   (plataforma, G5)
│   ├─ notification/ Notifier (interface) + impl + canais            (plataforma, G5)
│   ├─ episode/  Room: EpisodeEntity, EpisodeDao, ResurfaceDatabase, EpisodeRepository
│   └─ config/   DataStore: ConfigRepository (limite, pausadoAté…)
├─ service/                         MonitorService (FGS), AlarmReceiver, BootReceiver
├─ permission/                      AppPermission, PermissionChecker (live, §3)
└─ ui/
    ├─ ResurfaceApp.kt · MainShell.kt          shell adaptativo
    ├─ navigation/  Destination.kt · ResurfaceNavHost.kt
    ├─ theme/       Color·Type·Shape·Spacing·Motion·Theme
    └─ screens/<feature>/  <Feature>Screen.kt + <Feature>ViewModel.kt
```

Regra: **adicionar uma feature = criar uma pasta em `ui/screens/`**. Não espalhar.

---

## 3. Padrões de código Kotlin

```
· Nomes: Repository = <Tipo>Repository. DataSource = <Tipo><Local|Remote>DataSource
  (nunca pelo impl: "UsagePrefsDataSource" ✗). ViewModel = <Feature>ViewModel.
· KDoc curto que explica o PORQUÊ, não o quê. Segue o tom do resurface_old.
· Imutabilidade (G6): `data class` + `copy()`. Coleções: `List/Set/Map` read-only na API.
· Nada de `!!`. Use `?:`, `requireNotNull` com mensagem, ou modele o nulo no tipo.
· `sealed interface` pra estados finitos (StartRoute, AlertDecision, EpisodeState).
· `Result<T>` pra escrita falível (ver Repository, §5.1). Chamador nunca assume sucesso.
· `enum` com companion `all`/`required` como fonte única de listas (ver AppPermission, Destination).
· Dispatchers injetados (G9): `class X @Inject constructor(@IoDispatcher private val io: CoroutineDispatcher)`.
· Tempo: injete um relógio (`() -> Long` ou `Clock`) no domínio pra testar; nada de
  System.currentTimeMillis() escondido dentro do EpisodeEngine.
```

### 3.1 Comentário por método (G12)

**Toda função — pública ou privada — leva um comentário de 1–2 linhas dizendo o que faz.**
Pode ser em português. Curto, sobre a intenção, não repetir a assinatura.

```kotlin
// ✅ diz o que faz, em 1 linha
/** Traduz um evento cru do UsageStats pro modelo de domínio; null = ruído descartável. */
fun UsageEvents.Event.toDomainOrNull(): UsageEvent? { ... }

/** Reagenda o alarme exato do próximo cruzamento; cancela se pausado ou fora do episódio. */
fun rescheduleAlarm() { ... }

// ❌ sem comentário
fun setLimit(minutes: Int): Result<Unit> { ... }
// ❌ comentário que só repete o nome ("Sets the limit") — não agrega
```

Exceções (não precisam): `override` triviais (`toString`, getters gerados), lambdas,
e composables (que são UI — mas o composable de nível de tela ainda leva 1 linha do que a
tela mostra). Data class e campos não são "método" — não exigem comentário por campo.

---

## 4. Models, entidades e mapeamento

Três tipos de modelo, **nunca misturados**:

| Tipo | Onde | Exemplo | Regra |
|---|---|---|---|
| **Domínio** | `domain/model/` | `UsageEvent`, `Episode`, `EpisodeState` | Puro, imutável, sem anotação de framework. É o que o EpisodeEngine fala. |
| **Entidade Room** | `data/episode/` | `EpisodeEntity` (`@Entity`) | Só persistência. `@PrimaryKey`, `@ColumnInfo`. Nunca vaza pra UI/domínio. |
| **UiState / Row** | junto do ViewModel | `HomeUiState`, `EpisodeRow` | Imutável, com defaults. O que a tela consome. |

**Mapeamento explícito** (parser/mapper), como o `EventMapper` do old:

```kotlin
// data/usage/UsageEventMapper.kt — traduz a API crua pro modelo de domínio.
// Aqui mora TODO o conhecimento sujo do UsageEvents.Event; o domínio fica limpo.
fun UsageEvents.Event.toDomainOrNull(): UsageEvent? = when (eventType) {
    UsageEvents.Event.ACTIVITY_RESUMED -> UsageEvent.Enter(packageName, timeStamp)
    UsageEvents.Event.ACTIVITY_PAUSED  -> UsageEvent.Leave(packageName, timeStamp)
    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> UsageEvent.ScreenOff(timeStamp)
    else -> null   // ruído descartado na fronteira, não no domínio
}
```

```kotlin
// domain/model/UsageEvent.kt — o vocabulário do EpisodeEngine (D13/§5.3 do PRODUTO).
sealed interface UsageEvent {
    val timestamp: Long
    data class Enter(val pkg: String, override val timestamp: Long) : UsageEvent
    data class Leave(val pkg: String, override val timestamp: Long) : UsageEvent
    data class ScreenOff(override val timestamp: Long) : UsageEvent
}
```

Entidade ↔ domínio também por função de extensão (`EpisodeEntity.toDomain()` /
`Episode.toEntity()`), no arquivo da entidade. **Nunca** anote um modelo de domínio com
`@Entity`/`@Serializable` de rota só pra "economizar" — mistura camadas (G2).

---

## 5. Camada de dados

### 5.1 Repository — o padrão (idêntico ao `resurface_old`)

```kotlin
@Singleton
class ConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val LIMIT_MIN = intPreferencesKey("limit_minutes")
        val PAUSED_UNTIL = longPreferencesKey("paused_until")
    }

    /** Expõe Flow IMUTÁVEL com default. Nunca expõe MutableStateFlow/DataStore cru. */
    val limitMinutes: Flow<Int> = dataStore.data.map { it[Keys.LIMIT_MIN] ?: 20 }

    /** Escrita: suspend, atômica dentro de edit{}. Validação no read-modify-write. */
    suspend fun setLimit(minutes: Int): Result<Unit> {
        if (minutes !in 10..60) return Result.failure(IllegalArgumentException("fora de 10–60"))
        dataStore.edit { it[Keys.LIMIT_MIN] = minutes }
        return Result.success(Unit)
    }
}
```

Regras: `@Singleton @Inject constructor`; `object Keys`; leitura via `.data.map` (Flow frio);
escrita `suspend` via `edit{}` (transação); validação e regras atômicas DENTRO do `edit{}`
(read-modify-write, sem corrida); `Result<Unit>` quando pode falhar. A UI **nunca** flippa
otimista — re-emite do `Flow`.

### 5.2 Room — episódios fechados (arquivo permanente, D24/G8)

```kotlin
@Entity(tableName = "episode")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long, val endedAt: Long,
    val accumulatedMs: Long, val apps: String,   // "ig,tt"
    val alertsFired: Int,
)

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episode ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<EpisodeEntity>>          // leitura reativa = Flow
    @Insert suspend fun insert(e: EpisodeEntity)          // escrita = suspend (main-safe)
}

@Database(entities = [EpisodeEntity::class], version = 1)
abstract class ResurfaceDatabase : RoomDatabase() {
    abstract fun episodeDao(): EpisodeDao
}
```

Regras: leitura observável = `Flow` (Room emite sozinho, off-main); escrita = `suspend`;
DB **singleton** (provido por Hilt); bump `version` + `Migration` em toda mudança de schema
(senão crash/perda); `@TypeConverters` pra tipos não suportados. Room 2.8 usa KSP (já no build).

### 5.3 Plataforma atrás de interface (G5) — o ponto central

APIs de Android ficam atrás de interface na `data/`, pra o domínio/ViewModel dependerem da
abstração e os testes usarem fake.

```kotlin
interface UsageStatsReader {
    /** Eventos dos 2 alvos + notificações próprias, na janela [from,to]. Off-main. */
    suspend fun events(from: Long, to: Long): List<UsageEvent>
    fun hasUsageAccess(): Boolean
}

interface AlarmScheduler {
    fun scheduleExact(atElapsedRealtime: Long)   // setExactAndAllowWhileIdle (D22)
    fun cancel()
}

interface Notifier {
    fun ensureChannels()
    fun postAlert(appLabel: String, minutes: Int)   // canal HIGH + 2 botões (G5 validado)
    fun ongoing(minutes: Int): Notification          // FGS fixa, baixa
}
```

O impl (`UsageStatsReaderImpl @Inject constructor(@ApplicationContext ctx, mapper)`) mora ao
lado da interface e é ligado por `@Binds` num módulo Hilt. Detalhes de cada API em §10.

---

## 6. Camada de UI

### 6.1 ViewModel + UiState (padrão do `resurface_old`)

```kotlin
data class HomeUiState(
    val liveMinutes: Int = 0,
    val currentApp: String? = null,
    val pausedToday: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    episodeState: EpisodeStateSource,     // Flow<EpisodeState> do serviço/repo
    config: ConfigRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(episodeState.stream, config.pausedUntil) { ep, pausedUntil ->
            HomeUiState(
                liveMinutes = (ep.accumulatedMs / 60_000).toInt(),
                currentApp = ep.currentApp,
                pausedToday = pausedUntil > now(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onPauseToday() = viewModelScope.launch { config.pauseForToday() }
}
```

Regras:
- Uma `data class UiState` imutável, com defaults. Expõe **`StateFlow`**, nunca `MutableStateFlow`.
- Produção reativa: `combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), initial)`.
  Sempre `initialValue`. `WhileSubscribed(5_000)` sobrevive à troca de config sem vazar.
- Imperativo (quando não é derivado de Flow): `private val _s = MutableStateFlow(...); val s = _s.asStateFlow()`, atualiza com `_s.update { it.copy(...) }`.
- Evento sobe como função (`onPauseToday()`), roda em `viewModelScope`.
- **Sem lógica de UI no ViewModel** (formatação de data, navegação). Sem `Context` (exceto
  `@ApplicationContext` via repo). ViewModel consome serviços, não os cria.

### 6.2 Screen (composable stateless)

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()   // lifecycle-aware!
    HomeContent(state = state, onPauseToday = viewModel::onPauseToday)
}

@Composable
private fun HomeContent(state: HomeUiState, onPauseToday: () -> Unit) { /* ... stateless ... */ }
```

- `collectAsStateWithLifecycle()` (não `collectAsState()`) — pausa coleta quando invisível.
- Separe `Screen` (liga VM) de `Content` (stateless, `@Preview`-ável). Estado desce, lambda sobe (G4).
- `hiltViewModel()` só no nível da tela; nunca passe o ViewModel pra filhos.

### 6.3 Compose — golden rules

```
· Composable é idempotente, sem efeito colateral, roda em qualquer ordem/paralelo, pode
  ser pulado/reiniciado. NADA de IO, escrita em DataStore, ou mutação global no corpo.
· Hoisting: componente stateless recebe (value, onValueChange). Suba o estado ao menor
  ancestral comum que o lê.
· Estabilidade: params estáveis → composable "skippable". Marque @Immutable/@Stable em
  tipos que o Compose não infere. UiState é data class de tipos estáveis.
· remember(keys){ } pra cálculo caro. Lista Lazy: SEMPRE key = { it.id }.
· derivedStateOf SÓ pra afunilar input que muda muito → output grosso. Não pra "$a $b".
· Efeitos colaterais só pelas APIs próprias:
    LaunchedEffect(key)  suspend atado à composição (cancela ao sair, reinicia se key muda)
    rememberCoroutineScope()  disparar de onClick
    DisposableEffect(key)  registrar/desregistrar listener — SEMPRE onDispose{}
    rememberUpdatedState  capturar valor recente sem reiniciar o efeito
    produceState / snapshotFlow  ponte não-Compose ↔ State/Flow
· "LaunchedEffect(Unit)" é cheiro — confirme a intenção.
· Sem backwards write (escrever estado depois de lê-lo na mesma composição = loop infinito).
  Escreva estado só em event handler.
```

---

## 7. Tema e UI

O tema (teal M3 fixo + Fraunces/Plus Jakarta) já está em `ui/theme/`. **Dynamic color é OFF
de propósito.** Use sempre pelos tokens — nunca cor/tamanho cru (G10).

### 7.1 Cor

```kotlin
// ✅ roles do M3
Text(color = MaterialTheme.colorScheme.onSurfaceVariant)
Surface(color = MaterialTheme.colorScheme.surfaceContainer) { }
// ✅ role custom (success = "manteve o foco"), fora do ColorScheme
val ok = resurfaceColors.success
// ❌ NUNCA
Text(color = Color(0xFF006A65))   // cor crua numa tela
```

Respeite os pares on-/container (`primary`/`onPrimary`/`primaryContainer`/`onPrimaryContainer`).
Não pareie fora do par. O aviso ("você está há X min") é conteúdo neutro — `surface`/`onSurface`;
o momento "você escolheu parar" usa `resurfaceColors.success`.

### 7.2 Tipografia

- `MaterialTheme.typography.*`. Display/Headline = Fraunces (nunca < 24sp). Title/Body/Label
  = Plus Jakarta. Fraunces **não tem Medium 500** — não peça.
- Números que "andam" (contador vivo): `ResurfaceTextStyles.statDisplay` / `statBody`
  (tabular, `tnum`, não treme).

### 7.3 Spacing / Shape / Motion

```kotlin
Modifier.padding(Spacing.space4)                 // grid de 8dp; margem de tela: space4 compacto
Card(shape = MaterialTheme.shapes.large) { }     // cards 16dp; sheet/dialog = extraLarge 28dp
ResurfaceShapes.full                              // botões/chips/ponto que respira
// Motion: ResurfaceMotion.tidalSpring() pro contador; NudgeEnter/Exit pro aviso subir.
// Honre reduce-motion: respiração → glow estático; intervenção → cross-fade.
```

### 7.4 Edge-to-edge (obrigatório na 16)

`enableEdgeToEdge()` já está no `MainActivity`. Toda tela trata insets
(`Scaffold`/`NavigationSuiteScaffold` fazem o grosso; conteúdo custom usa
`Modifier.windowInsetsPadding(...)`). Nada de altura de status/nav bar hard-coded.

---

## 8. Navegação (type-safe, Navigation Compose)

- Rotas `@Serializable` — `object` sem-arg, `data class` com-arg. Precisa do plugin de serialization (já no build).
- **`Destination` é a fonte única** (§ do old): o shell itera `Destination.all` pra montar os
  itens; o NavHost registra `composable<T>` pras mesmas rotas. Adicionar destino top-level =
  entrada no enum + `composable<>` no host.
- Navegar com o objeto, ler arg com `toRoute<T>()`:

```kotlin
@Serializable data class EpisodeDetail(val id: Long)

composable<EpisodeDetail> { entry ->
    val route: EpisodeDetail = entry.toRoute()
    EpisodeDetailScreen(id = route.id)
}
// navController.navigate(EpisodeDetail(42))
// no ViewModel: private val args = savedStateHandle.toRoute<EpisodeDetail>()
```

- Sub-telas (ex.: detalhe, ajustes internos) **não** entram no `Destination` (senão viram item
  de navegação). Ficam como rota avulsa alcançada por push, como o `MonitoredAppsRoute` do old.
- Nada de rota string (`"home/42"`) nem parse manual do back-stack.

---

## 9. Bibliotecas

### 9.1 Hilt (DI)

```
· @HiltAndroidApp na Application (uma só). @AndroidEntryPoint em Activity/Service/Receiver.
  Campo injetado NÃO pode ser private.
· ViewModel: @HiltViewModel + @Inject constructor. Na tela: hiltViewModel().
· Ligações: @Inject constructor (preferido) > @Binds (interface→impl, módulo abstract) >
  @Provides (tipos que você não possui, ex. DataStore/Room DB). Todo módulo com @InstallIn.
· Escopo só quando precisa de instância única/estatal/cara: @Singleton, @ViewModelScoped…
· Injetar em Service/BroadcastReceiver: @AndroidEntryPoint funciona no Service; pro
  AlarmReceiver/BootReceiver (BroadcastReceiver) também dá @AndroidEntryPoint. Fora de classe
  Android (ex. código puro chamado por um Worker), use @EntryPoint + EntryPointAccessors.
```

```kotlin
@Module @InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds abstract fun bindUsageReader(impl: UsageStatsReaderImpl): UsageStatsReader
    @Binds abstract fun bindAlarm(impl: AlarmSchedulerImpl): AlarmScheduler
}
```

### 9.2 Coroutines / Flow

```
· Frio & preguiçoso: flow{ } roda por coletor. Ponte de callback: callbackFlow{ trySend; awaitClose{} }.
· flowOn(dispatcher) muda SÓ o upstream. Erro upstream: operador .catch{ }, não try/catch no collect.
· Quente: StateFlow (replay=1, precisa initial → estado), SharedFlow (eventos). Frio→quente:
  stateIn/shareIn(WhileSubscribed(5_000)) pra não duplicar trabalho entre coletores.
· viewModelScope pra tudo do ViewModel (cancela sozinho). Injete dispatchers (G9).
```

### 9.3 DataStore (Preferences)

Um `by preferencesDataStore(name=...)` por arquivo (já em `DataStoreModule`). Leitura reativa
`data.map{}`, escrita atômica `edit{}`, default sempre, `.catch{}` pra `IOException`. Nunca
ler/escrever de dentro de composable — sempre via Repository (§5.1).

### 9.4 WorkManager

Só pra trabalho **adiável e periódico** que tolera Doze (ex.: limpeza/arquivamento, resumos
semanais do F5). **Não** use pra o disparo do aviso (isso é alarme exato, D22) nem pro tick de
manutenção crítico. WorkManager é suspenso em Doze — por isso não serve pro caminho do minuto 20.

---

## 10. APIs de plataforma (Android 16) — padrões corretos

> Fonte: doc oficial (lida 2026-08-09) + validação no aparelho (`GAPS.md`).
> **Nota de distribuição:** sideload/pessoal, NÃO vai pra Play Store. Várias restrições de
> *política* da Play (não técnicas) **não se aplicam** — marcadas com 🏪 abaixo.

### 10.1 UsageStatsManager — o contador (D13)

```
· getSystemService(USAGE_STATS_SERVICE). Use queryEvents(begin,end) — stream de eventos, ms,
  ~1s de frescor (medido, GAPS G2). NÃO use queryUsageStats/aggregate pro cruzamento em tempo
  real (é batelado/atrasado).
· Iterar: enquanto hasNextEvent(); getNextEvent(ev) reusando UM UsageEvents.Event.
· Eventos que importam: ACTIVITY_RESUMED(1)=entrou, ACTIVITY_PAUSED(2)=saiu,
  SCREEN_NON_INTERACTIVE=tela apagou, NOTIFICATION_INTERRUPTION/SEEN=aviso (F7).
  NÃO use MOVE_TO_FOREGROUND/BACKGROUND (deprecados).
· Off-main (IPC binder síncrono).
· Permissão: PACKAGE_USAGE_STATS (acesso especial, NÃO runtime). Concede via
  Settings.ACTION_USAGE_ACCESS_SETTINGS. Checa via AppOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS)
  == MODE_ALLOWED (ver PermissionChecker do old — copiar). checkSelfPermission NÃO funciona aqui.
· Histórico é curto (poucos dias) — persista seu tally (Room). Backfill de 6h no cold-start (D24).
```

### 10.2 AlarmManager — o disparo (D22, validado G1: 12ms em Doze deep)

```
· setExactAndAllowWhileIdle(type, triggerAt, pendingIntent) — o ÚNICO (com setAlarmClock) que
  atravessa Doze. setExact/setWindow são adiados na janela de manutenção.
· Tipo: ELAPSED_REALTIME_WAKEUP (monotônico, imune a mudança de relógio) — combina com o
  "acordar-pra-conferir" que recalcula do UsageStats no disparo. (RTC_WAKEUP se precisar de hora
  de parede.) O probe usou ELAPSED_REALTIME_WAKEUP.
· PendingIntent: FLAG_IMMUTABLE OBRIGATÓRIO (API 31+) | FLAG_UPDATE_CURRENT.
· Rate-limit: *AndAllowWhileIdle dispara no máx ~1×/9min em Doze. Nossos avisos são ≥20min
  (dobra, D18) → OK. NÃO faça loop apertado de re-agendamento nisso.
· Alarme NÃO sobrevive a reboot → re-agenda no BOOT_COMPLETED (D24/G3).
· canScheduleExactAlarms() antes de agendar; trate SecurityException.
· Permissão: USE_EXACT_ALARM (concedida na instalação, não revogável) vs SCHEDULE_EXACT_ALARM
  (usuário concede/revoga). 🏪 A Play RESTRINGE USE_EXACT_ALARM a apps de relógio/calendário —
  MAS como não vamos pra loja, usamos USE_EXACT_ALARM (validado). Se um dia virar caminho C
  (loja), trocar pra SCHEDULE_EXACT_ALARM com fluxo de permissão in-app.
```

### 10.3 Foreground Service `specialUse` (D20, validado G4: sem timeout)

```
· Manifesto: uses-permission FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE;
  <service foregroundServiceType="specialUse"> com
  <property name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" value="justificativa">.
· startForeground(id, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) — o tipo do
  startForeground DEVE bater com o manifesto (senão MissingForegroundServiceTypeException).
· specialUse = SEM timeout de 6h (dataSync/mediaProcessing morrem às 6h; shortService pior).
· onTimeout(startId, fgsType) só é chamado em tipos com limite — não no specialUse; se um dia
  for chamado, stopSelf() em segundos.
· Start de FGS em BACKGROUND é barrado (Android 12+). Inicie de contexto de foreground
  (onboarding/Activity) ou do BootReceiver (contexto permitido + app na allowlist de bateria,
  validado G3). NÃO tente iniciar de um am broadcast/receiver comum.
· 🏪 A Play exige declarar o tipo no Console e revisa specialUse — não se aplica (sideload).
```

### 10.4 Notificações (D7/F3/F7, validado G5: heads-up próprio sobre Reels)

```
· Canal OBRIGATÓRIO (API 26+); sem canal a notificação não aparece. Crie no boot do app
  (idempotente). Importância TRAVA após criar — pra mudar, canal NOVO com id novo.
· IMPORTANCE_HIGH → som + heads-up. IMPORTANCE_DEFAULT NUNCA faz heads-up (medido). O aviso
  do minuto 20 = canal HIGH. A notificação fixa do FGS = IMPORTANCE_LOW/MIN + setOngoing(true).
· setPriority() é no-op em API 26+ (a importância do canal manda). Mantenha só pra pre-26.
· Botões (F7): NotificationCompat.addAction com PendingIntent.getBroadcast, FLAG_IMMUTABLE,
  requestCode ÚNICO por ação (extras não entram na igualdade de Intent — requestCode diferencia).
· POST_NOTIFICATIONS (runtime, API 33+): peça contextual (depois do valor). Negado = fica off
  até reinstalar; a notificação do FGS some da gaveta mas o serviço roda. Cheque
  areNotificationsEnabled() antes de notify().
· NÃO precisa de SYSTEM_ALERT_WINDOW nem full-screen-intent — o heads-up HIGH já cobre o Reels (G5).
```

### 10.5 Doze / bateria (D23)

```
· FGS mantém o processo vivo e fora do App Standby. Alarme exato atravessa o Doze (§10.2).
· Isenção: PowerManager.isIgnoringBatteryOptimizations(pkg);
  Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. 🏪 A Play restringe pedir isso — não
  se aplica (sideload). One UI: a lista "apps que nunca dormem" é separada e manual (D23).
· Teste: adb dumpsys deviceidle force-idle / unforce; battery unplug / reset (ver GAPS).
```

---

## 11. Android 16 / targetSdk 36 — checklist de comportamento

```
[ ] Edge-to-edge OBRIGATÓRIO (sem opt-out): toda tela trata insets (§7.4).
[ ] Predictive back ligado por padrão: navegação compatível; não depender de onBackPressed().
[ ] Orientação/resizability ignorados em ≥600dp: telas se comportam em qualquer tamanho.
[ ] Safer intents: intents explícitos batendo com filtro; nossos PendingIntent/receiver são
    explícitos (class alvo) — OK.
[ ] scheduleAtFixedRate roda no máx 1 execução perdida ao voltar — não afeta nosso alarme.
[ ] FGS + JobScheduler concorrentes respeitam quota do job — só importa se usarmos Job junto do FGS.
    (Não usamos: o disparo é alarme, o tick é do próprio serviço.)
```

Nada disso quebra a arquitetura validada. Detalhe: `behavior-changes-16`.

---

## 12. Testes

**G11 — toda implementação tem teste. Nada de lógica entra sem teste.** UI visual é a única
exceção. Regra do que testar:

```
TESTA (obrigatório)                          NÃO TESTA
──────────────────────────────────          ──────────────────────────────
· domínio: EpisodeEngine, AlertPolicy,       · composable / tela / @Preview
  MessageComposer, mappers/parsers           · aparência (cor, layout, pixel)
· Repository (lógica, validação, Result)     · FGS/Service ao vivo (validado no
· ViewModel (UiState derivado, eventos)        aparelho via probe, GAPS)
· qualquer função de lógica pura
```

Como:
```
· Núcleo puro (G1) = TDD em JVM. EpisodeEngine e AlertPolicy testados contra as fixtures reais
  em app/docs/logs/ — incluindo o golden test do episódio 18:43 (PRODUTO §5.5): alimenta os
  eventos → espera 1 episódio de 18:43, aviso dispararia no limite.
· Escreve o teste ANTES do impl (TDD). Relógio e dispatchers injetados → determinístico, sem sleep.
· Flow/StateFlow: Turbine (flow.test { awaitItem() }) + runTest + dispatcher de teste.
  StateFlow com WhileSubscribed só roda upstream com coletor — o teste precisa coletar.
· ViewModel: injeta fakes dos repos, coleta uiState com Turbine, verifica o UiState por evento.
· Fakes das interfaces de plataforma (§5.3): FakeUsageStatsReader devolve listas de UsageEvent;
  o EngineTest nunca toca no Android.
· Nome: <Classe>Test em src/test/. Um caso = um comportamento, nome descritivo do que verifica.
```

---

## 13. Checklist final (toda mudança)

```
[ ] Domínio novo é Kotlin puro? (sem import android.*)                              G1
[ ] Dependências só descem? (data não conhece ui)                                   G2
[ ] Tipo exposto é imutável? Estado de permissão lido ao vivo?                       G3/G6
[ ] Estado desce / evento sobe? ViewModel fora dos filhos?                           G4
[ ] API de plataforma atrás de interface na data/?                                   G5
[ ] Dispatcher/relógio injetados, não hard-coded?                                    G9
[ ] Cor/tipo/spacing por token, nunca cru?                                           G10
[ ] StateFlow (não Mutable) + collectAsStateWithLifecycle?                           §6
[ ] Escrita de repo é suspend/edit{} atômico com Result?                             §5.1
[ ] PendingIntent com FLAG_IMMUTABLE + requestCode único?                            §10.4
[ ] Rota @Serializable, registrada no Destination + NavHost?                         §8
[ ] Toda lógica nova tem teste? (UI visual isenta)                                   G11/§12
[ ] Todo método tem comentário de 1–2 linhas?                                        G12/§3.1
[ ] Teste do núcleo cobre o caso (fixtures/golden) antes do impl?                    §12
```

---

*Última atualização: 2026-08-09. Baseado em: doc oficial Android (§10/§11), padrões do
`resurface_old`, e a validação de plataforma do `GAPS.md`.*
