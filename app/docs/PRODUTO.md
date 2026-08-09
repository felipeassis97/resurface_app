# Resurface — Documento de Produto

> Consolidação das decisões tomadas até 2026-08-08.
> Escrito para ser levado ao repositório novo. Sem detalhe técnico — exceto a §10,
> que só reúne links oficiais para conferência das decisões na fonte.
> Decisões marcadas 🔒 estão fechadas. ⏳ dependem de teste. ✅ foram **medidas**.
>
> **Revisão de 2026-08-08 (noite):** a rodada de testes de detecção foi executada.
> Ela derrubou o desenho do contador, resolveu o R9 e removeu duas limitações.
> Evidência completa em `REELS.md` e `TIKTOK.md`; logs crus em `logs/`.

---

## 0. Contexto de construção

Não é detalhe de implementação — muda decisões de produto.

```
alvo            Android 16 (API 36)          mesmo do aparelho de referência
aparelho        Samsung SM-A536E · One UI 8
distribuição    NÃO vai pra Play Store       instalação direta (sideload)
natureza        projeto de estudo            não é produto comercial
```

**O que isso destrava:**

| Consequência | Efeito |
|---|---|
| Sem Play Store | A política da loja sobre a API de acessibilidade deixa de ser risco. O tipo de serviço `specialUse` deixa de exigir justificativa |
| `minSdk` pode ser alto | Quase nenhum código de compatibilidade. Sem backports |
| Um aparelho só | Não precisa lidar com variação de fabricante — só com o One UI |
| Não é comercial | Sem suporte, sem aquisição, sem monetização. O sucesso é o do `NEGOCIO.md` §6 |

**O que isso obriga** — `targetSdk 36` traz mudanças de comportamento que não são opcionais:

| Mudança | Impacto aqui |
|---|---|
| **Edge-to-edge obrigatório** (desde a 15, sem opt-out) | As telas de onboarding, ajustes e histórico precisam tratar insets |
| **Predictive back** ligado por padrão | Navegação entre telas precisa ser compatível |
| **Orientação e resizability ignorados** | Não dá mais pra travar em retrato. As telas têm que se comportar em qualquer tamanho |
| **Timeout de FGS** (desde a 15) | `dataSync` e `mediaProcessing` morrem após 6 h em 24 h. **Decide o D20** |

> 🔒 **Alvo fixado: Android 16 (API 36).** `compileSdk 36`, `targetSdk 36`, `minSdk 36`.
> Não subir pra 37. A 37 é Android 17 (prévia) e traz mudanças de comportamento que este
> documento **não analisou** — subir sem reanalisar reabriria riscos já fechados.
> ✅ **Aplicado (2026-08-09):** `app/build.gradle.kts` alinhado pra `compileSdk 36 /
> targetSdk 36` (era 37). Toda a validação de plataforma do `GAPS.md` rodou em `targetSdk 36`.

> 🔒 **Caminho fechado em 2026-08-08: A — ferramenta pessoal.**
> O app é construído para o próprio autor usar. Não é pesquisa (sem participantes,
> sem entregável acadêmico, sem prazo externo) e não é produto de mercado (não vai
> para a loja). Ver `NEGOCIO.md` §0.
>
> **Consequência direta no escopo:** nada entra por ser interessante de construir.
> O critério é *"isto testa o H1 ou sustenta o S1?"*. Se não, fica pra depois da v1.

---

## 1. O que é

Um app de Android que percebe quando você está há muito tempo em um app de vídeo curto
— Instagram e TikTok — e te avisa, com uma mensagem escrita no seu tom,
devolvendo a decisão pra você.

Não bloqueia. Não pune. Não julga. Só torna visível uma coisa que costuma passar
despercebida: quanto tempo já passou.

Depois, transforma esse histórico em observações sobre o seu próprio padrão de uso.

**Frase de uma linha:**
*"Você está no Instagram há 22 minutos. Ainda é isso que você quer estar fazendo?"*

> ✅ **Mudou na revisão:** era *"no Reels"*. O contador passou a medir o **app inteiro**,
> não a aba. Ver D14. A frase ficou menos específica e mais honesta — o app afirma
> exatamente o que consegue medir (P2).

---

## 2. Princípios

Estes vêm antes de qualquer feature. Se uma feature conflita com um princípio, a
feature cai.

| # | Princípio | Consequência prática |
|---|---|---|
| P1 | **Autonomia acima de tudo** | Nunca bloqueia, nunca prende, nunca esconde o botão de fechar. A decisão é sempre do usuário. |
| P2 | **Só afirma o que mede** | O app sabe quanto tempo passou. Não sabe o que você estava sentindo. Nunca diz "você estava no automático". |
| P3 | **Ignorar é uma resposta válida** | Cada aviso ignorado compra mais silêncio. O app recua, não insiste. |
| P4 | **Tudo local** | Nenhum dado sai do aparelho. Sem conta, sem servidor, sem nuvem. |
| P5 | **Sem culpa** | Nada de streak quebrado, ranking, cara triste, "você falhou". |
| P6 | **Silêncio é o estado padrão** | O app existe pra ser esquecido na maior parte do tempo. |

> ⚠️ **P2 sob pressão — a palavra "hesitou".**
> A rodada de testes confirmou que dá pra detectar um deslize iniciado e revertido
> (`dy` negativo no meio da rajada). Mas **"hesitou" é uma afirmação sobre estado
> mental**, da mesma família de "você estava no automático" — que o P2 proíbe.
> O que o app mede é: *o gesto começou e voltou*. É isso que ele pode dizer.
>
> ```
> ❌ "Você hesitou em 8% dos deslizes hoje"
> ✅ "8% dos seus deslizes começaram e voltaram"
> ```

---

## 3. Escopo da v1

### Dentro

| Item | Detalhe |
|---|---|
| **Instagram** | **O app inteiro** — Reels, feed, stories |
| **TikTok** | **O app inteiro** |
| Contagem de tempo | Um contador único que atravessa os dois apps |
| Aviso | Notificação do sistema ✅ **validado sobre tela cheia** |
| Botões no aviso | `[era hora]` / `[agora não]` — ver F7 |
| Perfil | Tom preferido + hobbies + limite de minutos |
| Mensagem personalizada | Gerada no aparelho a partir do perfil |
| Histórico | Sessões salvas localmente |
| Observações periódicas | Resumos do próprio padrão de uso |
| Registro pós-aviso | O que aconteceu depois de cada aviso — ver F7 |

### Fora — e por quê

| Item | Motivo |
|---|---|
| YouTube Shorts | Não medido. Barato de adicionar depois. |
| X, Reddit, YouTube comum | Não são vídeo curto. |
| Pulseira háptica | v2. O hardware já funciona, mas exige mudança na forma como o app roda. |
| Bloqueio / limite rígido | Viola P1. Nunca entra. |
| Conta, login, nuvem, social | Viola P4. Nunca entra. |
| Metas, streaks, gamificação | Viola P5. |

> ✅ **Saíram da lista "Fora": feed do Instagram e Stories.**
> Estavam excluídos por custo de detecção. Com o contador baseado em primeiro plano
> (D13), eles passam a contar **de graça** — separar as abas é que dá trabalho, não
> juntá-las. Ver D14 e a seção 7.

---

## 4. Features da v1

### F1 — Onboarding com perfil

Primeira abertura, três perguntas. Rápido, pulável exceto pelo limite.

**1. Como você quer ser lembrado?**

| Opção | Exemplo de mensagem |
|---|---|
| Direto | "22 minutos no Instagram." |
| Gentil | "Ei — já faz um tempinho por aqui. Tudo bem?" |
| Bem-humorado | "Placar: algoritmo 22, você 0." |

**2. O que você gosta de fazer?** (múltipla escolha + campo livre opcional)

Ler · Música · Exercício · Cozinhar · Jogos · Estudar · Sair com amigos · Séries/filmes

Usado só pra dar textura à mensagem. Nunca vira cobrança
("você devia estar lendo" está proibido — viola P5).

**3. Depois de quantos minutos você quer ser avisado?**

Padrão **20 min**. Faixa 10–60. Alterável a qualquer momento nos ajustes.

> 🔒 **Decisão:** perguntamos *tom*, não *idade*. Idade é um proxy indireto pra tom,
> coleta dado sensível e não melhora a mensagem.

> ⚠️ **Revisto: o quadro completo de permissões — não são só as duas de acesso especial.**
>
> O produto é uma notificação disparada por um serviço que roda o dia inteiro. Isso
> exige mais do que "acesso ao uso". Quadro completo, por natureza da permissão:
>
> ```
> ACESSO ESPECIAL  (tela do sistema, toggle, revogável)
>   Acesso ao uso   PACKAGE_USAGE_STATS         → OBRIGATÓRIA. É o contador. Sem ela não há produto.
>   Acessibilidade  BIND_ACCESSIBILITY_SERVICE  → OPCIONAL (D15). Só liga o F5 (contagem de vídeos,
>                                                  deslize revertido).
>
> RUNTIME  (diálogo do sistema, Android 13+)
>   Notificações    POST_NOTIFICATIONS          → OBRIGATÓRIA. Sem ela o aviso E a notificação fixa
>                                                  do FGS ficam mudos. O produto É a notificação.
>
> NORMAIS  (declaradas no manifesto, sem diálogo)
>   FGS             FOREGROUND_SERVICE                → o serviço que hospeda o relógio
>   FGS tipo        FOREGROUND_SERVICE_SPECIAL_USE    → + <property> no manifesto (D20)
>   Boot            RECEIVE_BOOT_COMPLETED            → religa o serviço após reiniciar o aparelho
>   Alarme exato    USE_EXACT_ALARM                   → dispara o aviso no minuto certo, através do Doze (D22)
>
> ISENÇÃO  (tela do sistema, não é permissão)
>   Bateria         REQUEST_IGNORE_BATTERY_OPTIMIZATIONS → TELA 3. Mitiga o congelamento do One UI (D23)
> ```
>
> **Três dessas eram gaps — não estavam no onboarding e matam o produto em silêncio:**
>
> | Faltava | O que acontece sem ela |
> |---|---|
> | `POST_NOTIFICATIONS` | O aviso não aparece. O produto inteiro fica invisível. |
> | `RECEIVE_BOOT_COMPLETED` + receiver | Depois de todo reinício do aparelho, o serviço fica morto até o app ser aberto na mão. `DEVICE_STARTUP` foi observado no log — isso acontece de verdade. |
> | Passo de *restricted settings* (a11y) | Android 13+ bloqueia ligar acessibilidade em app instalado fora da loja. Exige "Permitir configurações restritas" em Info do app — passo extra que a TELA 5 não mostrava. |
>
> O onboarding pede as **obrigatórias** (acesso ao uso + notificações) e deixa a
> **acessibilidade** como um "quer mais detalhe?" depois. A de acessibilidade continua
> a mais assustadora e continua **opcional** (D15) — o app funciona inteiro sem ela, só
> com estatísticas mais pobres. As normais são silenciosas (só manifesto). Ver o fluxo
> completo em 5.1 e as decisões D22 (disparo) e D23 (sobrevivência no One UI).
>
> Isso **melhora** o risco de onboarding do NEGOCIO.md §7, não piora — mas só depois de
> as três faltantes entrarem no fluxo.

---

### F2 — Contador de tempo em vídeo curto

O coração do produto. **Redesenhado na revisão de 2026-08-08.**

**Um contador só, atravessando os apps.**

```
15 min de TikTok  +  15 min de Instagram  =  30 minutos
```

Não são duas sessões de 15. É meia hora em vídeo curto — que é o que
importa e é o que a pessoa sente.

> 🔒 **Decisão (mantida):** contador único, não por app.
> **Por quê:** pular entre TikTok e Instagram é o comportamento mais comum de todos.
> Um contador por app seria derrotado por ele. As ferramentas nativas têm esse buraco.
>
> ✅ **Validado em dado real:** um episódio de 2026-08-08 atravessou os dois apps —
> Instagram até 18:00, TikTok até 18:22 — e o contador único somou **18 min 43 s**
> num episódio só. O Bem-Estar Digital teria mostrado "Instagram 39 min · TikTok 8 min"
> e nenhum alerta.

**Quando conta, quando pausa, quando zera:**

| Situação | O que acontece |
|---|---|
| App em primeiro plano | **Conta** |
| Trocou TikTok → Instagram | **Continua contando** — é a mesma atividade |
| Saiu do app | **Pausa** — não zera · `ACTIVITY_PAUSED` |
| Tela apagou | **Pausa** · ✅ `SCREEN_NON_INTERACTIVE`, mesma API, custo zero |
| Voltou em menos de 5 min | **Retoma de onde parou** |
| Ficou 5 min fora | **Zera** — é um novo episódio |

> ✅ **Sumiu da tabela: "parou de interagir por ~1,5 min → pausa".**
> Essa regra estava errada e teria quebrado o produto. Ver abaixo.

#### 🔒 Decisão nova — o tempo vem de primeiro plano, não de interação

O desenho original media tempo pelos eventos de acessibilidade: enquanto chegassem
eventos, a pessoa estaria usando. **A rodada de testes mostrou que isso não funciona.**

```
deslizando          →  23 ev/s      visível
ASSISTINDO PARADO   →   0 ev/s      INVISÍVEL por 158,7 s (medido)
saiu do app         →   0 ev/s      INVISÍVEL
```

Assistir e ter saído produzem **o mesmo sinal**. E é pior do que parece:

```
saiu 17 s do app        →  silêncio de  22,7 s
saiu 39 s do app        →  silêncio de  44,4 s
FICOU assistindo 158 s  →  silêncio de 158,7 s
```

Qualquer limiar acerta ao contrário. Um timeout de 60 s diria "ainda assistindo" pra
quem saiu por 39 s e "foi embora" pra quem estava vidrado por 2,5 minutos. Não é
ajuste de parâmetro — é ausência de sinal.

**A fonte do tempo passa a ser o `UsageStatsManager`.** Validado contra linha do tempo
real: acertou **6 de 6** transições entrar/sair, com precisão melhor que polling.

```
┌──────────────────────────────┬──────────────────────────────────┐
│ TEMPO  (F2, F3 — o produto)  │ COMPORTAMENTO  (F5 — o extra)    │
├──────────────────────────────┼──────────────────────────────────┤
│ UsageStatsManager            │ Acessibilidade (opcional)        │
│  · entrada, saída, duração   │  · contagem de vídeos            │
│  · por package               │  · deslize revertido             │
│  · NOTIFICATION_SEEN         │  · Reels vs feed vs comentários  │
└──────────────────────────────┴──────────────────────────────────┘
```

Ganhos medidos, além de simplesmente funcionar:

- **Sobrevive ao Samsung.** O One UI congela o processo do app a cada ~6 s
  (`FreecessHandler: freeze ... result : 8`). O `UsageStats` é gravado **pelo sistema** —
  se o app for congelado ou morto, você consulta depois e recupera o histórico inteiro.
- **Imune a updates do Instagram e do TikTok.** `ACTIVITY_RESUMED` de um package não
  muda quando o Instagram redesenha o Reels. Isso mata, **para o contador**, o risco
  de chance ALTA do NEGOCIO.md §7.
- **Bateria.** O sistema já grava isso pro Bem-Estar Digital. O app só lê.

---

### F3 — O aviso

Notificação normal do sistema. Aparece, pode ser deslizada pra fora, some.

> ✅ **R9 RESPONDIDO — a notificação aparece por cima do Reels em tela cheia.**
>
> ```
> 19:01:44  ACTIVITY_RESUMED  com.instagram.android
> 19:01:49  NOTIFICATION_INTERRUPTION + NOTIFICATION_SEEN
> 19:01:54  headsUpNotificationShowing: true      ← cartão na tela
> 19:01:55  dispensado sozinho, ~5 s depois
> ```
>
> **Consequência: o app NÃO precisa de `SYSTEM_ALERT_WINDOW`.** Uma permissão a menos,
> uma tela assustadora a menos no onboarding, e o P1 fica intacto — o cartão desliza
> pra fora e some sozinho.
>
> Exigência técnica: o canal precisa ser `IMPORTANCE_HIGH`. Canais em `DEFAULT` nunca
> fazem heads-up (foi o que invalidou duas tentativas de teste).
>
> ✅ **Reconfirmado com o NOSSO app (2026-08-09, G5).** O R9 original usou notificação de
> terceiro (Calendário). O probe postou o **próprio** aviso, canal `IMPORTANCE_HIGH` +
> os dois botões do F7, com o Instagram em tela cheia → SystemUI reportou
> `headsUpNotificationShowing: true`. O cartão do Resurface aparece por cima. **D7 de pé,
> sem `SYSTEM_ALERT_WINDOW`.** Ver `GAPS.md` G5.

**Ritmo: o intervalo dobra a cada aviso.**

```
limite de 20 min escolhido:

   20 min  →  1º aviso
   40 min  →  2º aviso
   80 min  →  3º aviso
  160 min  →  4º aviso

numa maratona de 2 horas: 3 avisos
```

> 🔒 **Decisão:** dobrar. É o princípio P3 escrito em matemática. Cada aviso ignorado
> é lido como "entendi, vou recuar mais".

**Trava de segurança:** no máximo 6 avisos por dia. Zera à meia-noite.

> ⏳ **Aberto — o dobro é por episódio ou por dia?**
> A tabela do F2 diz que 5 min fora zeram o episódio. Se o contador de avisos zerar
> junto, quem sai a cada 20 min recebe aviso a cada 20 min e o D4 nunca acontece.
> Se não zerar, dois episódios distantes somam. **Precisa de decisão explícita.**

**O que a mensagem pode e não pode dizer:**

| ✅ Pode | ❌ Não pode |
|---|---|
| Quanto tempo passou | Que você estava no automático |
| Quais apps | Que você desperdiçou tempo |
| Devolver a pergunta | Dizer o que você deveria estar fazendo |
| Referência leve a um hobby | Comparar com outras pessoas ou com ontem |

> 🔒 **D25 — qual app o aviso nomeia num episódio que atravessa os dois.** O aviso
> nomeia **o app em foco no instante do disparo**, não os dois. A releitura do D22
> ("acordar-pra-conferir") já sabe qual dos dois está na frente agora — sai de graça.
>
> ```
> episódio: 15 min Instagram + 7 min TikTok, TikTok em foco no minuto 22
>   ✅ "Você está no TikTok há 22 minutos."      ◄── tempo = total; app = foco agora
>   ❌ "Você está no Instagram e no TikTok..."   ◄── mais complexo, sem ganho
> ```
>
> Honesto pelo P2: o **tempo** é o acumulado real de vídeo curto; o **app** é onde a
> pessoa está no momento em que lê. Não afirma nada sobre o que ela fez antes.

---

### F4 — Mensagem escrita no seu tom

A mensagem é gerada no próprio aparelho, combinando o tom escolhido, os hobbies e
o momento (minutos acumulados, apps envolvidos, hora do dia).

**Sempre existe um texto pronto.** Se a geração falhar, ou se o aparelho não
suportar, entra uma mensagem escrita à mão do mesmo tom.

> 🔒 **Decisão:** as mensagens escritas à mão vêm primeiro; a geração vem depois,
> por cima. A geração no aparelho só existe em alguns modelos.

> 🔒 **Decisão:** a geração escolhe **como falar**, nunca **se avisa**.
> A decisão de avisar tem que ser previsível, auditável e a mesma em qualquer
> aparelho. É o que sustenta o P2.

> ✅ **R16 fechado por hardware: v1 sai só com mensagens à mão.**
> Gemini Nano roda via AICore, que é *gated* a flagship (Pixel 8+, Galaxy S24+). O
> aparelho de referência (SM-A536E / Galaxy A53) é mid-range e **não** está na lista de
> suporte. Avaliado trocar pelo **Galaxy A55 (SM-A556E)**: **não muda nada** — o A55
> (Exynos 1480) também é mid-range e não tem NPU certificada pro AICore; o gating é por
> hardware, não por RAM. Ambos = geração indisponível.
>
> **Consequência:** a geração no dispositivo sai do escopo da v1. O **D8** já era isso —
> as mensagens à mão são a fundação, não o plano B. A geração fica pra quando/se rodar
> num aparelho compatível, sem bloquear nada.

---

### F5 — Histórico e observações

Uma tela simples: seus episódios, quanto durou cada um, quais apps, se houve aviso.

#### O histórico é ilimitado — por arquitetura, não por retenção da API

```
┌──────────────────────────────────────────────────────────────────┐
│  UsageStatsManager   =  FONTE AO VIVO                            │
│                         janela curta, some sozinho               │
│                              │                                   │
│                              ▼  MonitorService lê a cada 45 s    │
│  Room                =  ARQUIVO SEU                              │
│                         episódios fechados · permanente          │
│                         ~1 KB por dia · cresce pra sempre        │
└──────────────────────────────────────────────────────────────────┘
```

Cada episódio que fecha (transição `PAUSADO → FORA` em 5.3) vira uma linha no Room.
A partir daí a retenção do sistema não importa mais — o dado é seu, no seu modelo.

#### Vocabulário disponível — medido no aparelho de referência, 24 h reais

```
320  ACTIVITY_RESUMED           ◄── entrada no app
318  ACTIVITY_PAUSED            ◄── saída
310  ACTIVITY_STOPPED
 56  STANDBY_BUCKET_CHANGED
 30  NOTIFICATION_INTERRUPTION  ◄── aviso disparou (o seu E os de terceiros)
 28  NOTIFICATION_SEEN          ◄── chegou aos olhos
 19  SHORTCUT_INVOCATION
 16  SCREEN_INTERACTIVE         ◄── tela acendeu
 15  SCREEN_NON_INTERACTIVE     ◄── tela apagou
 15  FOREGROUND_SERVICE_START
 14  FOREGROUND_SERVICE_STOP
  6  CONFIGURATION_CHANGE
  4  USER_INTERACTION
  2  DEVICE_SHUTDOWN            ◄── celular desligou
  1  DEVICE_STARTUP
  1  USER_UNLOCKED
```

Também disponível neste Android:
`android.app.usage.filter_based_event_query_api: true` — dá pra consultar só os dois
packages e só os tipos que interessam, em vez de iterar tudo a cada tick.

#### Três achados desta medição

**1. `SCREEN_NON_INTERACTIVE` — a regra "tela apagou → pausa" vem de graça.**
Está na tabela do F2 e não tinha fonte definida. Vem da mesma API, mesma consulta,
sem custo. A máquina de estados de 5.3 ganha uma transição sem ganhar dependência.

**2. `DEVICE_SHUTDOWN` / `DEVICE_STARTUP` — o backfill fica honesto.**
Sem eles, reconstruir episódios de dias anteriores confundiria "celular desligado"
com "pessoa parou de usar". Com eles, dá pra descontar os buracos.

**3. O sistema registra as notificações do Instagram e do TikTok.** Ver D17.

```
2  com.instagram.android     ig_heads_up_reminders_with_vibrations
4  com.zhiliaoapp.musically  recommend_video_push_associated_4
```

#### O que o app consegue dizer — separado por fonte

```
┌─ SÓ COM ACESSO AO USO (permissão obrigatória) ──────────────────┐
│ tempo por app · por episódio · por dia · semana · mês           │
│ nº de episódios · média · mediana · histograma de duração       │
│ hora do dia (mapa de calor)                                     │
│ Instagram vs TikTok, e episódios que cruzam os dois             │
│ episódios iniciados logo após notificação do próprio app (D17)  │
│ efeito dos avisos (ver F7)                                      │
│ comparação período a período                                    │
├─ PRECISA DA ACESSIBILIDADE (permissão opcional) ────────────────┤
│ contagem de vídeos ✅ validada: 16 contados à mão = 16 detectados│
│ deslizes iniciados e revertidos ✅ 8% Instagram · 6% TikTok      │
│ Reels vs feed vs comentários ✅ classes distintas                │
│ ritmo de varredura — vídeos por minuto                          │
└─────────────────────────────────────────────────────────────────┘
```

#### Esboço do dashboard

```
┌─ ESTA SEMANA ──────────────────────────────────────────────┐
│   4h 12min      23 episódios      média 11min              │
│   ▼ 18% vs semana passada                                  │
│                                                            │
│   seg ████████░░░░░░░░  38min                              │
│   ter ██████████████░░  62min                              │
│   qua ███░░░░░░░░░░░░░  14min                              │
│   qui ████████████████  71min   ← maior                    │
│   sex ██████░░░░░░░░░░  29min                              │
│                                                            │
│   por hora do dia:  23h ████████  ← concentração           │
│                                                            │
│   Instagram 71% · TikTok 29%                               │
│   6 episódios atravessaram os dois apps          (D2)      │
│   7 episódios começaram logo após notificação    (D17)     │
├─ AVISOS ───────────────────────────────────────────────────┤
│   9 disparados · 7 vistos                                  │
│   "era hora" 5 · "agora não" 2 · ignorados 2   → 71%  (S2) │
│                                                            │
│   saiu em <2 min após aviso:   4 de  9  (44%)              │
│   controle, sem aviso:         1 de 11  ( 9%)              │
│                    ▲ isso é o H1 sendo respondido          │
└────────────────────────────────────────────────────────────┘
```

O bloco de baixo é o que nenhuma ferramenta de tempo de tela mostra — e é a razão
de o F7 existir.

Exemplos de frases que o app pode afirmar:

> "Nesta semana: 23 episódios, média de 18 minutos. A maioria entre 23h e 1h."

> "8% dos seus deslizes começaram e voltaram hoje, contra 2% na semana passada."

> "7 dos seus 23 episódios começaram até 2 minutos depois de uma notificação do
> próprio app."

> "340 vídeos nos últimos 7 dias. Na semana anterior foram 210."

> ✅ **R14 RESPONDIDO — a contagem de vídeos é confiável.**
> Regra validada: `type=4096` · `class=androidx.viewpager.widget.ViewPager` · `dy≠0` ·
> `dx=0`, agrupado por gap > 0,5 s. Gabarito humano: 16 vídeos contados à mão,
> 16 detectados, erro zero. **Funciona igual no Instagram e no TikTok.**
> Rolar comentários usa outra classe (`RecyclerView`) e não infla a contagem.

> ⏳ **O único número não medido: a retenção do `queryEvents`.**
> O `dumpsys` imprime só as últimas 24 h **por design do dump**, não por limite da API
> (`Last 24 hour events`). Os arquivos em `/data/system/usagestats/0/` exigem root.
> Os quatro níveis agregados existem (`daily`, `weekly`, `monthly`, `yearly`), mas dão
> **tempo total por app**, não eventos — não servem pra reconstruir episódios.
>
> Só um app chamando `queryEvents` mede isso. **Primeira coisa a verificar quando o
> código existir:** consultar 30 dias atrás e ver de quando é o evento mais antigo que
> volta. Determina só o tamanho do backfill inicial — nada além disso.

> 💡 **Backfill na instalação, de graça.** Na primeira execução, uma consulta cobrindo
> a janela inteira reconstrói os últimos dias de episódios. O app nasce com histórico
> em vez de tela vazia. Bom pro S1 (sobrevivência): dá o que olhar no dia 1.

#### 🧪 Teste F-fresh — latência do `queryEvents` ao vivo · ✅ MEDIDO (2026-08-09)

> ✅ **Passou forte no SM-A536E.** Latência ocorreu→visível ~**1 s** nas duas direções
> (RESUMED e PAUSED), medido via `dumpsys usagestats` polado a 1 s. Limite de passe era
> 45 s. **Consequências:** o contador começa na hora; o poll de 45 s vê episódio
> recém-começado com folga enorme; a releitura "acordar-pra-conferir" do D22 lê estado
> fresco e a saída (`PAUSED`) não escapa dela. Ver `GAPS.md` G2. Roteiro original abaixo.

Diferente da retenção acima. A retenção pergunta *"até quando atrás o dado existe?"*.
Este pergunta *"quão fresco é o dado agora?"* — quanto tempo o sistema demora pra gravar
um `RESUMED` e torná-lo visível ao `queryEvents`. **Decide duas coisas do núcleo:** se um
poll de 45 s enxerga um episódio recém-começado, e se a releitura do D22 no disparo do
alarme vê estado fresco. Se a latência for alta, o contador começa atrasado e o
"acordar-pra-conferir" pode ler estado velho.

```
Roteiro (dá pra automatizar por adb, sem canal de instrução):
  1. anota T0 (relógio de parede)
  2. abre o Instagram em T0
  3. em T0+15s, T0+30s, T0+45s, T0+60s:
        chama queryEvents(T0−60s → agora)
        registra: o ACTIVITY_RESUMED de T0 já apareceu? com que timestamp?
  4. calcula latência = (instante em que o RESUMED ficou visível) − T0

Passa:  o RESUMED de T0 fica visível em ≤ 45 s (um intervalo de poll)
Falha:  latência > 45 s  → o contador começa atrasado; reavaliar o intervalo do tick
                            e/ou apoiar o começo do episódio em evento de a11y quando ligada
```

> Vale medir também o outro lado: a latência do `PAUSED`. Se `PAUSED` demora a aparecer,
> a releitura do alarme pode ver "ainda em foreground" logo após a pessoa sair. A
> releitura do D22 mitiga (relê no disparo), mas o número informa a margem.

---

### F6 — Ajustes

Trocar o limite de minutos · trocar o tom · editar hobbies · pausar o app por hoje ·
apagar todo o histórico · ligar/desligar a permissão de acessibilidade.

**"Pausar por hoje" é obrigatório.** Existem dias em que a pessoa quer assistir e
pronto. Um app que não aceita isso vira aquilo que ela desinstala na terceira semana.

> 🔒 **Decisão — "pausar" silencia, não desliga o serviço.** É um flag `pausadoAté =
> próxima meia-noite`, checado no caminho do aviso (`reagendarAlarme()` cai no `senão`
> e cancela o alarme). O contador continua rodando, o histórico fica completo, a
> notificação fixa continua. Desligar o FGS seria mais complexo e arriscado — perde a
> notificação fixa, esbarra na restrição de iniciar FGS em segundo plano pra religar, e
> pode não voltar. Silenciar é um booleano; parar é um problema de ciclo de vida.
> Já bate com o fluxo do 5.4 ("pausado por hoje? → não faz nada").

---

### F7 — Registro pós-aviso · **NOVO**

**Sem isto, o H1 não tem resposta — e o H1 é a hipótese que pode matar o projeto.**

O `NEGOCIO.md` §0 conta que o caminho B (pesquisa) travou por falta de um mecanismo
de rotulagem. A v1 precisa desse mecanismo, em escala menor.

**Dois botões no próprio aviso:**

```
        ┌──────────────────────────────────────┐
        │  Você está há 22 minutos por aqui.   │
        │                                      │
        │   [ era hora ]      [ agora não ]    │
        └──────────────────────────────────────┘
```

> 🔒 **Decisão — Q1 respondido: sim, o aviso leva botões.**
> Eles não são UX, são **instrumento de medida**. Um toque, sem tela, sem formulário.
> Alimentam a métrica S2 do NEGOCIO.md ("≥70% 'sim, era hora'"), que sem isso não existe.
> Não violam P1: ignorar os dois continua sendo resposta válida (P3).

**O lado objetivo vem de graça.** O `UsageStats` registra as notificações do próprio app:

```
NOTIFICATION_INTERRUPTION   →  o aviso disparou, quando
NOTIFICATION_SEEN           →  chegou aos olhos
ACTIVITY_PAUSED em seguida  →  saiu, e quanto tempo depois
(nenhum PAUSED)             →  continuou
```

Classificação por aviso: saiu em <30 s · <2 min · <5 min · continuou.

**O grupo de controle sai de graça:** pela regra do dobro (D4), a maioria dos episódios
longos passa **sem** aviso. Esses são o controle natural.

---

## 5. Como funciona — fluxos

Quatro fluxos e um exemplo real. Tudo aqui é consequência das decisões D13–D16.

### 5.1 Primeira abertura — onboarding

```
┌─────────────────────────────────────────────────────────────────┐
│  usuário abre o Resurface pela primeira vez                     │
└────────────────────────────┬────────────────────────────────────┘
                             ▼
              ┌──────────────────────────────┐
              │  TELA 1 — o que é            │
              │  "Um relógio pro Reels."     │
              │  1 frase. Um botão.          │
              └──────────────┬───────────────┘
                             ▼
              ┌──────────────────────────────┐
              │  TELA 2 — ACESSO AO USO      │  ◄── OBRIGATÓRIA
              │  "Preciso saber quais apps   │      sem ela não há produto
              │   estão abertos, e por       │
              │   quanto tempo. Só isso."    │
              └──────────────┬───────────────┘
                             ▼
                    abre Ajustes do sistema
                             │
                ┌────────────┴────────────┐
                ▼                         ▼
            concedeu                   negou
                │                         │
                │                         ▼
                │              ┌──────────────────────┐
                │              │ explica de novo,     │
                │              │ oferece tentar.      │
                │              │ Não insiste (P1).    │
                │              └──────────────────────┘
                ▼
   ┌──────────────────────────────┐
   │  TELA 2-b — NOTIFICAÇÕES     │  ◄── OBRIGATÓRIA (Android 13+)
   │  POST_NOTIFICATIONS          │      sem ela o AVISO não aparece
   │  "Preciso poder te avisar."  │      — o produto É a notificação
   │   diálogo do sistema         │
   └──────────────┬───────────────┘
                  ▼
   ┌──────────────────────────────┐
   │  TELA 3 — bateria + suspensão│  ◄── por causa do One UI (D23)
   │  "O Samsung congela apps em  │      medido: freeze a cada ~6 s
   │   segundo plano. Sem isso o  │      camada 3+4 do D23:
   │   aviso pode atrasar."       │      · isenção de bateria
   │   [ permitir ]  [ depois ]   │      · + abrir 'apps que nunca
   │   depois: abre 'apps que     │        dormem' com print (manual)
   │   nunca dormem' (print)      │
   └──────────────┬───────────────┘
                  ▼
   ┌──────────────────────────────┐
   │  TELA 4 — perfil (F1)        │
   │  1. Como quer ser lembrado?  │   direto · gentil · bem-humorado
   │  2. O que gosta de fazer?    │   múltipla escolha
   │  3. Avisar após quantos min? │   padrão 20
   └──────────────┬───────────────┘
                  ▼
   ┌──────────────────────────────┐
   │  TELA 5 — acessibilidade     │  ◄── OPCIONAL (D15)
   │  "Quer saber quantos vídeos  │      pode pular sem perder nada
   │   passaram? Isso precisa de  │      do produto principal
   │   outra permissão. Dá pra    │      ⚠ sideload: exige antes
   │   ligar depois."             │        'permitir config.
   │   [ ligar ]   [ agora não ]  │        restritas' em Info do app
   └──────────────┬───────────────┘
                  ▼
   ┌──────────────────────────────┐
   │  MonitorService inicia       │
   │  notificação fixa, mínima    │
   └──────────────┬───────────────┘
                  ▼
        ┌────────────────────────┐
        │  o app some.           │  ◄── P6: silêncio é o padrão
        │  Não abre mais sozinho.│
        └────────────────────────┘
```

> ✅ **A notificação fixa contra o P6 — resolvido pelo D21.**
> O Android obriga um serviço em primeiro plano a exibir notificação permanente. Em vez
> de fingir que ela não existe, ela **vira o contador vivo**:
>
> ```
> ┌────────────────────────────────┐
> │  Resurface · Instagram 12 min  │   prioridade mínima · sem som · recolhida
> └────────────────────────────────┘   dispensável no Android 13+
> ```
>
> Custo zero, e fecha a Q5 (*"vale mostrar o contador vivo?"*). Com interruptor nos
> ajustes pra desligar o número, porque a própria Q5 levanta o risco de virar obsessão.

> ⚠️ **Três telas entraram na revisão de permissões — antes faltavam e matavam o produto
> em silêncio.** Ver o quadro completo em F1.
>
> | Tela | Por quê é obrigatória | Sem ela |
> |---|---|---|
> | **2-b · Notificações** (`POST_NOTIFICATIONS`) | O aviso e a notificação fixa são notificações | O produto inteiro fica invisível |
> | **3 · +apps que nunca dormem** | Isenção de bateria sozinha não vence a Samsung (D23) | O aviso atrasa; a camada manual mitiga |
> | **5 · +restricted settings** | Android 13+ bloqueia a11y em app sideload | Não dá pra ligar a acessibilidade |
>
> Além delas, duas coisas **sem tela**, só código: `RECEIVE_BOOT_COMPLETED` + receiver
> (religa o serviço após reiniciar o aparelho — senão morre a cada reboot) e o alarme
> exato do D22 (dispara o aviso através do freeze/Doze).

### 5.2 O loop invisível

Roda pra sempre, em segundo plano. O usuário nunca vê.

```
              ┌──────────────────────────────────────┐
              │   MonitorService — tick a cada 45 s  │
              │   FGS tipo specialUse (D20)          │
              │   sem timeout de 6 h                 │
              └──────────────────┬───────────────────┘
                                 ▼
        ┌────────────────────────────────────────────────┐
        │  UsageStatsManager.queryEvents(último → agora)  │
        │                                                │
        │  devolve, do sistema:                          │
        │    ACTIVITY_RESUMED  com.instagram.android      │
        │    ACTIVITY_PAUSED   com.instagram.android      │
        │    ACTIVITY_RESUMED  com.zhiliaoapp.musically   │
        └────────────────────┬───────────────────────────┘
                             ▼
                 filtra: é um dos 2 alvos?
                             │
                ┌────────────┴────────────┐
               não                       sim
                │                         │
                ▼                         ▼
          dorme até o          ┌──────────────────────┐
          próximo tick         │  EpisodeEngine       │
                               └──────────┬───────────┘
                                          ▼
                               ┌──────────────────────┐
                               │  EpisodeEngine        │
                               │  atualiza estado +    │
                               │  acumulado (5.3)      │
                               └──────────┬───────────┘
                                          ▼
                               ┌──────────────────────┐
                               │  reagendarAlarme()   │  ◄── NÃO dispara aqui (D22/D24)
                               │  agenda o alarme exato│      o disparo é do alarme,
                               │  pro instante do      │      não do tick
                               │  cruzamento do limite │
                               └──────────┬───────────┘
                                          ▼
                                   dorme até o próximo tick
```

> ⚠️ **O tick NÃO dispara o aviso — só mantém o estado (D22/D24).** Quem dispara é o
> `setExactAndAllowWhileIdle`, agendado por `reagendarAlarme()`. O tick de 45 s existe
> pra **manter o episódio** (detectar `PAUSED`, fechar episódio, corrigir deriva) e
> reagendar o alarme. Ele **tolera atraso**: se o processo congelar e o tick não rodar,
> o alarme exato ainda entrega o aviso na hora — é o ponto do D22.

> **Por que ~45 s:** é a granularidade da manutenção — detectar saída, fechar episódio,
> reagendar. A **precisão do aviso** não vem daqui (vem do alarme exato, no minuto certo);
> vem daqui a rapidez em perceber que a pessoa saiu. Barato: a leitura é do sistema.
>
> Nota: com o app congelado, mesmo esse tick pode atrasar — por isso o alarme carrega o
> disparo, e o `PAUSED` atrasado é coberto pela releitura "acordar-pra-conferir" do D22.

### 5.3 O coração — máquina de estados do episódio

É aqui que moram o D2 e o D3.

```
                    ┌─────────────┐
          ┌────────►│    FORA     │◄──────────┐
          │         │ acumulado=0 │           │
          │         └──────┬──────┘           │
          │                │                  │
          │      RESUMED de um alvo           │ passou 5 min
          │      (Instagram OU TikTok)        │ sem voltar
          │                │                  │  → fecha episódio
          │                ▼                  │  → grava no histórico
          │         ┌─────────────┐           │
          │         │   DENTRO    │           │
          │         │ acumulando  │           │
          │         └──┬───────┬──┘           │
          │            │       │              │
          │   PAUSED   │       │  RESUMED do OUTRO alvo
          │            │       │  ┌──────────────────────┐
          │            │       └─►│ NÃO MUDA NADA.       │
          │            │          │ Continua acumulando. │
          │            │          │      ▲ isso é o D2   │
          │            │          └──────┴───────────────┘
          │            ▼
          │     ┌─────────────┐
          └─────┤   PAUSADO   │
     RESUMED    │ relógio     │
     em < 5 min │ congelado   │
     retoma ────┤             │
                └─────────────┘
```

**O que saiu desta máquina:**

```
✂  "parou de interagir por 1,5 min → pausa"
```

Teria pausado quem estava assistindo. Medido: **158,7 s de silêncio total com a pessoa
vidrada num vídeo**. O estado agora vem do sistema — se o app está na frente, conta.

### 5.4 O minuto 20

```
       acumulado cruza o limite
                  │
                  ▼
       ┌──────────────────────┐
       │  pausado por hoje?   │──sim──► não faz nada (D11)
       └──────────┬───────────┘
                 não
                  ▼
       ┌──────────────────────┐
       │  já deu 6 avisos     │──sim──► não faz nada (D5)
       │  hoje?               │
       └──────────┬───────────┘
                 não
                  ▼
       ┌──────────────────────────────────────┐
       │  MessageComposer                     │
       │  tom + hobbies + minutos + hora      │
       └──────────────────┬───────────────────┘
                          ▼
       ┌──────────────────────────────────────────┐
       │  ┌────────────────────────────────────┐  │
       │  │ Você está no Instagram há 22 min.  │  │ ◄── canal IMPORTANCE_HIGH
       │  │ Ainda é isso que você quer estar   │  │     ✅ medido: aparece por
       │  │ fazendo?                           │  │     cima do Reels em tela
       │  │                                    │  │     cheia. Some em ~5 s.
       │  │  [ era hora ]     [ agora não ]    │  │
       │  └────────────────────────────────────┘  │
       └──────────────────┬───────────────────────┘
                          ▼
       ┌──────────────────────────────────────┐
       │  próximo limite = limite × 2 (D4)     │
       │  20 → 40 → 80 → 160                   │
       └──────────────────┬───────────────────┘
                          ▼
        ┌─────────────────────────────────────────────────┐
        │  OutcomeRecorder — grava as duas metades        │
        ├─────────────────────────────────────────────────┤
        │  SUBJETIVO (os botões — F7)                     │
        │    tocou "era hora" · "agora não" · ignorou     │
        │                                                 │
        │  OBJETIVO (de graça, do sistema)                │
        │    NOTIFICATION_INTERRUPTION  → disparou        │
        │    NOTIFICATION_SEEN          → chegou aos olhos│
        │    ACTIVITY_PAUSED depois?    → saiu em quanto? │
        │    nenhum PAUSED              → continuou       │
        └─────────────────────────────────────────────────┘
                          │
                          ▼
                  isso é o H1 sendo medido
```

### 5.5 Como teria funcionado — episódio real de 2026-08-08

Dados crus do aparelho de referência, em `logs/`:

```
17:45:58  ▶ RESUMED Instagram        FORA → DENTRO      acumulado 0:00
17:47:48  ⏸ PAUSED                   DENTRO → PAUSADO   acumulado 1:50
17:51:27  ▶ RESUMED Instagram        voltou em 3:39 → RETOMA
17:53:14  ⏸ PAUSED                                      acumulado 3:37
17:57:10  ▶ RESUMED Instagram        voltou em 3:56 → RETOMA
             ← aqui: 158 s sem tocar na tela.
                O desenho antigo teria PAUSADO. O novo continua contando.
18:00:22  ⏸ PAUSED                                      acumulado 6:49
18:04:07  ▶ RESUMED Instagram        voltou em 3:45 → RETOMA
18:10:29  ⏸ PAUSED                                      acumulado 9:12
18:13:45  ▶ RESUMED TikTok           ◄── TROCOU DE APP
                                         não zera. Mesma atividade. (D2)
18:22:16  ⏸ PAUSED                                      acumulado 18:43
                                                              ▲
                                         faltou 1:17 pro aviso de 20 min

18:37:54  ▶ RESUMED Instagram        ficou 15:38 fora → > 5 min
                                     ✂ ZERA. Episódio novo. (D3)
```

Um episódio de **18 min 43 s** atravessando Instagram e TikTok. O Bem-Estar Digital do
mesmo aparelho teria mostrado *"Instagram 39 min · TikTok 8 min"* e nenhum alerta —
porque conta por app e reporta no fim do dia.

### 5.6 O que a acessibilidade acrescenta, se ligada

Nada dos fluxos acima muda. Ela só enriquece o histórico:

```
episódio 17:45 → 18:22    18:43

    sem acessibilidade  →  "18 minutos, Instagram e TikTok"
    com acessibilidade  →  "18 minutos · 41 vídeos · 3 deslizes voltaram atrás
                            · 82% no Reels, 18% no feed"
```

Se o usuário nunca conceder, o produto continua inteiro.

---

## 6. Decisões de produto — resumo

| # | Decisão | Alternativa recusada | Motivo |
|---|---|---|---|
| D1 | ~~Só Reels e TikTok~~ → **Instagram e TikTok inteiros** | Só as abas de vídeo | Ver D14 |
| D2 | Contador único entre apps | Contador por app | Pular entre apps é o padrão. ✅ Validado em dado real (episódio de 18:43 atravessando os dois) |
| D3 | Pausar ≠ zerar, janela de 5 min | Fechar o app zera | Sair pra responder mensagem e voltar é comum demais. ✅ Implementável com D13 |
| D4 | Intervalo dobra a cada aviso | Fixo, ou uma vez só | Fixo vira ruído; uma vez só é invisível. ⏳ falta definir se é por episódio ou por dia |
| D5 | Teto de 6 avisos por dia | Sem teto | Rede de segurança |
| D6 | Tom, não idade | Perguntar idade | Mais direto, menos dado sensível |
| D7 | Notificação, não sobreposição | Cartão por cima do app | ✅ **Validado (R9).** Heads-up aparece sobre tela cheia. Dispensa `SYSTEM_ALERT_WINDOW` |
| D8 | Mensagens à mão primeiro | Só geração automática | Geração não existe em todo aparelho |
| D9 | A geração escreve, não decide | Deixar a IA decidir o aviso | Previsibilidade e P2 |
| D10 | Nunca bloquear | Bloqueio / limite rígido | P1. Não é negociável |
| D11 | Pausar por hoje | Sem escape | Sem isso, o app é desinstalado |
| D12 | Pulseira na v2 | Pulseira na v1 | Maior risco, menor valor no núcleo |
| **D13** | **Tempo vem de `UsageStatsManager`, não de eventos** | Contador dirigido por eventos de acessibilidade | **Medido: 158,7 s assistindo sem nenhum evento.** Assistir e ter saído são indistinguíveis. Não é ajuste de timeout, é ausência de sinal |
| **D14** | **Contar o app inteiro, não a aba** | Só a aba Reels / só o For You | Separar abas dá trabalho; juntá-las é grátis. Remove as limitações #1 e #2. Mais honesto pelo P2 |
| **D15** | **Acessibilidade é opcional** | Permissão obrigatória no onboarding | O produto inteiro funciona sem ela. Ela só liga o F5. Reduz o atrito da tela mais assustadora — e apps instalados fora da loja passam por um passo extra pra habilitá-la |
| **D16** | **O aviso leva dois botões** | Aviso sem ação | É o instrumento de medida do H1 e da S2. Sem ele, o projeto não sabe se funcionou |
| **D17** | **Registrar o que puxou a pessoa pra dentro** | Só medir o que acontece depois que ela entra | Medido: o sistema entrega `NOTIFICATION_INTERRUPTION` do Instagram e do TikTok. Dá pra correlacionar notificação de terceiro com início de episódio |

| **D18** | **O dobro do intervalo é por episódio** | Por dia | Zero estado persistido — morre com o episódio. E por dia produz frase mentirosa: um 2º aviso poderia cair no minuto 3 de uma sessão, dizendo "você está há 3 minutos por aqui". Fecha a **Q6** |
| **D19** | **TikTok permanece no escopo** | Só Instagram na v1 | Remover dá **mais** trabalho que manter: uma linha de código idêntica, mas exigiria reescrever D1, D2, e o §3/§4/H2 do `NEGOCIO.md`. Mataria o D2, já validado em dado real. Fecha a **Q7** |
| **D20** | **Serviço em primeiro plano, tipo `specialUse`** | Hospedar o relógio no serviço de acessibilidade | Falha suave vs falha na porta de entrada. E `specialUse` é o único tipo viável — ver abaixo. Fecha a **Q8** |
| **D21** | **A notificação fixa mostra o contador vivo** | Notificação fixa vazia ("está rodando") | O Android obriga a notificação a existir. Torná-la útil custa zero e fecha a **Q5**. Com interruptor nos ajustes, porque a própria Q5 levanta o risco de virar obsessão |
| **D22** | **O aviso é disparado por alarme exato (`setExactAndAllowWhileIdle`), não por polling de timer próprio** | Loop/`Handler` dentro do processo do serviço | Processo congelado **não roda timer próprio** (medido: freeze a cada ~6 s). O alarme exato é entregue pelo *sistema* e **atravessa Doze e o freezer** — acorda o app no instante calculado. ✅ **MEDIDO (G1): disparou 12 ms de atraso em Doze deep.** Fecha o Gap #1 sem tornar a acessibilidade obrigatória. Ver abaixo |
| **D23** | **Sobreviver ao One UI = 5 camadas, não só isenção de bateria** | Só pedir `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | A isenção padrão do Android **não** desliga a lista de suspensão própria da Samsung. Nenhuma API adiciona o app a "apps que nunca dormem" — só dá pra levar o usuário até a tela. Exige redundância + o app detectando o próprio atraso. Ver abaixo |
| **D24** | **Episódio aberto é derivável do `UsageStats`; recuperação = replay, não persistência** | Persistir episódio em andamento + estado de alarme | Tudo (eventos dos apps + avisos próprios) já está no `UsageStats`. Morreu → reconsulta 6 h + replay pela mesma EpisodeEngine. Zero estado frágil. Uma função `reagendarAlarme()` em toda transição. ✅ **Parcial (G3): `MY_PACKAGE_REPLACED` religou o FGS sozinho.** Falta o reboot físico. Ver abaixo |
| **D25** | **O aviso nomeia o app em foco no instante do disparo** | Nomear todos os apps do episódio, ou dizer "em vídeo curto" | Mais simples e já vem de graça: a releitura do D22 no acordar já diz qual dos 2 está em foco agora. O tempo é o total; o app nomeado é onde a pessoa está. Ver F3 |

### D17 — o que puxou a pessoa pra dentro

O `UsageStats` não registra só as notificações do próprio app. Registra as de todos —
incluindo `com.instagram.android` / `ig_heads_up_reminders_with_vibrations` e
`com.zhiliaoapp.musically` / `recommend_video_push_associated_4`, ambos observados no
aparelho de referência.

Correlacionando com o início dos episódios, o app pode afirmar:

> *"7 dos seus 23 episódios desta semana começaram até 2 minutos depois de uma
> notificação do próprio app."*

**Por que isso importa:** o `NEGOCIO.md` §1 diz que *"não é defeito do usuário; é o
produto funcionando como projetado"* — mas trata isso como premissa. Com o D17, vira
coisa medida, no próprio aparelho da pessoa.

**Por que cabe no P2:** é afirmação factual sobre dois timestamps, não inferência sobre
estado mental. O app **não** pode dizer "você foi manipulado". Pode dizer quantos
episódios começaram logo depois de um toque no ombro.

**Custo:** zero. Os eventos já vêm na mesma consulta.

### D18 — o dobro por episódio

```
POR EPISÓDIO                          POR DIA
────────────────────────────          ────────────────────────────
estado vive dentro do episódio        contador separado, persistido
morre quando o episódio fecha         precisa zerar à meia-noite
                                      precisa decidir o que fazer com
                                      episódio que cruza meia-noite
```

**Por dia não é só mais complexo — produz mensagem errada:**

```
09:00  episódio de 22 min  →  aviso 1. Próximo limite: 40 min do DIA.
23:00  episódio de 25 min  →  acumulado do dia = 47 min → dispara
                              mas você sentou há 3 minutos.
                              A frase seria "você está há 3 minutos por aqui".
```

O produto inteiro fala em episódio. Misturar escala de dia com frase de episódio
quebra o P2.

**Preço:** quem sai a cada 6 min pode receber aviso a cada 20 min de uso. O **D5**
já cobre — teto de 6 por dia. Chegar lá exige 2 h de uso em 6 sessões separadas.
**O D5 continua sendo por dia** — é o único estado diário que resta.

### D20 — serviço em primeiro plano, e por que `specialUse`

**Por que primeiro plano e não a acessibilidade:** o que decide é o modo de falha.

```
SERVIÇO EM PRIMEIRO PLANO             SERVIÇO DE ACESSIBILIDADE
─────────────────────────             ──────────────────────────
foi congelado / morto?                a11y volta a ser OBRIGATÓRIA
  → UsageStats é retroativo             → reverte o D15
  → nenhum dado se perde                → a permissão mais assustadora
  → o AVISO atrasa                        volta pro caminho crítico
  falha SUAVE                           falha na porta de entrada
```

O `NEGOCIO.md` §6 diz que *"a sobrevivência do app é pré-requisito de qualquer efeito"*.
Uma permissão a menos na porta vale mais que precisão no minuto 20. E a TELA 3 do
onboarding já pede isenção de bateria, mitigando o congelamento do One UI.

**Por que `specialUse` e não `dataSync`** — citação direta da documentação:

> *"The system permits `dataSync` and `mediaProcessing` foreground services to run for
> a total of **6 hours in a 24-hour period**, after which the system calls the running
> service's `Service.onTimeout(int, int)` method."*

`specialUse` **não aparece** nessa página. `shortService` tem limite ainda mais duro.

```
dataSync         →  morre às 6 h. Todo dia. Fatal pra um contador 24/7
mediaProcessing  →  idem
shortService     →  pior ainda
specialUse       →  sem timeout  ◄── único viável
```

E como o app **não vai pra Play Store** (seção 0), `specialUse` não exige a
justificativa que a loja pediria.

> ⚠️ **Requisito técnico do `specialUse`, independente da loja:** o manifesto ainda
> precisa declarar a permissão `FOREGROUND_SERVICE_SPECIAL_USE` **e** a
> `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" .../>` dentro
> do `<service>`. Sem isso o serviço lança exceção ao iniciar. "Não vai pra loja" só
> dispensa a *revisão* da justificativa — não a declaração.
>
> ✅ **MEDIDO (2026-08-09):** o probe subiu o FGS de contexto de foreground sem exceção —
> `isForeground=true types=0x40000000` (= `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`),
> `targetSdk 36`. A `<property>` valida. Iniciar de background (broadcast/`am`) é barrado
> pela restrição de start em segundo plano — por isso o start vem do onboarding
> (foreground) e do BootReceiver (contexto permitido). Ver `GAPS.md` G4.
> Pendente só o teste longo: 6 h+ sem `onTimeout` (confirma o tipo, D20).

### D22 — quem dispara o aviso (Gap #1)

O `UsageStatsManager` é **pull-only**: não existe evento pra assinar, é preciso
consultar. E o processo do app é **congelado pelo One UI a cada ~6 s** (medido). Um
`Handler`/loop dentro do processo não sobrevive — timer congelado não dispara. Esse é
o buraco: a fonte de tempo é passiva e o processo dorme.

**Dois papéis, dois mecanismos:**

```
MANUTENÇÃO DO ESTADO DO EPISÓDIO          DISPARO DO AVISO NO MINUTO CERTO
(barato, tolera atraso)                    (precisão exigida)
─────────────────────────────────          ──────────────────────────────────
tick leve quando o app já está acordado    ao entrar em DENTRO, calcula o instante
 · evento de a11y (se ligada), ou           em que o acumulado cruza o limite e
 · alarme inexato periódico                 agenda UM setExactAndAllowWhileIdle
reconstrói o episódio do UsageStats         para esse instante exato
não precisa ser pontual                     PAUSED cancela / reagenda
```

**Por que o alarme exato resolve:** `setExactAndAllowWhileIdle` é entregue pelo
sistema. Ele **acorda o app mesmo congelado ou em Doze** — é justamente o primitivo
desenhado pra atravessar o freezer. O disparo não depende mais de o processo estar vivo
no minuto 20. Usa `USE_EXACT_ALARM` (Android 13+), concedida na instalação para apps
cuja função central é lembrete cronometrado — sem diálogo, não revogável. É o caso: o
aviso **é** um lembrete cronometrado. (`SCHEDULE_EXACT_ALARM` é a alternativa revogável,
pior aqui.)

> 🔒 **O alarme é "acordar-pra-conferir", não disparo cego.** Ele não posta a
> notificação sozinho. Ao acordar, o app **relê o `UsageStats`** e decide:
>
> ```
> alarme dispara no instante calculado
>         │
>         ▼
> relê UsageStats (now-5min → now)   ◄── consulta fresca; o disparo descongelou o app
>         │
>    ┌────┴───────────────────────────────────────────────┐
>    ▼                        ▼                            ▼
> ainda em foreground      saiu (PAUSED que o           acumulado < limite
> E acumulado ≥ limite     tick congelado não pegou)    (deriva de relógio)
>    │                        │                            │
>    ▼                        ▼                            ▼
> POSTA o aviso            NÃO posta.                   reagenda pro instante
> dobra o limite (D4)      cancela. episódio já         corrigido. não posta
> agenda o próximo         estava fora                  ainda
> ```
>
> **Por que isso é necessário:** se a pessoa saiu no minuto 12 e o processo congelado
> não detectou o `PAUSED`, um disparo cego diria *"você está há 20 min por aqui"* com
> ela fora do app — mentira, viola o P2. A releitura no acordar elimina o falso disparo,
> e funciona porque a entrega do alarme **descongela** o app: a consulta ao `UsageStats`
> no instante do disparo é fresca. O alarme diz *"acorde e confira"*, não *"poste agora"*.

**"E se a acessibilidade deixar de ser obrigatória?"** — pergunta levantada na revisão.
Ela daria um processo que **sobrevive ao freeze** (medido: o serviço de a11y aguentou o
Freecess quando o app comum foi congelado — REELS P4), um host confiável pro relógio.
Mas:

```
1. não é preciso   →  o alarme exato já atravessa o freeze sem processo residente
2. custa caro      →  reverte D15/D20: a permissão mais assustadora volta pro caminho
                      crítico, com o passo de restricted settings junto
3. não acorda em   →  os eventos de a11y somem por 158 s assistindo parado. O ganho
   assistir parado    seria só "processo residente", nunca "eventos que avisam"
```

→ **A11y-obrigatória é plano B**, acionado só se o alarme exato falhar no aparelho de
referência. **D15 continua de pé.** Primeira coisa a validar quando o código existir:
um `setExactAndAllowWhileIdle` agendado dispara na hora com o app congelado pelo One UI?

> ✅ **MEDIDO no SM-A536E (2026-08-09) — o teste-mãe passou.** Um probe APK
> (`targetSdk 36`) agendou um `setExactAndAllowWhileIdle` e forçou Doze deep. Disparou
> com **`deltaMs=12`** (12 ms de atraso) e **`idleMode=true`** — o sistema descongelou o
> app e entregou o alarme no instante, em Doze profundo. **D22 confirmado no hardware.**
> O plano B (a11y obrigatória) **não** é necessário; o D15 fica de pé. Ver `GAPS.md` G1.
>
> Pendente só o caso extremo: Doze natural por horas + Freecess simultâneo (o teste usou
> Doze forçado). Sinal já é forte — 12 ms de margem.

### D23 — sobreviver ao One UI (Gap #3)

A isenção de bateria da TELA 3 (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) desliga o **Doze
padrão do Android** — mas **não** a lista de suspensão própria da Samsung ("Apps em
suspensão" / "Apps que nunca entram em suspensão"), que é agressiva e independente.
Nenhuma API adiciona o app a essa lista — só dá pra abrir a tela e instruir. `dontkillmyapp.com/samsung`
cataloga o comportamento.

Solução = **cinco camadas redundantes**, da mais robusta pra menos:

```
1. Alarme exato (D22)          o aviso acorda o app mesmo congelado. A pontualidade já
                                não depende do não-congelamento. PRINCIPAL camada.
2. FGS com notificação visível  a Samsung é menos agressiva com FGS que mostra cartão.
3. Isenção de bateria (TELA 3)  desliga o Doze padrão.
4. Passo manual (TELA 3-b)      levar o usuário a Cuidados do dispositivo → Bateria →
                                remover Resurface de "Apps em suspensão" e adicionar em
                                "Apps que nunca dormem". NÃO automatizável — só abrir a
                                tela + print com o passo a passo.
5. Autodiagnóstico              o app compara o instante esperado do último alarme/tick
                                com o real. Se houve buraco (indício de congelamento ou
                                adiamento), mostra nos ajustes: "os avisos podem estar
                                atrasando — confira apps em suspensão", com botão que
                                abre a tela. Torna a falha VISÍVEL em vez de silenciosa.
```

A camada 1 é o que muda o jogo: mesmo que o passo manual (4) não seja feito, o alarme
exato ainda entrega o aviso. As camadas 2–4 reduzem o atraso do *tick* de manutenção; a
5 garante que, se ainda assim falhar, o usuário saiba — em vez de o app silenciar sem
avisar. O modo de falha deixa de ser invisível, que era o pior do Gap #3.

> Nota: intents diretos pra tela de suspensão da Samsung são **não documentados e
> frágeis** entre versões do One UI. O caminho seguro é abrir Ajustes/Info do app e
> instruir com print. Não prometer deep-link estável.

### D24 — ciclo de vida do alarme e recuperação após morte

Duas peças que o D22 exige e que faltavam desenhar: **quando o alarme é (re)agendado**,
e **como o app se reconstrói se o processo morre no meio de um episódio.**

#### O estado é derivável — não persistir episódio aberto

Chave da simplicidade: **tudo do episódio em andamento sai do `UsageStats`.** Ele guarda
os eventos dos apps (`RESUMED`/`PAUSED`) **e** as notificações do próprio app
(`NOTIFICATION_INTERRUPTION` — quantos avisos já dispararam). Então o episódio aberto
**não precisa ser persistido** em lugar nenhum. O Room só guarda episódios **fechados**
(F5). Zero estado frágil em memória ou em disco pro episódio vivo.

```
estado do episódio aberto  =  replay(UsageStats, últimas 6 h)
                              └─ mesma EpisodeEngine do loop ao vivo (5.3)
```

#### Uma função só: `reagendarAlarme()`

Chamada depois de **qualquer** transição de estado. Idempotente: cancela o alarme atual
e, se as condições valerem, agenda **um** novo.

```
reagendarAlarme():
    cancela o alarme pendente (sempre)
    se  estado == DENTRO
        e  não pausado-por-hoje (D11)
        e  avisos-hoje < 6 (D5):
            restante = próximoLimite − acumulado
            agenda setExactAndAllowWhileIdle em  agora + restante
    senão:
        não agenda nada
```

Gatilhos que a chamam:

| Evento | Efeito via `reagendarAlarme()` |
|---|---|
| `RESUMED` (entrou / retomou em <5 min) | agenda pro instante do cruzamento |
| `RESUMED` do outro alvo (D2) | recalcula com o acumulado atual; não zera |
| `PAUSED` | cai no `senão` → cancela (relógio congela) |
| Alarme disparou e **postou** (D4) | `próximoLimite ×= 2`, reagenda o próximo |
| Alarme disparou e **não** postou (saiu/deriva) | reagenda ou cancela, conforme a releitura |
| Episódio fechou (5 min fora, 5.3) | estado→FORA → cancela |
| "Pausar por hoje" ligado (D11) | cancela. Desligado → reagenda |
| Teto de 6 avisos batido (D5) | cancela |
| **Reboot** (`BOOT_COMPLETED`) | reconstrói + reagenda (abaixo) |
| **App atualizado** (`MY_PACKAGE_REPLACED`) | idem reboot |

#### Recuperação após morte — o caminho mais simples

Cold start, reboot, crash: **um único caminho, reaproveitando a EpisodeEngine.**

```
serviço inicia (por qualquer motivo)
        │
        ▼
queryEvents(agora − 6 h → agora)  filtrado nos 2 packages + notificações próprias
        │
        ▼
replay pela EpisodeEngine (5.3)     ◄── mesma máquina do loop ao vivo, nada novo
        │
        ▼
estado reconstruído: FORA | DENTRO | PAUSADO
 · acumulado do episódio aberto
 · quantos avisos já dispararam  → define o próximoLimite (20→40→80…)
        │
        ▼
reagendarAlarme()
```

**Por que 6 h de janela:** longa o bastante pra conter qualquer episódio real; um
episódio de 6 h+ já passou de todos os limites e do teto de 6 avisos, então perder o
começo exato dele não muda nenhuma decisão. Bound fixo, simples, seguro.

**O que NÃO se faz:** nenhuma tabela de "episódio em andamento", nenhum `SharedPreferences`
de estado de alarme, nenhuma reconciliação. Morreu? Reconsulta e replay. O contador é do
sistema (D13) — reconstruir é grátis e sem perda.

> ✅ **MEDIDO no reboot (2026-08-09, G3) — duas condições pro religamento:**
> 1. **Primeiro desbloqueio pós-boot.** Por FBE (criptografia por arquivo), o
>    `BOOT_COMPLETED` só chega **depois** que o usuário desbloqueia o aparelho uma vez. Até
>    lá o serviço fica morto. Aceitável (o usuário desbloqueia logo). Se um dia for preciso
>    religar antes, exige `LOCKED_BOOT_COMPLETED` + receiver `directBootAware` — hoje
>    desnecessário.
> 2. **Isenção de bateria.** O start do FGS no boot só foi permitido com o app na allowlist
>    (`SYSTEM_ALLOW_LISTED`). Sem a isenção da TELA 3, pode ser barrado. Reforça a TELA 3 no
>    onboarding. Confirmado: com as duas, o FGS voltou sozinho, sem abrir o app.

---

## 7. Limitações declaradas da v1

Escritas aqui de propósito, pra não virarem "bug" depois.

**Removidas nesta revisão:**

| Antes | Por que sumiu |
|---|---|
| ~~O feed do Instagram não conta~~ | D14 — o app inteiro conta |
| ~~Stories não contam~~ | D14 — o app inteiro conta |
| ~~Celular largado com a tela acesa pode contar até ~1,5 min a mais~~ | A regra de inatividade caiu com D13. Ver a nova limitação 4 |

**As que valem:**

1. **YouTube Shorts não conta.**
2. **O app não distingue Reels de feed no aviso.** Ele diz "no Instagram". Com a
   permissão opcional de acessibilidade, o **histórico** consegue separar — o aviso não.
3. **Celular largado com a tela acesa conta como uso.** Se você deixar o Instagram
   aberto e sair da sala, o contador continua. Só a tela apagando pausa.
   **Não há solução barata:** o logger não expõe leitura de acelerômetro, e via eventos
   de acessibilidade "celular na mão" e "celular na mesa" são indistinguíveis (medido).
4. **Duas permissões de acesso especial** — uma obrigatória (acesso ao uso), uma
   opcional (acessibilidade).
5. **O One UI congela apps em segundo plano.** Medido: `freeze com.resurface.app` a
   cada ~6 s. O contador sobrevive porque o `UsageStats` é do sistema, mas o app precisa
   pedir isenção de otimização de bateria pra disparar o aviso na hora certa.
6. **Atualizações dos apps podem quebrar a detecção de comportamento** (contagem de
   vídeos, superfície). **O contador de tempo não quebra** — depende só do nome do package.
7. **A mensagem gerada não existe em todo aparelho.** Nesses, entram as escritas à mão.
8. **O app não sabe se você estava distraído.** Ele sabe quanto tempo passou.

---

## 8. Depois da v1

Ordem por valor, não por facilidade.

| Fase | O que entra | Por que depois |
|---|---|---|
| v1.1 | Separar Reels/feed/stories **no aviso**, não só no histórico | Só vale se a limitação nº 2 incomodar de verdade |
| v1.2 | Ajuste do limite baseado no seu próprio uso | Precisa de semanas de histórico |
| **v2** | **Pulseira háptica** — aviso no pulso, sem tela | Canal sem tela é qualitativamente diferente. Hardware pronto |
| v2 | YouTube Shorts | Ampliar cobertura depois que o núcleo funciona |
| v3 | Exportar o próprio histórico | Só importa com meses de dado |

---

## 9. Perguntas de produto ainda abertas

| # | Pergunta | Como decidir | Estado |
|---|---|---|---|
| Q1 | ~~O aviso deve oferecer botões?~~ | — | ✅ **Sim.** Ver D16 e F7 |
| Q2 | 20 min é um bom padrão? | Duas semanas de uso próprio respondem | **aberto** — só o uso responde |
| Q3 | 5 min de janela de retorno é o número certo? | Registrar cada pausa e olhar a distribuição real | **aberto** — só o uso responde |
| Q4 | O aviso deve mudar de tom conforme a hora? | 1h da manhã pede outra coisa que 15h | **aberto** — só o uso responde |
| Q5 | ~~Vale mostrar o contador vivo em algum lugar?~~ | — | ✅ **Sim, na notificação fixa.** Ver D21 |
| Q6 | ~~O dobro é por episódio ou por dia?~~ | — | ✅ **Por episódio.** Ver D18 |
| Q7 | ~~TikTok continua no escopo?~~ | — | ✅ **Sim, permanece.** Ver D19 |
| Q8 | ~~Quem hospeda o relógio?~~ | — | ✅ **Serviço em primeiro plano, `specialUse`.** Ver D20 |

**Nenhuma pergunta aberta bloqueia código.** Q2, Q3 e Q4 são calibração de número —
só semanas de uso real respondem, e todas têm padrão razoável definido.

---

## 10. Referências das APIs

> A única seção técnica do documento. Está aqui para que as decisões D13–D16 possam
> ser conferidas na fonte, não porque o documento tenha virado especificação.
> Todos os links verificados em 2026-08-08 (HTTP 200, redirecionamentos resolvidos).

### Núcleo — o contador (D13)

| API | Link |
|---|---|
| `UsageStatsManager` | https://developer.android.com/reference/android/app/usage/UsageStatsManager |
| `UsageEvents.Event` — `ACTIVITY_RESUMED`, `ACTIVITY_PAUSED`, `NOTIFICATION_SEEN`, `NOTIFICATION_INTERRUPTION` | https://developer.android.com/reference/android/app/usage/UsageEvents.Event |
| `UsageStats` | https://developer.android.com/reference/android/app/usage/UsageStats |
| Permissão `PACKAGE_USAGE_STATS` | https://developer.android.com/reference/android/Manifest.permission#PACKAGE_USAGE_STATS |
| `Settings.ACTION_USAGE_ACCESS_SETTINGS` — a tela da TELA 2 do onboarding | https://developer.android.com/reference/android/provider/Settings#ACTION_USAGE_ACCESS_SETTINGS |
| `AppOpsManager` — checar se a permissão foi concedida | https://developer.android.com/reference/android/app/AppOpsManager |

`UsageEvents.Event` é a página mais importante da lista: as constantes de evento são o
vocabulário inteiro da máquina de estados de 5.3.

### Alvo Android 16 (seção 0)

| | Link |
|---|---|
| Android 16 — visão geral | https://developer.android.com/about/versions/16 |
| **Mudanças de comportamento para `targetSdk 36`** | https://developer.android.com/about/versions/16/behavior-changes-16 |
| Edge-to-edge (obrigatório desde a 15) | https://developer.android.com/develop/ui/views/layout/edge-to-edge |
| Predictive back | https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture |

### Serviço em primeiro plano (5.2 · D20)

| | Link |
|---|---|
| Guia de foreground services | https://developer.android.com/develop/background-work/services/fgs |
| **Tipos de FGS** — obrigatório declarar desde o Android 14 | https://developer.android.com/develop/background-work/services/fgs/service-types |
| **Timeout de FGS** — a página que decidiu o D20 | https://developer.android.com/develop/background-work/services/fgs/timeout |
| `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` | https://developer.android.com/reference/android/content/pm/ServiceInfo#FOREGROUND_SERVICE_TYPE_SPECIAL_USE |
| **`PROPERTY_SPECIAL_USE_FGS_SUBTYPE`** — a `<property>` obrigatória no manifesto | https://developer.android.com/reference/android/content/pm/PackageManager#PROPERTY_SPECIAL_USE_FGS_SUBTYPE |
| Requisitos por versão (14+) | https://developer.android.com/about/versions/14/changes/fgs-types-required |
| Restrições de iniciar FGS em segundo plano | https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start |
| `RECEIVE_BOOT_COMPLETED` — religar o serviço após reboot (D22/D23) | https://developer.android.com/reference/android/Manifest.permission#RECEIVE_BOOT_COMPLETED |
| `ACTION_BOOT_COMPLETED` | https://developer.android.com/reference/android/content/Intent#ACTION_BOOT_COMPLETED |

> ✅ **A página de timeout resolveu o tipo de FGS.** Citação direta:
>
> *"The system permits `dataSync` and `mediaProcessing` foreground services to run for
> a total of 6 hours in a 24-hour period, after which the system calls the running
> service's `Service.onTimeout(int, int)` method."*
>
> `specialUse` não aparece nessa página. Para um contador que roda o dia inteiro,
> `dataSync` morreria às 6 h todo dia. Ver D20.

### Notificação (F3 · D7 · F7)

| | Link |
|---|---|
| Visão geral | https://developer.android.com/develop/ui/views/notifications |
| `NotificationChannel` | https://developer.android.com/reference/android/app/NotificationChannel |
| **Canais e importância** — é o que faz o heads-up existir | https://developer.android.com/develop/ui/compose/notifications/channels |
| Criar notificação e **botões de ação** (F7) | https://developer.android.com/develop/ui/compose/notifications/create-notification |
| `POST_NOTIFICATIONS` (Android 13+) | https://developer.android.com/develop/ui/compose/notifications/notification-permission |
| `NotificationCompat.Builder` | https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder |
| `PendingIntent` — para os dois botões do F7 | https://developer.android.com/reference/android/app/PendingIntent |

> A página de canais é onde está `IMPORTANCE_HIGH`. Foi exatamente o que invalidou
> duas tentativas de teste do R9: canal em `DEFAULT` nunca faz heads-up,
> independente de tela cheia.

### Bateria, disparo e sobrevivência (limitação 5 · D22 · D23)

| | Link |
|---|---|
| Doze e App Standby | https://developer.android.com/training/monitoring-device-state/doze-standby |
| `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — a TELA 3 do onboarding | https://developer.android.com/reference/android/provider/Settings#ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS |
| Mudanças do Android 12 (alarmes exatos) | https://developer.android.com/about/versions/12/behavior-changes-12 |
| **`setExactAndAllowWhileIdle`** — o disparo do D22, atravessa Doze/freeze | https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent) |
| **`USE_EXACT_ALARM`** (Android 13+) — concedida na instalação p/ lembrete cronometrado | https://developer.android.com/reference/android/Manifest.permission#USE_EXACT_ALARM |
| `SCHEDULE_EXACT_ALARM` — alternativa revogável | https://developer.android.com/reference/android/Manifest.permission#SCHEDULE_EXACT_ALARM |
| Guia de alarmes exatos | https://developer.android.com/develop/background-work/services/alarms/schedule-alarms#exact |
| AlarmManager | https://developer.android.com/develop/background-work/services/alarms |
| WorkManager | https://developer.android.com/develop/background-work/background-tasks/persistent |
| **Restricted settings** (a11y em app sideload, Android 13+) | https://support.google.com/android/answer/12623953 |

**Não oficial, mas o mais útil para o aparelho de referência:**
https://dontkillmyapp.com/samsung — cataloga o comportamento do One UI que medimos
(`FreecessHandler: freeze com.resurface.app` a cada ~6 s). Complementa a documentação
oficial onde ela é silenciosa.

### Módulo opcional — acessibilidade (F5 · D15)

| | Link |
|---|---|
| `AccessibilityService` | https://developer.android.com/reference/android/accessibilityservice/AccessibilityService |
| `AccessibilityServiceInfo` — `eventTypes`, `packageNames` | https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo |
| `AccessibilityEvent` — `TYPE_VIEW_SCROLLED` (4096), `TYPE_WINDOW_STATE_CHANGED` (32) | https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent |
| Guia do serviço | https://developer.android.com/guide/topics/ui/accessibility/service |
| **Política da Play Store sobre a API de acessibilidade** | https://support.google.com/googleplay/android-developer/answer/10964491 |

> A política da Play Store restringe apps que usam a API de acessibilidade para fins
> não relacionados a acessibilidade. Só importa no caminho C — e é **mais um argumento
> a favor do D15**: com a permissão opcional, uma eventual rejeição não mata o app.

### Armazenamento e mensagem

| | Link |
|---|---|
| Room — episódios, avisos, respostas | https://developer.android.com/training/data-storage/room |
| DataStore — o perfil do F1, mais leve que Room | https://developer.android.com/topic/libraries/architecture/datastore |
| Gemini Nano no dispositivo (F4 · R16) | https://developer.android.com/ai/gemini-nano |
| Índice de IA no Android | https://developer.android.com/ai |

> As duas últimas mudam rápido e o suporte varia por modelo de aparelho. **R16 fechado**
> (ver F4): nem o A53 nem o A55 têm AICore; a v1 sai só com mensagens à mão. O **D8** já
> era essa fundação, não plano B. Os links ficam pra quando/se rodar em aparelho compatível.

---

*Última atualização: 2026-08-09 (revisão de permissões e disparo — D22, D23; alvo fixado em Android 16)*
*Evidência: `REELS.md` · `TIKTOK.md` · logs crus em `logs/`*
*Companheiro: `NEGOCIO.md`*
