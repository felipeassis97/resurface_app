# alert-delivery Specification

## Purpose
TBD - created by archiving change monitor-service. Update Purpose after archive.
## Requirements
### Requirement: Agendar o alarme exato no instante do cruzamento
Quando o episódio está DENTRO, não pausado e abaixo do teto diário, o sistema SHALL calcular o
instante em que o acumulado cruzará o limite atual (`limite × 2^avisosDoEpisódio`) e agendar um
`setExactAndAllowWhileIdle` para esse instante (D22). Esse cálculo (`AlarmPlanner`) SHALL ser puro
e testável.

#### Scenario: Calcula o tempo até o cruzamento
- **WHEN** o acumulado está em 12 min, o limite é 20 e nenhum aviso saiu no episódio
- **THEN** o planner devolve um disparo em +8 min (o restante até 20)

#### Scenario: Não agenda quando pausado ou no teto
- **WHEN** "pausar por hoje" está ativo, ou o teto de 6 avisos foi batido, ou o estado não é DENTRO
- **THEN** o planner não devolve nenhum disparo (e o alarme pendente é cancelado)

### Requirement: Acordar-pra-conferir no disparo
Ao disparar, o `AlarmReceiver` NÃO SHALL postar o aviso cegamente. Ele SHALL reler o
`UsageStatsReader`, recomputar o estado pelo `EpisodeEngine`, e só postar se ainda estiver em
primeiro plano num alvo E o acumulado tiver de fato cruzado o limite; caso contrário, reagenda ou
cancela (D22).

#### Scenario: Confirma antes de postar
- **WHEN** o alarme dispara e a releitura confirma que o alvo está em primeiro plano e o acumulado ≥ limite
- **THEN** o aviso é postado

#### Scenario: Não posta se a pessoa já saiu
- **WHEN** o alarme dispara mas a releitura mostra que o usuário saiu do app antes
- **THEN** o aviso NÃO é postado e o alarme é reavaliado

### Requirement: Postar o aviso heads-up com dois botões
O aviso SHALL ser postado num canal `IMPORTANCE_HIGH` (heads-up sobre tela cheia, GAPS G5) com os
botões `[era hora]` e `[agora não]`. Após postar, o próximo limite do episódio SHALL dobrar (D4/D18).

#### Scenario: Aviso aparece com as duas ações
- **WHEN** o aviso é postado
- **THEN** ele aparece como heads-up com os dois botões, e o próximo limite do episódio passa a ser o dobro

### Requirement: Reagendar em toda transição de estado
O sistema SHALL reavaliar e reagendar (ou cancelar) o alarme sempre que o estado mudar — entrada,
saída, troca de app, fechamento, pausa por hoje ou teto batido (`reagendarAlarme`, D24).

#### Scenario: Sair cancela o alarme
- **WHEN** o usuário sai do app (episódio vai pra PAUSADO)
- **THEN** o alarme pendente é cancelado

