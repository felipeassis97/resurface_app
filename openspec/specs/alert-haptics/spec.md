# alert-haptics Specification

## Purpose
TBD - created by archiving change wristband-haptics. Update Purpose after archive.
## Requirements
### Requirement: Vibra em todo aviso

O app MUST disparar um pulso háptico na pulseira sempre que posta uma notificação de aviso,
incluindo o aviso de teste. O disparo é fire-and-forget e NÃO SHALL atrasar, bloquear ou
falhar a notificação na tela.

#### Scenario: Aviso real vibra
- **WHEN** o app posta um aviso (limite cruzado) e a pulseira está conectada
- **THEN** um pulso é enviado à pulseira junto da notificação

#### Scenario: Aviso de teste vibra
- **WHEN** o usuário usa o botão de aviso de teste e a pulseira está conectada
- **THEN** o mesmo pulso é enviado (é o jeito de testar a pulseira sem esperar o limite)

#### Scenario: Sem pulseira é no-op silencioso
- **WHEN** um aviso é postado e não há pulseira conectada
- **THEN** a notificação na tela aparece normalmente e nenhum erro ocorre

### Requirement: Efeito fixo Gentle com intensidade configurável

O pulso do aviso MUST usar o efeito `Gentle` (um pulso) com a intensidade configurada pelo
usuário, persistida entre sessões. Sem intensidade configurada, o firmware aplica seu padrão.

#### Scenario: Usa a intensidade gravada
- **WHEN** o usuário define a intensidade em Ajustes e um aviso dispara
- **THEN** o comando enviado é `Gentle` com essa intensidade
- **AND** o valor sobrevive a reinício do app

#### Scenario: Intensidade dedicada, separada da config de aviso
- **WHEN** a intensidade é lida ou gravada
- **THEN** ela vem de um store dedicado da pulseira (não misturado com limite/janela/tom)

