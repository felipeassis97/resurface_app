## ADDED Requirements

### Requirement: Aviso ao cruzar o limite
A política SHALL decidir que um aviso deve disparar quando o acumulado do episódio cruza o
limite configurado. O limite padrão SHALL ser 20 minutos e SHALL ser configurável na faixa
de 10 a 60 minutos.

#### Scenario: Cruzou o limite padrão
- **WHEN** o acumulado do episódio atinge 20 min e nenhum aviso foi dado ainda neste episódio
- **THEN** a política decide disparar o aviso

#### Scenario: Abaixo do limite não dispara
- **WHEN** o acumulado do episódio está em 19 min 59 s
- **THEN** a política NÃO decide disparar

### Requirement: Intervalo dobra a cada aviso, por episódio
Após cada aviso, o próximo limite SHALL ser o dobro do anterior, dentro do mesmo episódio
(20 → 40 → 80 → 160). O estado do dobro SHALL viver no episódio e morrer com ele — NÃO é
por dia (D4/D18).

#### Scenario: Segundo aviso no dobro
- **WHEN** o primeiro aviso disparou aos 20 min e o acumulado atinge 40 min no mesmo episódio
- **THEN** a política decide disparar o segundo aviso

#### Scenario: Novo episódio recomeça no limite base
- **WHEN** um episódio novo começa após um anterior que já tinha dobrado para 40 min
- **THEN** o primeiro aviso do novo episódio volta a ser no limite base (20 min)

### Requirement: Próximo limite derivado, não persistido
A decisão SHALL ser função pura de (acumulado, número de avisos já disparados no episódio,
config). O número de avisos já disparados vem contado da fonte (as próprias notificações
registradas), NÃO de um contador persistido em disco (D24).

#### Scenario: Mesma entrada, mesma decisão
- **WHEN** a política recebe o mesmo (acumulado, avisosJáDisparados, config) duas vezes
- **THEN** produz exatamente a mesma decisão, sem efeito colateral

### Requirement: Teto diário de avisos
O sistema SHALL disparar no máximo 6 avisos por dia. Ao atingir o teto, nenhum aviso a mais
SHALL disparar até a virada da meia-noite (D5). O teto é o único estado diário.

#### Scenario: Sexto aviso é o último do dia
- **WHEN** 6 avisos já dispararam hoje e um episódio cruza o limite de novo
- **THEN** a política NÃO decide disparar

#### Scenario: Zera à meia-noite
- **WHEN** o dia vira e um episódio cruza o limite
- **THEN** a contagem diária recomeça e a política pode disparar

### Requirement: Pausar por hoje suprime avisos
Quando "pausar por hoje" está ativo, a política NÃO SHALL decidir disparar nenhum aviso até
a virada da meia-noite, independentemente do acumulado (D11). A contagem de tempo continua;
só o aviso é suprimido.

#### Scenario: Pausado não avisa
- **WHEN** "pausar por hoje" está ativo e um episódio cruza o limite
- **THEN** a política NÃO decide disparar, mesmo que o acumulado passe do limite
