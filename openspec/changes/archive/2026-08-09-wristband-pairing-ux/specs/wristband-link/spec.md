## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Desconectar e esquecer

O app MUST oferecer desconectar a pulseira e esquecer o device lembrado. Desconectar SHALL fechar o
link atual; esquecer SHALL remover o device lembrado para não reconectar passivamente.

#### Scenario: Forget desconecta e limpa
- **WHEN** o usuário toca "Forget" com a pulseira conectada
- **THEN** o link é fechado e o device lembrado é removido, e a auto-reconexão não o traz de volta

#### Scenario: Disconnect sem device lembrado é seguro
- **WHEN** não há link nem device lembrado
- **THEN** desconectar/esquecer é no-op, sem erro
