## 1. Domínio: modelo + gate (puro)

- [x] 1.1 `Schedule` (domain/model): `days: Set<DayOfWeek>`, `startMinute: Int`, `endMinute: Int`.
      Default vazio (sempre ativo). Comentário G12.
- [x] 1.2 `ScheduleGate` (domain): `isActive(schedule, now, zone): Boolean` — vazio→true, faixa
      normal, faixa que cruza meia-noite. Puro.
- [x] 1.3 `ScheduleGate.nextOpening(schedule, now, zone): Long?` — próximo instante ativo; null
      se sempre ativo.
- [x] 1.4 Adicionar `schedule` ao `Config` (default vazio).

## 2. Política + planner (guarda)

- [x] 2.1 `AlertPolicy.decide(...)`: novo param `isActiveNow: Boolean`; `!isActiveNow` → Hold
      (junto de pausa/teto).
- [x] 2.2 `AlarmPlanner.nextFireDelayMs(...)`: novos params `isActiveNow`, `nextOpeningDelayMs`;
      se DENTRO e fora da janela → agenda a abertura; senão, lógica atual.

## 3. Persistência

- [x] 3.1 `ConfigRepository`: nova chave string; `schedule: Flow<Schedule>`, `setSchedule(...)`,
      serialização compacta (dias|startMin|endMin). Montar no `config: Flow<Config>`.

## 4. Integração no AlertEvaluator

- [x] 4.1 Em `AlertEvaluator`: ler `schedule`, calcular `isActiveNow = ScheduleGate.isActive(...)`
      e `nextOpening` no `now`/fuso do device; passar para `policy.decide` e `planner`.
- [x] 4.2 Confirmar que a contagem/episódio não muda (janela só afeta disparo/agendamento).

## 5. UI (Ajustes)

- [x] 5.1 `SettingsViewModel`: expor `schedule` no UiState + `onSetDays(...)`/`onSetWindow(...)`.
- [x] 5.2 `SettingsScreen`: seção "Quando quero ser avisado" — `FilterChip` por dia da semana +
      dois seletores de horário (início/fim). Sem dia = "sempre ativo". Tokens de tema (G10).

## 6. Testes e verificação

- [x] 6.1 `ScheduleGate` (G11): vazio=sempre ativo; faixa normal (dentro/fora/limites); faixa
      que cruza meia-noite; `nextOpening` em cada caso.
- [x] 6.2 `AlertPolicy`: fora da janela → Hold; dentro + pausa → Hold; dentro + sem pausa → Fire.
- [x] 6.3 `AlarmPlanner`: fora da janela agenda a abertura; dentro agenda o cruzamento do limite.
- [x] 6.4 `ConfigRepository`: grava e lê a janela (round-trip da serialização).
- [x] 6.5 `./gradlew :app:testDebugUnitTest` verde.
- [x] 6.6 Device: verificado UI + persistência (seção renderiza, empty state, toggle→sliders,
      round-trip DataStore "MONDAY,WEDNESDAY|1080|1380", toggle-off→empty). NOTA: supressão em si
      coberta por unit test (gate/policy/planner); o botão de teste bypassa a AlertPolicy por
      design (D-1), então não demonstra supressão pelo device.
