## ADDED Requirements

### Requirement: Registrar a resposta ao aviso
Quando o usuário toca um botão do aviso, o sistema SHALL registrar a resposta subjetiva
(`era hora` ou `agora não`) junto ao instante e ao aviso correspondente (F7). O registro é o
instrumento que mede o H1 e a métrica S2.

#### Scenario: Toque em "era hora" é registrado
- **WHEN** o usuário toca `[era hora]` no aviso
- **THEN** um outcome com resposta "era hora" e o timestamp é persistido

#### Scenario: Toque em "agora não" é registrado
- **WHEN** o usuário toca `[agora não]`
- **THEN** um outcome com resposta "agora não" é persistido

### Requirement: Ignorar é uma resposta válida
Não tocar em nenhum botão NÃO SHALL travar nem repetir o aviso — ignorar é uma resposta válida
(P1/P3). O sistema SHALL poder distinguir depois, na análise, entre respondido e ignorado.

#### Scenario: Aviso ignorado não insiste
- **WHEN** o aviso é postado e o usuário não toca em nada
- **THEN** o app não repete o aviso fora do ritmo do dobro, e o outcome permanece "sem resposta" até (se) um toque acontecer
