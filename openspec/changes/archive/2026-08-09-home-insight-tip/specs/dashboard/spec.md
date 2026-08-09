## ADDED Requirements

### Requirement: Card de tip no topo
O dashboard SHALL exibir um card de tip perto do topo (abaixo do header) com a observação pessoal (no máximo duas linhas), vinda da capability `insight-tip`.

#### Scenario: Tip aparece no topo
- **WHEN** o dashboard abre e há um tip disponível
- **THEN** o card de tip é mostrado no topo, acima das seções de atividade

## MODIFIED Requirements

### Requirement: Composição das seções de dados
O dashboard SHALL exibir, a partir das fontes existentes: resumo da semana (total, episódios, média, tendência), distribuição por dia, distribuição por hora (heatmap), episódios que cruzam os dois apps (D2) e comportamento (vídeos + hesitação, quando há acessibilidade). O card de avisos (lista + S2) NÃO SHALL mais aparecer na home.

#### Scenario: Heatmap por hora aparece
- **WHEN** há episódios concentrados entre 23h e 1h
- **THEN** o dashboard exibe a distribuição por hora destacando essa faixa

#### Scenario: Comportamento só com acessibilidade
- **WHEN** não há dado de acessibilidade
- **THEN** a seção de comportamento não aparece, e o resto do dashboard funciona

#### Scenario: Sem card de avisos
- **WHEN** o dashboard é exibido
- **THEN** não há card de avisos nem a proporção S2 na home
