## ADDED Requirements

### Requirement: O texto do aviso é composto, não fixo
O aviso SHALL usar o texto composto no tom do usuário (via `message-composition`) em vez de uma
frase fixa. O restante do disparo — canal HIGH, dois botões, dobro do intervalo, acordar-pra-conferir —
NÃO SHALL mudar.

#### Scenario: Aviso sai no tom
- **WHEN** o aviso é postado e há um perfil de tom definido
- **THEN** o título/corpo do aviso vêm da composição no tom, e os dois botões continuam presentes

### Requirement: Registrar o tom e a fonte do aviso
Ao postar, o sistema SHALL registrar no outcome o tom usado e a fonte do texto (gerado vs à mão),
para permitir comparar a resposta por tom (H4) na análise.

#### Scenario: Outcome guarda tom e fonte
- **WHEN** um aviso é postado com uma mensagem gerada em tom Direto
- **THEN** o outcome registra tom = Direto e fonte = gerado
