## ADDED Requirements

### Requirement: Fora da janela ativa suprime avisos

Quando o instante atual está fora da janela ativa configurada, a política NÃO SHALL decidir
disparar nenhum aviso, junto das guardas existentes (pausa por hoje, teto diário). A contagem
continua; só o aviso é suprimido. Janela vazia conta como sempre ativa.

#### Scenario: Fora da janela não avisa
- **WHEN** um episódio cruza o limite mas o dia/horário está fora da janela ativa
- **THEN** a política decide Hold, mesmo com o acumulado acima do limite

#### Scenario: Guardas compõem
- **WHEN** o instante está dentro da janela mas "pausar por hoje" está ativo
- **THEN** a política ainda decide Hold (qualquer guarda em Hold segura o disparo)
