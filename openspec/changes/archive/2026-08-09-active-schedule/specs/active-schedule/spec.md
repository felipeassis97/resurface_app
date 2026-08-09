## ADDED Requirements

### Requirement: Janela ativa allow-list

O app MUST permitir que o usuário defina uma janela ativa como allow-list: um conjunto de
dias da semana e uma faixa de horário do dia em que aceita receber avisos. A janela governa
apenas o disparo do aviso, nunca a medição.

#### Scenario: Dentro da janela permite avisar
- **WHEN** a janela ativa inclui o dia e o horário atuais
- **THEN** a decisão de disparo segue a política normal (limite, dobro, teto)

#### Scenario: Fora da janela silencia o aviso
- **WHEN** o instante atual cai fora dos dias ou da faixa configurados
- **THEN** nenhum aviso dispara, mesmo que o acumulado cruze o limite

#### Scenario: Faixa que cruza a meia-noite
- **WHEN** a faixa vai de um horário maior para um menor (ex.: 22h00–01h00)
- **THEN** o período ativo abrange da hora de início até a de fim do dia seguinte

### Requirement: Janela vazia é sempre ativa

Quando nenhuma janela está configurada (nenhum dia ou faixa), o app MUST se comportar como
sempre ativo, preservando o comportamento atual para quem não configura nada.

#### Scenario: Sem configuração avisa a qualquer hora
- **WHEN** o usuário nunca definiu uma janela ativa
- **THEN** os avisos disparam a qualquer dia e hora (comportamento pré-existente)

### Requirement: Conta sempre, cutuca só na janela

A janela ativa MUST NOT afetar a contagem de tempo, a máquina de episódios ou a leitura de
uso. A medição continua 24h; só o aviso é condicionado à janela.

#### Scenario: Uso fora da janela ainda é medido
- **WHEN** o usuário assiste vídeo curto fora da janela ativa
- **THEN** o tempo é contado e o episódio evolui normalmente
- **AND** nenhum aviso é postado por esse uso

### Requirement: Agendar para a abertura da janela

O app MUST agendar o alarme para a próxima abertura da janela quando um episódio está em
andamento (DENTRO) mas o instante atual está fora da janela, para não perder o momento em
que ela abre.

#### Scenario: Episódio começa fora e entra na janela
- **WHEN** um episódio está ativo às 22h50 e a janela abre às 23h00
- **THEN** o alarme é agendado para as 23h00
- **AND** ao abrir, a política é reavaliada com o acumulado do momento

### Requirement: Persistência e edição da janela

O app MUST persistir a janela ativa como parte da configuração do usuário e oferecer um
editor na tela de Ajustes com chips de dia da semana e campos de horário de início e fim.

#### Scenario: Editar e persistir
- **WHEN** o usuário marca dias e define a faixa de horário em Ajustes
- **THEN** a janela é gravada e passa a valer para os próximos avisos
- **AND** sobrevive a reinício do app
