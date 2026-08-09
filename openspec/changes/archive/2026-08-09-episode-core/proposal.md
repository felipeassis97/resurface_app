## Why

O F1 precisa testar o H1 ("saber a duração no momento muda a decisão"). Antes de tocar em
qualquer API de Android, a lógica que decide **quanto tempo de vídeo curto passou** e
**quando avisar** precisa existir, pura e testada. Essa lógica (D2/D3/D14/D4/D18/D5/D11) é a
mais arriscada do produto e é reusada em três lugares (tick ao vivo, replay no cold-start,
releitura do alarme). Construí-la primeiro, isolada e coberta por teste, derrisca tudo o que
vem depois.

## What Changes

- Novo pacote `domain/` em Kotlin **puro** (sem `import android.*`), conforme G1 do `ENGENHARIA.md`.
- Modelos de domínio imutáveis: `UsageEvent` (Enter/Leave/ScreenOff), `EpisodeState`
  (FORA/DENTRO/PAUSADO com acumulado e app em foco), `ClosedEpisode`, `AlertDecision`, `Config`.
- `EpisodeEngine`: máquina de estados que consome um stream de `UsageEvent` e produz o
  `EpisodeState` atual + emite `ClosedEpisode` quando um episódio fecha. Implementa contador
  único entre apps (D2), pausa≠zera com janela de 5 min (D3) e app-inteiro (D14).
- `AlertPolicy`: função pura que, dado estado + config + avisos já disparados, decide se/quando
  avisar — dobra por episódio (D4/D18), teto de 6/dia (D5), respeita "pausar por hoje" (D11).
- Relógio injetado (sem `System.currentTimeMillis()` escondido) para teste determinístico.
- Suíte de testes JUnit, incluindo o **golden test** do episódio real de 18:43 (`PRODUTO.md` §5.5).
- Nenhuma mudança em UI, serviço, Room, DataStore ou manifesto — só domínio + testes.

## Capabilities

### New Capabilities
- `episode-tracking`: o contador único de tempo de vídeo curto atravessando Instagram e
  TikTok — quando conta, pausa, retoma e zera; a máquina de estados do episódio e o fechamento
  de episódios para o histórico.
- `alert-policy`: a política de disparo do aviso — limite, intervalo que dobra por episódio,
  teto diário, e a supressão por "pausar por hoje".

### Modified Capabilities
<!-- Nenhuma — primeiro código de domínio do projeto. -->

## Impact

- **Código novo:** `app/src/main/java/com/resurface/resurface/domain/**` (modelos, `EpisodeEngine`,
  `AlertPolicy`) e `app/src/test/java/com/resurface/resurface/domain/**` (testes).
- **Sem dependências novas:** usa só Kotlin + JUnit (já no build). Sem Android, sem Room, sem Hilt.
- **Fixtures:** o golden test usa a linha do tempo de `PRODUTO.md` §5.5 / `app/docs/logs/`.
- **Habilita:** a próxima change (`usage-data-layer`) — as interfaces de plataforma vão mapear
  eventos crus para os `UsageEvent` deste domínio; o `MonitorService` vai orquestrar este motor.
- **Sem impacto de runtime:** nada roda no app ainda; é lógica coberta por teste, integrada depois.
