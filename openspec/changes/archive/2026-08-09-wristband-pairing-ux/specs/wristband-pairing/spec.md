## ADDED Requirements

### Requirement: Scan animado por estado
A tela SHALL apresentar o pareamento por estado (repouso, buscando, conectando, conectado, falha), com uma animação de radar (anéis concêntricos) enquanto busca. Sob reduced-motion, a animação SHALL cair pra um estado estático com texto.

#### Scenario: Buscando mostra o radar
- **WHEN** o scan está em andamento
- **THEN** a tela mostra a animação de radar e o texto "Searching…"

#### Scenario: Reduced-motion sem animação
- **WHEN** o sistema pede reduzir animações
- **THEN** a tela mostra o estado buscando estático, sem anéis animados

### Requirement: Lista de devices ao vivo com sinal
Durante o scan, os devices encontrados SHALL aparecer ao vivo como itens, cada um com nome e uma indicação de força de sinal derivada do rssi. Tocar num item SHALL conectar a ele.

#### Scenario: Device aparece e conecta ao toque
- **WHEN** um device é encontrado no scan
- **THEN** ele aparece na lista com força de sinal, e tocar nele inicia a conexão

### Requirement: Empty state após timeout
Se o scan terminar (ou atingir o timeout) sem nenhum device, a tela SHALL mostrar um empty state com orientação (ligar a pulseira, aproximar) e uma ação de tentar de novo.

#### Scenario: Nada encontrado
- **WHEN** o scan termina sem devices
- **THEN** a tela mostra "No wristband found", dicas e um botão de Retry

### Requirement: Apresentação do estado conectado
Quando conectado, a tela SHALL mostrar o device (nome + selo "Connected"), o controle de intensidade, uma ação "Send test pulse" e uma ação "Forget".

#### Scenario: Conectado mostra device e ações
- **WHEN** o link está Connected
- **THEN** a tela mostra o nome, o selo, a intensidade, "Send test pulse" e "Forget"

#### Scenario: Test pulse vibra
- **WHEN** o usuário toca "Send test pulse" com a pulseira conectada
- **THEN** o app envia um pulso háptico pelo caminho existente

### Requirement: Falha legível com retry
Em falha (permissão negada, Bluetooth desligado, timeout, erro de GATT), a tela SHALL mostrar uma mensagem legível do motivo e uma ação de tentar de novo, sem crash.

#### Scenario: Bluetooth desligado
- **WHEN** o scan falha por Bluetooth desligado
- **THEN** a tela mostra a causa e oferece Retry
