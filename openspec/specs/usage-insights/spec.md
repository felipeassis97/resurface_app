# usage-insights Specification

## Purpose
TBD - created by archiving change home-counter-ui. Update Purpose after archive.
## Requirements
### Requirement: Listar episódios recentes
A tela de observações SHALL listar os episódios arquivados, do mais recente pro mais antigo,
com duração (acumulado) e quais apps, a partir do `EpisodeRepository`.

#### Scenario: Episódios aparecem em ordem
- **WHEN** há dois episódios arquivados, um mais recente que o outro
- **THEN** o UiState lista o mais recente primeiro, cada um com seu acumulado e apps

### Requirement: Listar avisos com a resposta
A tela SHALL listar os avisos disparados com sua resposta — "era hora", "agora não", ou "sem
resposta" (ignorado, P3) — a partir do `OutcomeRepository`.

#### Scenario: Aviso respondido e ignorado
- **WHEN** há um aviso com resposta "era hora" e outro sem resposta
- **THEN** o UiState mostra o primeiro como respondido e o segundo como "sem resposta"

### Requirement: Resumo da razão de acerto (S2)
A tela SHALL derivar e mostrar a proporção de avisos marcados "era hora" entre os respondidos —
a métrica S2 do `NEGOCIO.md`.

#### Scenario: Proporção calculada
- **WHEN** há 3 avisos "era hora" e 1 "agora não" entre os respondidos
- **THEN** o UiState reporta 75% de "era hora"

### Requirement: Resumo da semana com tendência
A tela SHALL mostrar, para a semana corrente, o tempo total de vídeo curto, o número de episódios
e a média por episódio, mais a tendência (variação %) em relação à semana anterior.

#### Scenario: Total e média da semana
- **WHEN** a semana tem episódios somando 4 h em 20 episódios
- **THEN** o resumo mostra 4 h, 20 episódios e média de 12 min

#### Scenario: Tendência vs semana anterior
- **WHEN** o total desta semana é 18% menor que o da anterior
- **THEN** o resumo indica queda de 18%

### Requirement: Distribuição por dia
A tela SHALL mostrar o tempo por dia dos últimos 7 dias, para ver os picos.

#### Scenario: Minutos por dia
- **WHEN** há episódios em dias diferentes da semana
- **THEN** cada dia mostra a soma de minutos dos seus episódios, e dias sem uso mostram zero

### Requirement: Distribuição por hora
A tela SHALL mostrar em que faixas de hora do dia o uso se concentra (por hora de início dos episódios).

#### Scenario: Concentração noturna
- **WHEN** a maioria dos episódios começa entre 23h e 1h
- **THEN** a distribuição por hora destaca essa faixa

### Requirement: Contagem de comportamento na semana
Quando há dado de acessibilidade, a tela SHALL mostrar o número de vídeos assistidos na semana e a
proporção de deslizes com hesitação. Sem dado de acessibilidade, essa seção fica vazia (D15).

#### Scenario: Vídeos e hesitação
- **WHEN** a semana tem 340 vídeos, 27 com hesitação
- **THEN** a tela mostra 340 vídeos e ~8% de hesitação

#### Scenario: Sem acessibilidade, seção vazia
- **WHEN** não há eventos de comportamento
- **THEN** a seção de vídeos não aparece (ou mostra "—"), e o resto do dashboard funciona

### Requirement: Episódios que cruzam os dois apps
A tela SHALL mostrar quantos episódios da semana atravessaram Instagram e TikTok (D2) — o
diferencial que o nativo não vê.

#### Scenario: Contagem de cruza-apps
- **WHEN** 6 episódios da semana envolveram os dois apps
- **THEN** a tela mostra 6 episódios que atravessaram os dois

