## ADDED Requirements

### Requirement: Pareamento por scan e conectar

O app MUST oferecer, na tela de Ajustes, uma ação simples que faz scan BLE filtrado pelo
service UUID da pulseira, conecta ao device encontrado e o lembra para reconexões futuras.

#### Scenario: Parear pela primeira vez
- **WHEN** o usuário toca "Procurar e conectar" com Bluetooth e permissões prontos
- **THEN** o app faz um scan limitado, conecta ao primeiro device válido e grava o address
- **AND** o estado do link passa a Connected

#### Scenario: Sem permissão ou adapter desligado
- **WHEN** falta permissão BLE ou o adapter está desligado
- **THEN** o app não trava; expõe um estado de falha legível (permissão/adapter) sem crash

### Requirement: Auto-reconexão passiva

O app MUST reconectar ao device lembrado sem scan, de forma passiva (autoConnect), sempre
que possível — disparado no start do serviço em primeiro plano. A reconexão NÃO SHALL fazer
scan contínuo (custo de bateria).

#### Scenario: Reconecta ao lembrado no start do serviço
- **WHEN** o FGS inicia e há um device lembrado e permissão BLUETOOTH_CONNECT concedida
- **THEN** o app tenta reconectar passivamente e o OS restabelece o link quando a pulseira
  entra no alcance

#### Scenario: Nada lembrado é no-op
- **WHEN** não há device lembrado
- **THEN** a reconexão não faz nada (sem scan, sem erro)

### Requirement: Envio de comando háptico

O app MUST enviar um comando háptico ao device apenas quando o link está pronto para
escrever; caso contrário devolve um resultado "não conectado" sem lançar exceção.

#### Scenario: Envia quando conectado
- **WHEN** o link está Connected e um comando é enviado
- **THEN** o payload é escrito na characteristic de comando e o resultado indica sucesso/falha

#### Scenario: Não envia quando desconectado
- **WHEN** não há link
- **THEN** o envio devolve "não conectado" e nada é escrito

### Requirement: Permissões BLE just-in-time

O app MUST declarar `BLUETOOTH_SCAN` (com `neverForLocation`) e `BLUETOOTH_CONNECT`, e ler o
status de permissão/adapter do sistema a cada tentativa (nunca em cache), pois o usuário pode
revogar permissão ou desligar o rádio fora do app.

#### Scenario: Readiness reflete o estado atual do sistema
- **WHEN** a permissão é revogada ou o Bluetooth é desligado
- **THEN** a próxima leitura de readiness reporta o bloqueio correspondente
