## Why

O app hoje avisa a qualquer hora que o uso cruze o limite. Mas o objetivo nunca foi punir
— rede social é legítima; o problema é o uso **descontrolado**. Deixar o usuário escolher
**quando** quer ser cutucado transforma o app de fiscal em guarda-costas que ele mesmo
contratou (reforça D9: o humano põe as regras, a IA nunca decide SE avisa).

Exemplo: "me avise em dias de semana à noite, quando eu tendo a rolar sem parar — não no
sábado de manhã, que é meu tempo tranquilo." Sem isso, todo aviso fora de hora vira ruído
e mina a confiança na ferramenta.

## What Changes

- Novo conceito **janela ativa** (allow-list): o usuário marca os **dias da semana** e uma
  **faixa de horário** em que aceita ser avisado.
- **Conta sempre, só não cutuca** (Opção B): o contador e a medição continuam 24h — o dado
  de pesquisa nunca para. A janela governa **só o disparo do aviso**, não a contagem.
- Fora da janela: nenhum aviso dispara. Se um episódio começa fora e entra na janela, o
  alarme é agendado para a **abertura** da janela (não perde o momento).
- **Janela vazia = sempre ativo** (retrocompatível: quem não configurar nada segue como hoje).
- **Convive com "pausar por hoje" (D11)**: pausa = exceção pontual de um dia; janela = regra
  recorrente. Ambos silenciam o aviso; qualquer um dos dois em Hold segura o disparo.
- Editor da janela na tela de Ajustes: chips de dia da semana + dois campos de horário
  (início/fim). Suporta faixa que cruza a meia-noite (ex.: 22h–01h).

## Capabilities

### New Capabilities
- `active-schedule`: a janela ativa — modelo de agenda (dias + faixa), semântica allow-list,
  regra "conta sempre/cutuca só na janela", persistência e o editor na UI.

### Modified Capabilities
- `alert-policy`: a decisão de disparar passa a segurar (Hold) quando o instante está **fora**
  da janela ativa, junto das guardas existentes (pausa, teto diário).

## Impact

- **Domínio (puro, novo):**
  - `Schedule` (model): dias da semana ativos + faixa `[inícioMin, fimMin]` do dia.
  - `ScheduleGate`: `isActive(schedule, now, zone)` — trata janela vazia (sempre ativo) e
    cruzamento de meia-noite; `nextOpening(schedule, now, zone)` para agendar a abertura.
- **Domínio (alterado):**
  - `AlertPolicy.decide(...)`: +guarda "fora da janela → Hold".
  - `AlarmPlanner.nextFireDelayMs(...)`: se fora da janela mas DENTRO, agenda para a próxima
    abertura; senão, lógica atual.
- **Dados (alterado):** `Config` ganha `schedule`; `ConfigRepository` persiste (nova chave
  DataStore, serialização compacta) e expõe `setSchedule(...)`.
- **UI (alterado):** `SettingsScreen`/`SettingsViewModel` — seção "Quando quero ser avisado"
  (chips de dia + horários).
- **Sem** alteração no motor de contagem, episódio, `UsageStatsReader` ou banco (a janela não
  toca a medição). **Sem** dep externa.
