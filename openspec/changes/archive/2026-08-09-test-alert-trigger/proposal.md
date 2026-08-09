## Why

Verificar o loop de aviso hoje exige acumular tempo real de vídeo curto até bater o
limite (20 min). Isso torna lento validar F2 (mensagem no tom), a aparência da
notificação heads-up e os botões F7 — e impossível demonstrar o app na hora (ex.: defesa
do mestrado). Precisamos disparar o aviso **sob demanda**, exercitando o caminho real de
composição (Gemini → guard P2/P5 → fallback à mão) e de notificação.

Restrição do dono: é uma ferramenta de teste, **não** parte do produto. Tem que ser
**totalmente isolada** e **fácil de esconder/remover depois**, sem deixar resíduo no
código de produção nem contaminar o dado de pesquisa.

## What Changes

- Novo botão **"Disparar aviso de teste"** na tela de Ajustes.
- Ao tocar: compõe a mensagem no tom do perfil atual pelo **mesmo caminho do aviso real**
  (gera no Gemini → `MessageGuard` P2/P5 → fallback a template à mão), e posta a
  notificação heads-up via `Notifier`, idêntica ao aviso de verdade.
- **Não** grava em `alert_outcome` nem em episódios — usa um id sentinela. Zero
  contaminação do dado de pesquisa, **zero migração de schema**.
- Todo o código do teste vive num pacote isolado `…/dev/` (uma classe de gatilho + um
  ViewModel + um bloco de UI). O ponto de contato na produção é **uma única linha** na
  tela de Ajustes, atrás de `BuildConfig.DEBUG` → nunca aparece em build de release.
- Esconder depois = apagar a pasta `dev/` + uma linha; ou só trocar o gate.

## Capabilities

### New Capabilities
- `dev-tools`: afordâncias de desenvolvimento/verificação isoladas, visíveis só em build
  de debug, que exercitam caminhos reais do app sem afetar o dado de pesquisa. Primeira: o
  gatilho de aviso de teste.

### Modified Capabilities
<!-- Nenhuma. O botão só aparece atrás de BuildConfig.DEBUG e reusa o caminho de
     composição/notificação existente sem alterar seus requisitos. -->

## Impact

- **Novo pacote** `com.resurface.resurface.dev/` (isolado):
  - `TestAlertTrigger` — compõe (reusa `MessageGenerator`/`MessageGuard`/`TemplateComposer`)
    e chama `Notifier.postAlert(...)` com id sentinela.
  - `DevToolsViewModel` — expõe `onTestAlert()`.
  - `DevToolsSection` (composable) — o botão.
- **Um** ponto de contato em produção: `SettingsScreen` chama `if (BuildConfig.DEBUG)
  DevToolsSection()` (uma linha).
- Reusa sem alterar: `Notifier`, `MessageGenerator` (Gemini), `MessageGuard`,
  `TemplateComposer`, `ProfileRepository`.
- **Sem** alteração de banco, de `AlertEvaluator`, de `OutcomeRepository`, nem do fluxo
  de produção. `BuildConfig.DEBUG` já existe (buildFeatures.buildConfig ligado).
