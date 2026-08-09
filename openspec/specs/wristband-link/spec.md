# wristband-link Specification

## Purpose
TBD - created by archiving change wristband-haptics. Update Purpose after archive.
## Requirements
### Requirement: Pareamento por scan e conectar

O app MUST oferecer, na tela de pulseira, um scan BLE filtrado pelo service UUID e apresentar os
devices encontrados; o usuário **escolhe** qual conectar (não é mais auto-conectar no primeiro). Ao
conectar, o app lembra o address para reconexões futuras.

#### Scenario: Escolher e conectar da lista
- **WHEN** o usuário toca num device encontrado com Bluetooth e permissões prontos
- **THEN** o app conecta a esse device, grava o address e o estado passa a Connected

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

### Requirement: Desconectar e esquecer

O app MUST oferecer desconectar a pulseira e esquecer o device lembrado. Desconectar SHALL fechar o
link atual; esquecer SHALL remover o device lembrado para não reconectar passivamente.

#### Scenario: Forget desconecta e limpa
- **WHEN** o usuário toca "Forget" com a pulseira conectada
- **THEN** o link é fechado e o device lembrado é removido, e a auto-reconexão não o traz de volta

#### Scenario: Disconnect sem device lembrado é seguro
- **WHEN** não há link nem device lembrado
- **THEN** desconectar/esquecer é no-op, sem erro

