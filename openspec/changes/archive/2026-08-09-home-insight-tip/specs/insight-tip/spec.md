## ADDED Requirements

### Requirement: Derivar o fato do insight localmente
O sistema SHALL derivar o fato do tip **localmente** a partir das estatísticas medidas (pico de hora, dia mais pesado, tendência semanal, episódios que cruzam os dois apps, vídeos quando há acessibilidade). O sistema NÃO SHALL deixar a IA inventar estatística (P2): o número vem sempre do dado.

#### Scenario: Fato derivado do dado
- **WHEN** os episódios da semana concentram início entre 14h e 15h
- **THEN** o fato disponível é "pico de hora entre 14h e 15h", a partir do dado medido

#### Scenario: Sem dado suficiente não força tip
- **WHEN** não há episódios suficientes pra um fato saliente
- **THEN** o sistema mostra um tip neutro de boas-vindas ou nenhum, sem inventar número

### Requirement: Alternar entre os fatos
O sistema SHALL alternar entre os fatos mais fortes disponíveis a cada abertura, por um índice rotativo persistido, pra aberturas repetidas mostrarem variedade.

#### Scenario: Aberturas seguidas variam o tip
- **WHEN** o usuário abre o app duas vezes e há mais de um fato forte
- **THEN** o tip mostrado na segunda abertura é diferente do da primeira

### Requirement: Frasear no tom, com fallback local
O tip SHALL ser escrito no tom escolhido pelo usuário, em no máximo duas linhas. O sistema SHALL mostrar de imediato uma frase local (template) do fato, e PODE substituí-la por uma reescrita da IA que passe por um guard P2/P5. Sem rede ou sem chave, a frase local SHALL permanecer.

#### Scenario: Sem chave usa a frase local
- **WHEN** não há chave de API
- **THEN** o tip mostra a frase local do fato, no tom escolhido

#### Scenario: IA reescreve e é usada
- **WHEN** a IA devolve uma reescrita que passa no guard
- **THEN** o card troca a frase local pela reescrita

#### Scenario: Saída insegura é descartada
- **WHEN** a IA devolve texto que fere P2 ou P5
- **THEN** o guard rejeita e o card mantém a frase local

### Requirement: Rede fora da abertura, cache por dia
A reescrita por IA SHALL ser cacheada e atualizada no máximo uma vez por dia (ou quando o fato escolhido muda); a abertura NÃO SHALL travar esperando a rede.

#### Scenario: Reabrir no mesmo dia usa cache
- **WHEN** o usuário reabre o app no mesmo dia com o mesmo fato
- **THEN** o tip vem do cache, sem nova chamada de rede

### Requirement: Só a frase-fato vai pro cloud
Apenas a frase-fato curta (e o tom) SHALL ser enviada pro cloud; os dados de uso crus NÃO SHALL ser enviados.

#### Scenario: Prompt não leva dados crus
- **WHEN** a IA é chamada pra reescrever
- **THEN** o prompt contém só o fato resumido e o tom, nunca a série de episódios/horas
