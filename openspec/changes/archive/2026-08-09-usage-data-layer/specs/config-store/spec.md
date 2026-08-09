## ADDED Requirements

### Requirement: Limite de minutos configurável
O sistema SHALL persistir o limite de minutos, com padrão 20 e faixa válida de 10 a 60. Uma
tentativa de gravar fora da faixa SHALL falhar sem alterar o valor guardado (retorna `Result`).

#### Scenario: Padrão sem nada gravado
- **WHEN** nenhum limite foi gravado ainda
- **THEN** o limite lido é 20

#### Scenario: Grava dentro da faixa
- **WHEN** o usuário grava o limite 30
- **THEN** a leitura subsequente devolve 30

#### Scenario: Rejeita fora da faixa
- **WHEN** o usuário tenta gravar o limite 5 (abaixo de 10)
- **THEN** a gravação falha e o limite guardado permanece o anterior

### Requirement: Pausar por hoje até a meia-noite
O sistema SHALL persistir um marco de "pausar por hoje" e reportar se está ativo, expirando
na virada da meia-noite (D11). Ativar SHALL apenas suprimir avisos — não zera nem para a contagem.

#### Scenario: Pausado hoje está ativo
- **WHEN** o usuário ativa "pausar por hoje"
- **THEN** o repositório reporta pausado ativo até a próxima meia-noite

#### Scenario: Expira na virada do dia
- **WHEN** o dia vira após um "pausar por hoje" ativado ontem
- **THEN** o repositório reporta não-pausado

### Requirement: Entregar o Config do domínio
O sistema SHALL montar e expor o `Config` do domínio (limite + alvos + janela de retorno) como
`Flow`, para o serviço e o motor consumirem uma única fonte da verdade (G3).

#### Scenario: Config reflete o limite gravado
- **WHEN** o limite gravado muda para 45
- **THEN** o `Config` emitido passa a ter `limitMinutes == 45`
