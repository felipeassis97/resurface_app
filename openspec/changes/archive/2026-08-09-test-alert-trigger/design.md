## Context

O aviso real é composto e postado em `AlertEvaluator.postComposed`: pega o perfil, tenta
a mensagem gerada (Gemini, pré-gerada+cache) senão o template à mão, grava o outcome
(tom+fonte) e chama `Notifier.postAlert(title, body, id)`. Disparar isso hoje exige bater
o limite de tempo real. Queremos o mesmo resultado visível (notificação no tom) sob
demanda, mas como **ferramenta de teste isolada e removível**, sem contaminar o dado.

## Goals / Non-Goals

**Goals:**
- Disparar um aviso real, no tom atual, com um toque.
- Exercitar o caminho de composição de verdade: Gemini → `MessageGuard` → fallback à mão.
- Isolamento total: um pacote `dev/`, um ponto de contato na produção, gate `BuildConfig.DEBUG`.
- Zero contaminação do dado de pesquisa e **zero migração de schema**.

**Non-Goals:**
- Simular acúmulo de tempo, estado de episódio ou reagendamento de alarme.
- Testar a política de disparo (D5/D9/D18) — o teste ignora a decisão e sempre posta.
- Persistir o outcome do teste (de propósito, pra não sujar a pesquisa).
- Aparecer em release.

## Decisions

**D-1 — Reusar a composição, não o `AlertEvaluator`.**
`AlertEvaluator` amarra composição a estado de episódio + política + reagendamento. O
gatilho de teste não tem episódio. Então uma classe nova `TestAlertTrigger` reusa só as
peças de composição (`MessageGenerator`, `MessageGuard`, `TemplateComposer`,
`ProfileRepository`, `Notifier`) — as mesmas de produção — montando um `Moment` sintético
(limite = limite configurado; app = "Instagram"; hora = relógio real). Mantém o teste
fiel sem acoplar ao motor de estado.

**D-2 — Não gravar outcome; id sentinela.**
`Notifier.postAlert` exige um id (liga os botões F7 a uma linha de outcome). O aviso real
grava a linha antes. O teste **não** grava — passa `TEST_ALERT_ID = -1L`. Se o usuário
tocar um botão de resposta, `OutcomeRepository.recordResponse(-1, …)` faz um UPDATE que
casa 0 linhas — inócuo. Assim: nada de migração Room, nada no dado de pesquisa.

**D-3 — Isolamento por pacote + gate de build.**
Todo o código no pacote `com.resurface.resurface.dev/`:
- `TestAlertTrigger` (`@Singleton`, injeta as peças de composição) — método `suspend fire()`.
- `DevToolsViewModel` (`@HiltViewModel`) — `onTestAlert()` roda `fire()` no escopo do VM.
- `DevToolsSection` (composable) — o botão.
Único toque na produção: `SettingsScreen` chama `if (BuildConfig.DEBUG) DevToolsSection()`.
Em release o bloco é morto pelo gate. Esconder = remover a linha ou a pasta.

**D-4 — Fidelidade da composição = a de produção.**
`TestAlertTrigger.fire` replica a ordem do `postComposed`/`preGenerate`: gera →
`guard.isSafe` → senão template. Como reusa as mesmas classes, o que o teste mostra é o
que o aviso real mostraria naquele tom/hora. (Sem cache: gera na hora, síncrono ao toque.)

## Risks / Trade-offs

- **Vazar pra release.** Mitigado: gate `BuildConfig.DEBUG` compila o bloco fora do
  release; o pacote `dev/` não é referenciado em nenhum caminho de produção.
- **Chamada de rede na thread errada.** `MessageGenerator`/`GeminiClient` já trocam pra
  `@IoDispatcher` internamente; o VM só coleta. Sem risco de NetworkOnMainThread.
- **Botão F7 na notificação de teste.** Toca → UPDATE de 0 linhas (id -1). Aceito: o teste
  cobre a aparência/entrega; a persistência de resposta é coberta pelo aviso real.
- **Divergência futura com `postComposed`.** Se o caminho de produção mudar, o teste pode
  ficar desatualizado. Aceito: é ferramenta de dev; o custo de sincronizar é baixo e o
  código fica lado a lado nas mesmas classes reusadas.
