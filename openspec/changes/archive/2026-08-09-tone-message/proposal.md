## Why

O aviso hoje sai numa frase fixa hard-coded. O F2 (PRODUTO §F4) é a **mensagem no seu tom** —
escrita a partir do tom escolhido, dos hobbies e do momento. O modelo-alvo do produto é o Gemini
**Nano** (on-device, privado), mas o aparelho de referência (A53) não tem AICore (R16). Como o app
é a ferramenta do mestrado e a pesquisa é sobre *"nudge personalizado muda a decisão"* (não sobre
privacidade local), usamos o Gemini **cloud (Flash)** como **proxy fiel** do que o Nano entregaria:
mesma família, modelo pequeno, saída curta. A arquitetura fica **Nano-ready** — trocar cloud↔Nano é
uma linha de binding.

## What Changes

- **Perfil do usuário** (`ProfileRepository`, DataStore): tom (Direto/Gentil/Bem-humorado) + hobbies
  (múltipla escolha + campo livre). Definido no onboarding (passo novo) e editável nos Ajustes.
- **Base à mão** (`TemplateComposer`, domínio puro): pools de mensagens por tom, com slots
  (minutos, app, hora, hobby), rotação por seed. Curadas P2/P5-seguras. É a fundação (D8) e o fallback.
- **Interface `MessageGenerator`** (domínio) — o *design* do produto. Impls:
  - `CloudMessageGenerator` (Gemini Flash via REST, proxy do Nano) — agora.
  - `NanoMessageGenerator` — futuro, no hardware compatível (não implementado aqui; o seam existe).
- **Guard P2/P5** (`MessageGuard`, puro): rejeita saída gerada que afirme estado mental (P2) ou vire
  cobrança (P5) → cai no template. Fidelidade **e** segurança.
- **Pré-geração + cache**: a mensagem do próximo aviso é gerada no tick/agendamento e cacheada; no
  disparo posta a cacheada (a rede não bloqueia o momento). Rede/erro/vazio → fallback à mão.
- **Orquestração** no `AlertEvaluator`: perfil → generator → guard → (ok? usa : template) → Notifier.
  O `Notifier.postAlert` passa a receber o texto composto (não mais fixo). **D9 intacto**: a IA
  escreve COMO; a decisão de avisar (AlertPolicy) não muda.
- **Registro pro mestrado**: `alert_outcome` ganha o tom e a fonte (cloud/template) do que foi
  mostrado → Room v5. Permite comparar resposta por tom (H4).
- **Chave**: `GEMINI_API_KEY` em `local.properties` (gitignored) → `BuildConfig`. `local.properties.example`
  como referência. Sem chave → app inteiro roda no à mão.
- **Dep nova**: OkHttp (cliente REST); JSON via kotlinx-serialization (já no build).
- **Docs**: `PRODUTO.md` (F4/D8/R16) atualizado registrando a decisão do proxy cloud.

## Capabilities

### New Capabilities
- `user-profile`: o tom e os hobbies do usuário — definir no onboarding e editar nos ajustes.
- `message-composition`: compor a mensagem do aviso no tom — gerador (Gemini proxy) com guard P2/P5,
  pré-geração/cache, e fallback nas mensagens à mão.

### Modified Capabilities
- `alert-delivery`: o texto do aviso passa a ser composto (perfil + momento) em vez de fixo. Delta em
  `specs/alert-delivery/spec.md` (o disparo/agendamento não muda; só a origem do texto).

## Impact

- **Código novo:** `domain/` (Tone, Profile, Moment, Message, TemplateComposer, MessageGuard,
  MessageGenerator), `data/profile/` (ProfileRepository), `data/generation/` (CloudMessageGenerator,
  GeminiClient), cache, expansão de `AlertEvaluator`/`Notifier`, `alert_outcome` v5, passo de perfil
  no onboarding + ajustes. Testes dos puros (composer, guard, orquestração de fallback).
- **Dep:** OkHttp. **Build:** `buildConfigField GEMINI_API_KEY` lido do `local.properties`.
- **Rede:** só na pré-geração (não no disparo). Doze: a pré-geração roda no tick do FGS.
- **Privacidade:** relaxa o P4 (dado vai pro cloud) — **decisão consciente e documentada** pro
  contexto pessoal/mestrado; o modelo-alvo (Nano) restaura o P4 no futuro. Ver `PRODUTO.md`.
- **Não altera** o contador, o disparo, o outcome-resposta nem o dashboard (só a origem do texto + 1 coluna).
