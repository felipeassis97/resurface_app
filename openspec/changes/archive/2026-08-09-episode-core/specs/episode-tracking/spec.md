## ADDED Requirements

### Requirement: Contador único atravessando apps
O sistema SHALL manter um único acumulador de tempo de vídeo curto que atravessa
Instagram e TikTok. Trocar de um app-alvo para o outro NÃO SHALL zerar nem reiniciar o
acumulado — é a mesma atividade (D2).

#### Scenario: Trocar de app não zera
- **WHEN** o usuário fica 15 min no Instagram e depois entra no TikTok sem sair mais de 5 min
- **THEN** o acumulado continua de 15 min e segue somando o tempo do TikTok

#### Scenario: Episódio real atravessando os dois apps (golden)
- **WHEN** a linha do tempo de `PRODUTO.md` §5.5 (Instagram até ~18:00, depois TikTok até 18:22, pausas < 5 min, depois 15 min fora) é alimentada evento a evento
- **THEN** o motor produz **um único** episódio fechado atravessando Instagram **e** TikTok (da ordem de ~18–22 min), e um **segundo** episódio começa após o gap de 15 min
- **NOTA** o resumo textual do §5.5 é ilustrativo (omite sub-pausas) e não reproduz o número exato de 18:43; o golden valida a MECÂNICA de fusão/pausa/fechamento, não a precisão do acumulado (ver `design.md` D6)

### Requirement: Pausar não é zerar (janela de 5 minutos)
Sair de um app-alvo SHALL pausar o acumulado, não zerá-lo. Retornar em menos de 5 min
SHALL retomar de onde parou. Ficar 5 min ou mais fora SHALL fechar o episódio e o próximo
começa do zero (D3).

#### Scenario: Voltar em menos de 5 min retoma
- **WHEN** o usuário sai do app com 3 min acumulados e volta 4 min depois
- **THEN** o episódio retoma e o acumulado continua a partir de 3 min

#### Scenario: Ficar 5 min fora zera
- **WHEN** o usuário sai do app com 6 min acumulados e só volta 6 min depois
- **THEN** o episódio anterior é fechado e um novo episódio começa com acumulado 0

### Requirement: Conta o app inteiro
O sistema SHALL contar qualquer atividade em primeiro plano de um pacote-alvo, sem
distinguir aba (Reels, feed, stories) — o tempo é do app inteiro (D14).

#### Scenario: Feed conta igual ao Reels
- **WHEN** o usuário passa tempo no feed do Instagram (não só no Reels)
- **THEN** esse tempo entra no acumulado do episódio

### Requirement: Tela apagada pausa
O sistema SHALL pausar o acumulado quando a tela apaga (`SCREEN_NON_INTERACTIVE`) com um app-alvo em primeiro plano, como se o usuário tivesse saído.

#### Scenario: Tela apaga durante uso
- **WHEN** a tela apaga enquanto o Instagram está em primeiro plano
- **THEN** o acumulado pausa e retoma quando o app volta ao primeiro plano dentro de 5 min

### Requirement: Fechamento de episódio para o histórico
Ao fechar um episódio (5 min fora de qualquer alvo), o sistema SHALL emitir um episódio
fechado imutável com início, fim, acumulado e quais apps participaram — para arquivar depois.

#### Scenario: Episódio fechado emitido uma vez
- **WHEN** um episódio ativo excede 5 min sem retorno a um app-alvo
- **THEN** exatamente um episódio fechado é emitido com o acumulado final e os apps envolvidos

### Requirement: Estado reconstruível por replay
O estado do episódio SHALL ser função determinística apenas do stream de eventos e do
relógio injetado — sem estado escondido. Alimentar o mesmo stream duas vezes SHALL produzir
o mesmo estado (base do replay no cold-start, D24).

#### Scenario: Replay determinístico
- **WHEN** o mesmo stream de eventos é alimentado a duas instâncias independentes do motor
- **THEN** ambas produzem exatamente o mesmo `EpisodeState` e os mesmos episódios fechados
