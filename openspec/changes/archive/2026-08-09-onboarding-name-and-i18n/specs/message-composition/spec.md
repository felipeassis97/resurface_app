## MODIFIED Requirements

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
