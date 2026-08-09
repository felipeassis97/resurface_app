## Why

A home mostra números, mas não conta uma história. Um cartão de "tip" no topo, com uma observação pessoal e útil ("You scroll most between 14h and 15h"), dá um motivo pra abrir e um espelho leve do próprio padrão. O card de Alerts atual (S2 "right time") ocupa o rodapé com dado que faz mais sentido numa tela de detalhes do que na home.

## What Changes

- **Remover o card de Alerts** da home (a lista de avisos + a razão S2). O dado continua sendo gravado; só sai da tela. **BREAKING** (UI): o S2/H1 perde sua única superfície visível por ora (dívida registrada).
- **Adicionar um card de tip no topo** (abaixo do header): uma observação pessoal em até 2 linhas, no tom escolhido.
- **Derivar o fato localmente** do `InsightsAggregator` (pico de hora, dia mais pesado, tendência, cruza-apps, vídeos). A IA só **reescreve** o fato em 2 linhas no tom; nunca inventa número (P2). Sem chave/rede, usa a **frase local**.
- **Alternar** entre os fatos mais fortes a cada abertura (índice rotativo persistido).
- **Privacidade:** só a frase-fato curta vai pro cloud (nunca os dados crus). Melhor P4.
- **Cache por dia:** a rede acontece no máximo 1x/dia (ou quando o fato muda); a abertura mostra a frase local na hora e troca pela versão da IA quando chega.

Fora de escopo (dívida): tips **por-app** ("no Instagram entre 14h e 15h") e "Instagram é X%" — precisam gravar tempo por app por episódio (mudança de schema + migração). E devolver a visibilidade do H1/S2 numa tela de detalhes.

## Capabilities

### New Capabilities

- `insight-tip`: derivar o fato mais saliente das estatísticas, escolher por rotação, frasear no tom (frase local + reescrita opcional pela IA sob guard), e cachear por dia.

### Modified Capabilities

- `dashboard`: remover o card de Alerts; exibir o card de tip no topo.

## Impact

- **Domain (novo, puro):** `InsightSelector` (rankeia + rotaciona → `Insight`), `InsightTemplates` (frase local por tipo + tom).
- **Geração:** novo caminho reusando `GeminiClient.generate` (prompt = fato + tom, 2 linhas), sob um guard P2/P5; fallback = frase local.
- **Cache/rotação:** cache do tip por dia + contador rotativo (DataStore).
- **UI:** `DashboardViewModel` (expõe o tip), `DashboardScreen` (card novo no topo, remove `AlertsCard`).
- **Reusa:** `InsightsAggregator` (fatos já calculados), `ProfileRepository` (tom), `MessageGuard` (ou um guard equivalente).
- **Não muda:** episódios/schema, serviço, permissões.
