# dashboard Specification

## Purpose
TBD - created by archiving change consolidate-dashboard. Update Purpose after archive.
## Requirements
### Requirement: Tela inicial única, sem bottom nav
O app SHALL abrir numa **única tela inicial** que reúne o estado ao vivo e as observações de uso, **sem barra de navegação inferior**. Não SHALL haver abas top-level.

#### Scenario: App abre no dashboard único
- **WHEN** o onboarding está concluído e as obrigatórias concedidas
- **THEN** o app abre direto no dashboard, sem bottom navigation bar

#### Scenario: Sem abas
- **WHEN** o usuário está no dashboard
- **THEN** não há abas Home/Insights/Ajustes — o conteúdo de Home e Insights está na mesma tela

### Requirement: Hero adaptativo
O topo do dashboard SHALL se adaptar ao estado: com episódio ativo (DENTRO), o hero SHALL ser o contador vivo (minutos + app, atualizando ~1 s); ocioso (FORA), o hero SHALL ser o resumo da semana.

#### Scenario: Ativo mostra o contador
- **WHEN** há um episódio ativo com 22 minutos no Instagram
- **THEN** o hero mostra 22 e o app, atualizando com o tempo

#### Scenario: Ocioso mostra a semana
- **WHEN** não há episódio ativo
- **THEN** o hero mostra o total da semana (sem contador correndo), coerente com P6

### Requirement: Acesso aos ajustes pela top bar
O dashboard SHALL ter uma top bar com um ícone de ajustes que navega pra tela de configurações existente; voltar SHALL retornar ao dashboard.

#### Scenario: Ícone abre os ajustes
- **WHEN** o usuário toca no ícone de ajustes na top bar
- **THEN** o app navega pra tela de ajustes existente

#### Scenario: Voltar retorna ao dashboard
- **WHEN** o usuário volta da tela de ajustes
- **THEN** o app retorna ao dashboard

### Requirement: Composição das seções de dados
O dashboard SHALL exibir, a partir das fontes existentes: resumo da semana (total, episódios, média, tendência), distribuição por dia, **distribuição por hora (heatmap)**, episódios que cruzam os dois apps (D2), comportamento (vídeos + hesitação, quando há acessibilidade), e avisos com a proporção "era hora" (S2).

#### Scenario: Heatmap por hora aparece
- **WHEN** há episódios concentrados entre 23h e 1h
- **THEN** o dashboard exibe a distribuição por hora destacando essa faixa

#### Scenario: Comportamento só com acessibilidade
- **WHEN** não há dado de acessibilidade
- **THEN** a seção de comportamento não aparece, e o resto do dashboard funciona

### Requirement: Copy da tela em inglês
Todo o texto visível do dashboard e da top bar SHALL estar em inglês, incluindo as strings de display servidas pelo aggregator (rótulos de dia da semana e rótulos de resposta a aviso).

#### Scenario: Rótulos em inglês
- **WHEN** o dashboard mostra a distribuição por dia e a lista de avisos
- **THEN** os dias aparecem como "Mon/Tue/…" e as respostas como "right time"/"not now"/"no response"

### Requirement: Empty states sem culpa
Quando não há dado (primeira semana, sem avisos, sem episódios), o dashboard SHALL mostrar estados vazios calmos e convidativos, nunca cobrança ou "você falhou" (P5/P6).

#### Scenario: Sem avisos ainda
- **WHEN** nenhum aviso foi disparado
- **THEN** a seção de avisos mostra um estado vazio calmo, sem linguagem de culpa

