# message-composition Specification

## Purpose
TBD - created by archiving change tone-message. Update Purpose after archive.
## Requirements
### Requirement: Compor a mensagem no tom escolhido
O sistema SHALL compor o texto do aviso em **inglês** a partir do nome, do tom, dos hobbies e do momento (minutos, app, hora). A mensagem SHALL poder usar o nome da pessoa. O texto SHALL respeitar os limites do P2 (só afirma o que mede, nada de estado mental) e do P5 (sem cobrança, sem culpa), e SHALL evitar travessões (`—`), usando pontuação simples.

#### Scenario: Mensagem reflete o tom, em inglês, com o nome
- **WHEN** o tom é Bem-humorado, o nome é "Felipe" e o momento é 22 min no Instagram
- **THEN** a mensagem sai em inglês, no registro bem-humorado, podendo citar "Felipe", mencionando os 22 min e o Instagram

#### Scenario: Nunca afirma estado mental ou cobra
- **WHEN** qualquer mensagem é composta
- **THEN** ela não afirma estado mental (P2) nem sugere o que a pessoa deveria estar fazendo (P5)

#### Scenario: Sem travessões
- **WHEN** qualquer mensagem à mão ou gerada é usada
- **THEN** o texto não contém travessões

### Requirement: Gerar com proxy do Nano, sob guard, com fallback à mão
O sistema SHALL tentar gerar a mensagem por um `MessageGenerator` (proxy cloud do Nano). A saída
gerada SHALL passar por um guard que rejeita violação de P2/P5. Se a geração falhar (rede, erro,
vazio) ou o guard rejeitar, o sistema SHALL usar uma mensagem à mão do mesmo tom (D8).

#### Scenario: Geração válida é usada
- **WHEN** o gerador devolve uma mensagem que passa no guard
- **THEN** essa mensagem é usada no aviso

#### Scenario: Falha de rede cai no template
- **WHEN** o gerador falha (sem rede ou erro da API)
- **THEN** o aviso usa uma mensagem à mão do tom escolhido

#### Scenario: Saída insegura é descartada
- **WHEN** o gerador devolve um texto que fere P2 ou P5
- **THEN** o guard rejeita e o aviso usa a mensagem à mão

### Requirement: Pré-gerar sem bloquear o disparo
A geração (que usa rede) NÃO SHALL acontecer no instante do disparo do alarme. O sistema SHALL
pré-gerar a mensagem do próximo aviso antes (no tick/agendamento) e cachear; no disparo usa a
cacheada, ou o template se não houver.

#### Scenario: Disparo usa o cache
- **WHEN** o alarme dispara e há uma mensagem pré-gerada em cache
- **THEN** o aviso posta a cacheada sem chamar a rede naquele instante

### Requirement: Funciona sem chave e sem rede
Sem chave de API ou sem rede, o app SHALL continuar avisando com as mensagens à mão — a geração é um
proxy por cima da base, nunca um pré-requisito (D8).

#### Scenario: Sem chave, só à mão
- **WHEN** não há `GEMINI_API_KEY` configurada
- **THEN** todos os avisos usam mensagens à mão, e o resto do app funciona normalmente

