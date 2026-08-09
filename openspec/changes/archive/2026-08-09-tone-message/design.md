## Context

F2 = mensagem no tom (PRODUTO §F4). Modelo-alvo é o Gemini Nano (on-device, restaura o P4), mas o
A53 não o suporta (R16). Como a pesquisa é sobre *"nudge personalizado muda a decisão"* e o app é
pessoal, usamos o Gemini **cloud (Flash)** como **proxy fiel** do Nano. A arquitetura fica atrás de
uma interface (`MessageGenerator`), então trocar cloud↔Nano é um binding. Sobre a base à mão (D8),
que é fallback e garante P2/P5. Segue o `ENGENHARIA` (G1 puro testável, G5 plataforma atrás de
interface, G9 dispatcher injetado).

## Goals / Non-Goals

**Goals:**
- Perfil (tom + hobbies) no onboarding + ajustes.
- Base à mão pura e testável (pools por tom, rotação, P2/P5 curados).
- `MessageGenerator` interface + `CloudMessageGenerator` (Gemini Flash) como proxy do Nano.
- Guard P2/P5 puro sobre a saída gerada; fallback à mão.
- Pré-geração + cache (rede fora do instante do disparo).
- Registrar tom + fonte no outcome (H4).

**Non-Goals:**
- Implementar o `NanoMessageGenerator` (não há hardware; só o seam fica pronto).
- Geração decidir SE avisa (D9 — nunca; a AlertPolicy manda).
- Variação de tom por hora (Q4 aberta) — a hora é slot disponível, sem lógica de variação ainda.
- Modelo grande/frontier (feriria a fidelidade da simulação — ver D-2).

## Decisions

### D-1: `MessageGenerator` interface = o design; impl troca (Nano-ready)
`interface MessageGenerator { suspend fun generate(profile: Profile, moment: Moment): Message? }`.
`CloudMessageGenerator` agora; `NanoMessageGenerator` no futuro. O `AlertEvaluator` depende só da
interface. Trocar = um `@Binds`.

### D-2: Modelo pequeno (Gemini Flash-Lite) pra simulação FIEL
Usa `gemini-2.0-flash-lite` (ou `flash`), o menor/rápido da família — representa o que um on-device
de poucos B params daria, não um frontier. Saída restrita (`maxOutputTokens ~40`, uma linha). Isso
sustenta a defesa metodológica: proxy fiel, não experiência inflada.

### D-3: REST direto via OkHttp (sem SDK)
`GeminiClient` faz POST em
`https://generativelanguage.googleapis.com/v1beta/models/<model>:generateContent?key=<KEY>`
com `{contents:[{parts:[{text}]}], generationConfig:{maxOutputTokens,temperature}}`; lê
`candidates[0].content.parts[0].text`. JSON com kotlinx-serialization. Dep nova: OkHttp. Chave vazia
ou erro → devolve null (→ fallback).

### D-4: Base à mão pura (`TemplateComposer`) — fundação e fallback (D8)
Pools por tom (`Map<Tone, List<Template>>`), `Template(title, body)` com slots `{min}{app}{hobby}`.
`compose(profile, moment, seed): Message` escolhe `pool[seed % size]`, preenche; se o template usa
`{hobby}` e não há hobby, escolhe um template sem hobby. `seed` = índice do aviso (rotação → variedade, H4).
Puro, testável; os textos são curados P2/P5 (o código preenche, a autoria protege).

### D-5: Guard P2/P5 puro (`MessageGuard`)
`fun isSafe(message: Message): Boolean` — rejeita padrões de cobrança (P5: "deveria/devia/larga o/
precisa parar") e de estado mental (P2: "no automático/vidrad/viciad/sem perceber…"), e tamanho
absurdo. Aplicado só à saída GERADA (os templates já são seguros). Reprova → fallback.

### D-6: Pré-geração + cache desacopla rede do disparo
`MessageCache` (@Singleton) keyed por `(episodeStartedAt, thresholdMinutes) → Message`. No
`reschedule` (tick), se cloud ligado e ainda não cacheado pra aquele limite, gera async → guard →
cacheia. No `onAlarmFired`, usa o cache; miss → `TemplateComposer`. Uma geração por limite (não por tick).

### D-7: Orquestração no `AlertEvaluator`, D9 intacto
Fluxo no disparo: decisão Fire → `message = cache ?: template.compose(...)` → `notifier.postAlert(message, id)`
→ `outcomes.recordFired(..., tone, source)`. A IA nunca decide Fire — a `AlertPolicy` (inalterada) decide.

### D-8: Chave via `local.properties` → `BuildConfig`
`app/build.gradle.kts` lê `GEMINI_API_KEY` do `local.properties` → `buildConfigField`. `local.properties`
é gitignored; `local.properties.example` é a referência. Sem chave → `CloudMessageGenerator` devolve
null sempre (fallback total).

### D-9: `alert_outcome` v5 — tom + fonte
`ALTER TABLE alert_outcome ADD COLUMN tone TEXT; ADD COLUMN source TEXT`. Migração 4→5 additiva.
Permite comparar resposta por tom (H4) na análise do mestrado.

## Risks / Trade-offs

- **[P4 relaxado]** dado de uso vai pro cloud na geração. Decisão consciente pro contexto pessoal/
  mestrado; documentado no PRODUTO; o Nano (alvo) restaura o P4. Só `{tom, minutos, app, hora, hobby}`
  vai no prompt — nunca conteúdo de tela.
- **[Rede em Doze]** a pré-geração roda no tick do FGS (processo vivo). Se falhar, fallback. O disparo
  nunca espera rede (D-6).
- **[LLM fere P2/P5]** o guard (D-5) descarta; e como o alvo é fiel (modelo pequeno + prompt restrito),
  o risco é gerenciado. O template é o piso seguro.
- **[Chave exposta]** vai no APK (BuildConfig). Pessoal, só teu aparelho — aceitável; não é pra loja.
- **[Custo]** ~≤6 gerações/dia. Centavos/mês. Irrelevante.

## Migration Plan

Room v4→v5: `ALTER TABLE alert_outcome ADD COLUMN tone TEXT` + `ADD COLUMN source TEXT`. Additiva, sem perda.

## Open Questions

- Flash vs Flash-Lite: começar no **Flash-Lite** (mais fiel ao "pequeno"); se a qualidade decepcionar,
  subir pra Flash. Decidir ao testar no aparelho.
- Toggle "usar geração" nos ajustes? Proposta: sim, um interruptor simples (default ligado se há chave),
  útil pra testar à-mão vs gerado e pra medir H4. Decidir na task.
