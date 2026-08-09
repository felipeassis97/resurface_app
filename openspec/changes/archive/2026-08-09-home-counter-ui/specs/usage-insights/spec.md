## ADDED Requirements

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
