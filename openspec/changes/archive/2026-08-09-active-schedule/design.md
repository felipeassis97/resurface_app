## Context

O disparo do aviso passa por duas peças puras: `AlertPolicy.decide` (avisar agora?) e
`AlarmPlanner.nextFireDelayMs` (quando agendar). Ambas já recebem guardas como `pausedToday`
e `todayAlertCount`. A janela ativa é uma guarda a mais, do mesmo formato, mais uma peça de
agendamento pra "acordar" quando a janela abre. A medição (UsageReader/EpisodeEngine) não é
tocada — a janela só condiciona o nudge (Opção B, escolhida pelo dono).

## Goals / Non-Goals

**Goals:**
- Usuário escolhe dias da semana + faixa de horário em que aceita avisos (allow-list).
- Fora da janela: nenhum aviso; medição segue 24h.
- Episódio que entra na janela dispara na abertura, sem perder o momento.
- Janela vazia = sempre ativo (retrocompatível). Convive com "pausar por hoje".

**Non-Goals:**
- Faixas diferentes por dia (seg 18–23 ≠ sáb 10–14). MVP: uma faixa aplicada aos dias marcados.
- Pausar/alterar a contagem fora da janela.
- Múltiplas faixas por dia. Uma faixa contínua (pode cruzar meia-noite).

## Decisions

**D-1 — Modelo `Schedule` mínimo.**
`Schedule(days: Set<DayOfWeek>, startMinute: Int, endMinute: Int)` — `startMinute`/`endMinute`
são minutos do dia (0–1439). Vazio = `days` vazio → sempre ativo. Uma única faixa aplicada a
todos os dias marcados (Q1: "dia + faixa basta"). Puro, no domínio.

**D-2 — `ScheduleGate` puro, com duas funções.**
- `isActive(schedule, now, zone): Boolean` — resolve dia da semana + minuto do dia no fuso do
  device. Vazio → true. Faixa normal (`start < end`): ativo se `start ≤ m < end` no dia certo.
  Faixa que cruza meia-noite (`start > end`): ativo se `m ≥ start` (dia de início) **ou**
  `m < end` (madrugada seguinte). Testável sem relógio real.
- `nextOpening(schedule, now, zone): Long?` — próximo instante em que `isActive` vira true;
  null se sempre ativo (não precisa acordar). Usado pelo planner.

**D-3 — Guarda na política, agendamento no planner.**
`AlertPolicy.decide` ganha `isActiveNow: Boolean`; se false → Hold (junto de pausa/teto). O
`AlarmPlanner.nextFireDelayMs`: se DENTRO mas fora da janela, `delay = nextOpening - now`
(agenda a abertura); se dentro da janela, lógica atual. As duas peças recebem o resultado do
gate já resolvido — o gate roda no `AlertEvaluator` (que tem o relógio e o fuso), mantendo as
puras sem dependência de tempo real (segue o padrão de `pausedToday`).

**D-4 — Persistência compacta no DataStore.**
`Config` ganha `schedule`. `ConfigRepository`: nova chave string, serialização simples
(ex.: `"MON,TUE,WED|1290|60"` = dias + startMin + endMin). Expõe `schedule: Flow<Schedule>`
e `setSchedule(Schedule)`. Sem migração de banco (é DataStore, não Room).

**D-5 — Compõe com pausar-hoje.**
Ambas são guardas independentes que resultam em Hold. Ordem não importa: `decide` segura se
`pausedToday || !isActiveNow || teto`. Pausa = exceção de um dia; janela = regra recorrente.

**D-6 — UI: chips de dia + dois horários.**
Seção nova em Ajustes: `FilterChip` por dia da semana (reusa o padrão de tom/hobby) + dois
seletores de horário (início/fim). Sem dia marcado = "sempre ativo" explícito na UI.

## Risks / Trade-offs

- **Cruzamento de meia-noite** é a fonte clássica de bug. Mitigado: `ScheduleGate` puro com
  testes cobrindo faixa normal, faixa que cruza, e limites exatos (start, end).
- **DST / mudança de fuso.** Usa o fuso do device e minuto-do-dia; transições de horário de
  verão são raras e no máximo deslocam a janela em 1h no dia da virada. Aceito pro escopo.
- **Acordar na abertura da janela** depende do alarme agendado sobreviver ao Doze — mesmo
  mecanismo já validado (`setExactAndAllowWhileIdle`, D20/D24). Sem novo risco de plataforma.
- **Burlar a pesquisa** ligando a janela só quando não usa. Aceito: é ferramenta pessoal de
  auto-observação; a janela auto-autorada é o recurso, não um furo.
- **Divergência entre `decide` e o planner** se só um checar a janela. Mitigado: o gate é
  calculado uma vez no `AlertEvaluator` e passado às duas peças, como já é feito com pausa.
