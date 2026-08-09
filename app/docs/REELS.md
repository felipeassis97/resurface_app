# Reels (Instagram) — Rodada de Testes de Detecção

> Objetivo: descobrir **que sinais o Instagram emite durante uso real** — o suficiente
> pra afirmar "está no Reels há X minutos" sem inventar nada.
>
> Device: Samsung SM-A536E · Android 16 (SDK 36) · One UI 8.0
> Package alvo: `com.instagram.android`
> Logger: `com.resurface.app` / `ResurfaceAccessibilityService`, tag `Mindless`
> Status: ✅ rodada concluída (R1, R2, R3-B, R4, R5) · R3 bloqueado por L1
> Última atualização: 2026-08-08

---

## Conclusão da rodada — leia isto primeiro

**O que funciona.** A detecção de comportamento é sólida e barata:

```
vídeo contado   ⟺  type=4096 · class=ViewPager · dy≠0 · dx=0 · agrupado por gap >0,5 s
hesitação       ⟺  o mesmo, com dy negativo dentro da rajada
superfície      ⟺  ViewPager = Reels · RecyclerView = feed ou comentários
sub-tela        ⟺  type=32, class distinta por tipo de tela
entrada no app  ⟺  type=32 ×2 em InstagramMainActivity, ~1 s antes do polling
```

Validado: 9 deslizes reais → 9 contados, 0 falsos positivos.
Hesitação apareceu em 8% dos gestos, com assinatura própria.

**O que não funciona.** O contador de tempo — o F2, o coração do produto:

```
deslizando   →  23 ev/s     visível
assistindo   →   0 ev/s     invisível por 158,7 s (medido, não é teto)
saiu do app  →   0 ev/s     invisível
```

Assistir e ter saído produzem o mesmo sinal. Não há limiar que separe — quem saiu
17 s gera um gap *menor* do que quem ficou assistindo 158 s.

**A consequência de arquitetura.** Tempo e comportamento precisam de fontes diferentes:

```
┌──────────────────────────────┬──────────────────────────────────┐
│ TEMPO (F2 · contador)        │ COMPORTAMENTO (F5 · observações) │
├──────────────────────────────┼──────────────────────────────────┤
│ presença em primeiro plano   │ eventos de acessibilidade        │
│ · UsageStatsManager, ou      │ · contagem de vídeos             │
│ · a11y sem filtro de package │ · hesitação                      │
│ · polling de getWindows()    │ · superfície e sub-telas         │
└──────────────────────────────┴──────────────────────────────────┘
```

Isso não estava previsto em PRODUTO.md nem NEGOCIO.md. Afeta **D3**, a **limitação #4**
e o desenho do F2.

**A recomendação que sai disso — medida, não teórica:**

`UsageStatsManager` como base do contador. Validado contra a linha do tempo do R4:

```
                    vigia dumpsys      UsageStats      eventos a11y
entrou              18:07:47.5         18:07:47        18:07:47.5  ✓
SAIU                18:08:31.4         18:08:30        — nada —    ✗
voltou (recentes)   18:08:48.6         18:08:47        18:08:47.9  ✓
SAIU                18:09:25.0         18:09:24        — nada —    ✗
voltou (alternar)   18:10:03.6         18:10:02        18:10:02.6  ✓
SAIU                18:10:30.8         18:10:29        — nada —    ✗
```

6 de 6 transições, mais preciso que polling. E cobre o R3-B inteiro
(`17:57:10 RESUMED → 18:00:22 PAUSED`, 3 min 12 s), os mesmos 158 s em que a
acessibilidade não viu nada.

Ganhos adicionais medidos:

- **Imune ao Freecess do Samsung** — o registro é do sistema, não do seu processo.
  Se o app for congelado ou morto, você consulta depois e recupera o histórico.
- **Imune a updates do Instagram/TikTok** — mata o risco de chance ALTA do
  `NEGOCIO.md` §7 para o contador. A detecção de comportamento continua frágil,
  mas ela é o extra, não o produto.
- **Registra as próprias notificações** (`NOTIFICATION_INTERRUPTION` / `NOTIFICATION_SEEN`)
  — o instrumento de medida do H1 vem junto.

Custo: segunda permissão (acesso ao uso), mais branda que a de acessibilidade.
Perda: "no Reels" vira "no Instagram" — e as limitações #1 e #2 **somem**
(feed e stories passam a contar).

### Simulação em dados reais — 2026-08-08

Aplicando D2 (contador único) + D3 (janela de 5 min) sobre o `usagestats` do dia:

```
16:05:10 → 16:05:27    0:16
16:10:55 → 16:19:09    4:04
16:32:40 → 16:38:10    4:24
16:45:15 → 16:52:03    3:55
16:57:23 → 17:06:46    6:12
17:20:16 → 17:39:45    6:22
17:45:58 → 18:22:16   18:43   ← atravessou Instagram E TikTok
18:37:54 → 18:46:06    6:04
```

O episódio de **18:43** é o D2 funcionando: 36 min de relógio, Instagram até 18:00,
TikTok até 18:22, fundidos porque as pausas foram menores que 5 min. Faltou
**1 min 17 s** para o aviso de 20 minutos disparar.

O Bem-Estar Digital teria mostrado "Instagram 39 min · TikTok 8 min" e nenhum alerta.

> Ressalva: esse dia foi uso de teste, não uso natural. A mecânica está validada;
> os números não representam comportamento real.

---

## Protocolo

**Não existe canal de instrução em tempo real.** Testado e descartado:

| Canal | Resultado |
|---|---|
| `say` no Mac | roda no sandbox do agente, não chega no fone/alto-falante |
| `echo` no terminal | saída do Bash não é exibida durante a execução |
| vibração via `adb shell cmd vibrator` | serviço indisponível no One UI 8 |
| notificação no celular | poluiria o teste (muda foco e gera eventos) |

Consequência: **roteiros com fases internas cronometradas não funcionam.** O protocolo é:

```
1. o agente inicia a captura em segundo plano
2. o roteiro é lido no chat, ANTES de começar
3. você executa no seu ritmo, sem cronômetro externo
4. você escreve "pronto"
5. o agente para a captura e reconstrói as fases pelos dados
```

### Marcador de fronteira

Fases são delimitadas **por gesto**, dentro do próprio log:

```
3 DESLIZES RÁPIDOS seguidos  =  marcador
```

Inconfundível: 3 gestos com <1,5 s entre eles, contra 3–20 s do uso normal.
Medido: marcador = 25 ev/s · uso normal parado = 0,4 ev/s.

### Vocabulário

| Termo | Significa |
|---|---|
| **HOME** | tela inicial do **Android**. Sai do Instagram, não fecha |
| **feed** | tela de posts do Instagram, primeira aba |
| **aba Reels** | ícone de vídeo na barra de baixo do Instagram |
| **deslize** | arrastar de baixo pra cima **dentro do player** |
| **fechar de verdade** | deslizar o card pra fora nos recentes — nunca é pedido |

---

## Formato do log

```
08-08 17:46:11.749 D/Mindless( 6640): event app=com.instagram.android type=4096 dtMs=6 dy=1125 dx=0 cc=null class=androidx.viewpager.widget.ViewPager
```

| Campo | Significado |
|---|---|
| `app` | package que gerou o evento |
| `type=32` | `WINDOW_STATE_CHANGED` — trocou de tela |
| `type=2048` | `WINDOW_CONTENT_CHANGED` — conteúdo mudou |
| `type=4096` | `VIEW_SCROLLED` — rolou |
| `dtMs` | ms desde o evento anterior (de qualquer tipo) |
| `dy` | deslocamento vertical do scroll |
| `cc` | `contentChangeTypes` (1=SUBTREE, 4=CONTENT_DESCRIPTION, 64=PANE_DISAPPEARED…) |
| `class` | classe da view de origem |

---

## Achados do preflight

Coletados sem gastar tempo de teste.

**P1 — O serviço é filtrado por package.**
Sequência `HOME → abrir Ajustes → rolar → HOME` produziu **zero eventos**. O
`AccessibilityServiceInfo` restringe `packageNames`. O serviço não enxerga o launcher
nem nenhum outro app.

**P2 — Sair do app não gera evento.**
`Instagram em foco → HOME` → `ev=4, type=32=0`, depois silêncio absoluto. Voltar
gera `type=32` ×2. **Entrada é detectável, saída não.**

**P3 — O logger lê o acelerômetro.**
`D/SensorManager(6640): registerListener :: LSM6DSOTR Accelerometer, 20000, 5000000`
ao entrar no app, `unregisterListener` ao sair. Sinal disponível para separar
"celular na mão" de "celular na mesa" (R3).

**P4 — One UI congela o app de fundo.**
```
D/FreecessHandler(1656): freeze com.resurface.app(11301) result : 8
```
Repetido a cada ~6 s. O serviço de acessibilidade é privilegiado e sobreviveu, mas o
app de produção vai precisar de isenção de otimização de bateria. **Não estava em
nenhum risco do NEGOCIO.md.**

---

## R1 — Scroll contínuo · CONCLUÍDO

**Pergunta:** qual o teto de volume de eventos? Dá pra contar vídeos?

Rodado duas vezes: cadência rápida e cadência lenta.

#### Relatório

**Volume**

| Cenário | ev/s | `type=4096` |
|---|---|---|
| Deslizando rápido (25 gestos / 45 s) | 23 | 81 |
| Deslizando lento (12 gestos / 45 s) | ~6 | 36 |
| Fora do app | 0 | 0 |

93% dos eventos são `type=2048 cc=1` (subtree changed) — ruído puro. Filtrar esse
tipo no serviço corta o volume em uma ordem de grandeza sem perder informação útil.

**A assinatura do deslize**

Um deslize de vídeo = **3 eventos `type=4096` em ~200 ms**, todos com
`class=androidx.viewpager.widget.ViewPager`:

```
[ parcial ,  quase-cheio ,  resto ]
  320–930    1130–1578      2–390
              ↑ o do meio é sempre o maior
```

Confirmado nas duas cadências. Exemplos reais:

```
rápido:  1021  990    31        lento:  478  1326  238
          349  1554  139                 320  1335  387
         1191   838    13                696  1316   30
```

Contagem = agrupar `type=4096` por gap > 0,5 s. Cada grupo = 1 vídeo.
Grupos de 5–6 eventos são dois deslizes colados (gap < 0,5 s) e precisam ser
subdivididos — ocorreu em ~8% dos gestos na cadência rápida.

**Superfície: Reels vs feed**

```
aba Reels  →  class=androidx.viewpager.widget.ViewPager       dy ≈ 300–1600
feed       →  class=androidx.recyclerview.widget.RecyclerView dy variável, e dy=0
```

Discriminador funciona. Resolve a limitação #1 do PRODUTO.md — dá pra saber que a
pessoa está no Reels e não no feed, sem ler conteúdo nenhum.

**Hesitação (H7) é mensurável**

Dois gestos na rodada rápida tiveram `dy` negativo no meio da rajada:

```
393  -323  -66  -4      ← começou a deslizar e voltou
285  -245  -38  -2
```

2 em 25 gestos = 8%. **H7 sai de "promissor" para "detectável com assinatura própria".**
Não era garantido que aparecesse.

**Aberto:** não temos gabarito humano da contagem de vídeos — nas duas rodadas o áudio
de instrução nunca chegou e a contagem não foi feita. A validação `3 eventos = 1 vídeo`
está apoiada na consistência do padrão, não em contagem independente.

---

## R2 — Scroll espaçado · CONCLUÍDO

**Pergunta:** o log fica mudo enquanto a pessoa assiste sem tocar na tela?

Executado: marcador → 4 vídeos de ~20 s sem tocar → marcador → HOME.
Marcadores e intervalos saíram perfeitos (28,7 s · 21,7 s · 20,8 s · 19,6 s).

#### Relatório

**Sim. Fica mudo — e isso quebra o desenho do contador.**

| Bloco | Duração | Eventos | ev/s | Maior silêncio |
|---|---|---|---|---|
| Marcador (3 deslizes) | 4 s | 101 | **25,25** | 0,8 s |
| vídeo 1 parado | 26 s | 48 | 1,85 | 9,5 s |
| vídeo 2 parado | 19 s | 7 | 0,37 | **17,8 s** |
| vídeo 3 parado | 18 s | 8 | 0,44 | 3,9 s |
| vídeo 4 parado | 16 s | **0** | **0,00** | — |
| fora do app | — | 0 | 0,00 | — |

Nenhum `type=4096` durante os blocos parados. Os poucos `type=2048` que aparecem vêm
em rajada única (`cc=4` seguido de `cc=1`), provavelmente quando o vídeo reinicia.

**A consequência**

```
assistindo parado   →  silêncio
saiu do app         →  silêncio
                       ▲
              indistinguíveis
```

O fluxo de eventos de acessibilidade, sozinho, **não sabe** se a pessoa está vidrada
num vídeo ou se foi embora. Um contador dirigido só por eventos ou:

- pausa quem está assistindo de verdade (falso negativo), ou
- conta tempo de quem já saiu (falso positivo)

Não existe ajuste de timeout que resolva — é ambiguidade de sinal, não de limiar.

**O que isso exige do app**

Precisa de uma segunda fonte que diga "o app ainda está em primeiro plano". Opções:

| Opção | Custo |
|---|---|
| Tirar o filtro de `packageNames` do serviço | grátis em permissão; mais eventos, mais bateria |
| `UsageStatsManager.queryEvents` | **segunda permissão** no onboarding — atrito |
| Polling de `getWindows()` no serviço | acorda o processo periodicamente |
| Escutar `TYPE_WINDOWS_CHANGED` | precisa testar se dispara sem filtro |

A primeira parece a mais barata: o launcher gerar eventos já basta para saber que a
pessoa saiu. Custo a medir.

> ⚠️ Isso afeta **D3** (pausar ≠ zerar, janela de 5 min) e a **limitação #4** do
> PRODUTO.md. Os dois assumem que o app sabe quando a pessoa saiu. Hoje ele não sabe.

---

## R3 — Parar de deslizar · CONCLUÍDO (parcial)

**Pergunta:** dá pra separar "assistindo parado, celular na mão" de "largou na mesa"?

Executado: marcador → 43 s na mão → marcador → 43 s na mesa → marcador → HOME.
Os três marcadores saíram limpos, blocos de 43 s cada.

#### Relatório

**A pergunta central ficou sem resposta — por limitação do logger, não do device.**

```
17:51:27.386  D/SensorManager  registerListener :: LSM6DSOTR Accelerometer, 20000, 5000000
17:51:27.405  D/Mindless       accelerometer accuracy changed: 3     ← ÚNICA linha do sensor
17:53:09.658  D/SensorManager  unregisterListener
```

O serviço registra um `SensorEventListener` no acelerômetro mas **nunca loga leitura
nenhuma** — só a mudança de acurácia. Mão-vs-mesa exige alterar o logger para emitir
magnitude ou variância do vetor.

**Via eventos de acessibilidade, os dois blocos são indistinguíveis:**

| Bloco | Duração | Eventos | ev/s | Maior silêncio |
|---|---|---|---|---|
| A — celular **na mão** | 43 s | 42 | 0,98 | 12,9 s |
| B — celular **na mesa** | 43 s | 7 | 0,16 | 13,9 s |

Os 42 eventos do bloco A são quase todos o rabo do marcador (17:51:40). Descontando,
os dois blocos têm o mesmo comportamento: silêncio pontuado por uma rajada periódica.

**Achado A9 — existe um batimento de "vídeo tocando"**

Assinatura consistente, encontrada em R2 e R3:

```
cc=4  →  cc=1 (×4–5)  →  cc=5      todos em android.view.ViewGroup, dentro de ~100 ms
```

É o vídeo reiniciando o loop. Intervalos observados entre batimentos:

```
R2:  15,2s · 14,0s · 17,9s · 23,8s · 19,0s
R3:  16,4s · 28,6s · 14,0s · 31,7s
```

Isso **atenua** o problema do R2: assistir parado nem sempre é silêncio absoluto.
Mas o período do batimento = duração do vídeo, que varia — e no R2 o vídeo 4 passou
**16 s sem evento nenhum**.

```
maior silêncio observado DENTRO do app, com a pessoa assistindo:  17,8 s
```

Não testamos ocioso longo (2–3 min). Sem isso não dá pra escolher o timeout do
contador. Vira **R3-B**.

**Achado A10 — o logger atual desiste da sessão aos 45 s**

```
último gesto            17:52:24
unregisterListener      17:53:09   ← 45 s depois
saída real do app       17:53:16   ← ainda estava assistindo
```

O falso negativo previsto no R2, acontecendo na prática. É lógica do app antigo, não
limitação da plataforma — mas mostra o tamanho do erro que um timeout curto produz.

---

## R3-B — Ocioso longo · CONCLUÍDO — **teste decisivo da rodada**

**Pergunta:** qual o maior silêncio possível com a pessoa assistindo de verdade?

Executado: marcador → 3 min sem tocar, celular na mão → marcador → HOME.

#### Relatório

```
17:57:16   marcador (3 deslizes)
17:57:40   1 batimento cc=4            ← único evento em 3 minutos
18:00:19   marcador                    ← 158,7 s de silêncio absoluto
18:00:24   HOME
```

**158,7 segundos — 2 min 39 s — sem um único evento, com a pessoa assistindo.**

**Descartada a hipótese de o logger ter sido congelado:**

| Evidência | Durante os 158 s |
|---|---|
| Áudio (`AudioPathManager`) | ativo a cada ~3 s, ininterrupto |
| Decode de vídeo (`BufferPoolAccessor`, pid do Instagram) | 17:57:43 · 17:58:37 · 17:59:30 · 18:00:19 → **o vídeo deu loop 3×** |
| `Freecess result=2` | idêntico ao observado no R1, quando fluíam 23 ev/s |
| Serviço a11y | conectado, `Crashed services:{}` |
| Tela | acesa (timeout de 10 min) |
| Foco (`dumpsys window`) | `com.instagram.android` o tempo todo |

O vídeo tocou e reiniciou três vezes. O Instagram não emitiu **nenhum** evento.

**Config do serviço, extraída do `dumpsys accessibility`:**

```
eventTypes   = [TYPE_WINDOW_STATE_CHANGED, TYPE_WINDOW_CONTENT_CHANGED, TYPE_VIEW_SCROLLED]
capabilities = 0
retrieveInteractiveWindows = false
notificationTimeout = 16
```

### A conclusão

**Eventos de acessibilidade medem interação, não permanência.**

```
deslizando   →  23 ev/s   visível
assistindo   →   0 ev/s   INVISÍVEL   ← por 158 s ou mais
saiu do app  →   0 ev/s   INVISÍVEL
                  ▲
          mesmo sinal, indistinguível
```

O contador do **F2 — o coração do produto — não pode ser dirigido por eventos.**
Do jeito atual ele registraria "0 minutos" para quem passou 3 minutos hipnotizado
num vídeo, que é exatamente a pessoa que o app existe para avisar.

Não há timeout que resolva: 158 s é maior que qualquer janela de inatividade
plausível, e não é um teto — foi só o tempo que o teste durou.

### O que isso obriga

O tempo tem que vir de **"o app está em primeiro plano"**, não de eventos.
Os eventos continuam servindo — mas para outra coisa.

```
┌──────────────────────────────┬──────────────────────────────────┐
│ TEMPO (F2, o contador)       │ COMPORTAMENTO (F5, observações)  │
├──────────────────────────────┼──────────────────────────────────┤
│ foreground do app            │ eventos de acessibilidade        │
│ · UsageStatsManager, ou      │ · contagem de vídeos (A2)        │
│ · a11y sem filtro de package │ · hesitação (A3)                 │
│ · polling de getWindows()    │ · superfície Reels vs feed (A1)  │
└──────────────────────────────┴──────────────────────────────────┘
```

Isso é uma decisão de arquitetura, não um ajuste. Afeta **D3** (pausar ≠ zerar),
a **limitação #4** e o desenho inteiro do F2 no PRODUTO.md.

---

## R4 — Sair e voltar · CONCLUÍDO

**Pergunta:** recentes e alternar rápido diferem? A saída é detectável?

Executado: marcador → 30 s dentro → HOME 15 s → volta por **recentes** → 30 s dentro
→ HOME 30 s → volta por **alternar rápido** → 15 s dentro → HOME.
Vigia de foco (`dumpsys window`) rodando a cada 1 s como verdade independente.

#### Relatório

```
18:07:47.5  [LOG] type=32 ×2   InstagramMainActivity   ┐ entrada
18:07:47.5  [ADB] foco → com.instagram.android         ┘

18:08:31.4  [ADB] foco → launcher      ← SAÍDA 1 (17,2 s fora) · ZERO eventos
18:08:47.9  [LOG] type=32 ×2   InstagramMainActivity   ┐ volta por RECENTES
18:08:48.6  [ADB] foco → com.instagram.android         ┘

18:09:25.0  [ADB] foco → launcher      ← SAÍDA 2 (38,7 s fora) · ZERO eventos
18:10:02.6  [LOG] type=32 ×2   InstagramMainActivity   ┐ volta por ALTERNAR RÁPIDO
18:10:03.6  [ADB] foco → com.instagram.android         ┘

18:10:30.8  [ADB] foco → launcher      ← SAÍDA 3 · ZERO eventos
```

**A1 — Recentes e alternar rápido são indistinguíveis.**
Os dois produzem exatamente `type=32` ×2 em `com.instagram.mainactivity.InstagramMainActivity`.
O contador não precisa tratar caminhos de volta diferentes. Uma preocupação a menos.

**A2 — A entrada é um sinal bom, e rápido.**
O `type=32` chega **0,7–1,0 s antes** do polling de `dumpsys window` detectar a troca
de foco. Para entrada, o serviço é melhor que polling.

**A3 — A saída não existe no log. Confirmado três vezes.**
Nenhum evento em nenhuma das três saídas. Junto com P2 e A5, é definitivo.

### A demonstração final do problema do contador

Olhando **apenas** os intervalos entre deslizes — que é tudo que o serviço filtrado vê:

```
saiu 17 s do app         →  gap de  22,7 s
saiu 39 s do app         →  gap de  44,4 s
FICOU assistindo 158 s   →  gap de 158,7 s      (R3-B)
```

Qualquer limiar acerta ao contrário. Um timeout de 60 s classificaria:

- quem saiu por 39 s → "ainda assistindo" ❌
- quem estava vidrado por 2,5 min → "foi embora" ❌

Não é ajuste de parâmetro. É ausência de sinal.

> ⚠️ A janela de 5 min do D3 não foi testada — vira **R4-B** avulso, mas só faz
> sentido depois de resolver a detecção de foreground.

---

## R5 — Interagir sem deslizar · CONCLUÍDO

**Pergunta:** rolar comentários gera `type=4096` igual ao do vídeo?

Executado: marcador → comentários rolados ~20 s → marcador → like → perfil do autor
~20 s → marcador → compartilhar → HOME.

#### Relatório

**Não. As classes separam perfeitamente.**

```
DESLIZE DE VÍDEO                    ROLAR COMENTÁRIOS
androidx.viewpager.widget.ViewPager androidx.recyclerview.widget.RecyclerView
dy = 762 · 1318 · 1208 · 804        dy = 436 · 529 · 793 · -863 · -1140 · -1121
trio [médio, alto, baixo]           rajada longa, dy errático, muitos negativos
dx = 0                              dx = 0
```

**Regra de contagem de vídeo que sai daqui:**

```
vídeo contado  ⟺  class=ViewPager  E  dy≠0  E  dx=0
```

Aplicada ao R5: **9 deslizes** — exatamente os 3 marcadores de 3 cada. Nenhum falso
positivo entre os 38 eventos de RecyclerView. Uma contagem ingênua (todo `type=4096`
agrupado) teria inflado em ~4 vídeos só nos 20 s de comentários.

**Cuidado com a hesitação (A3):** comentários geram `dy` muito negativo
(−863, −1140, −1121). O filtro por `class=ViewPager` também é o que impede
"rolar comentário pra cima" de virar hesitação falsa.

**Cuidado com A1:** `RecyclerView` aparece tanto no **feed** quanto nos **comentários**.
Não serve sozinho como marcador de superfície — só o `ViewPager` identifica o Reels
positivamente.

**Sub-telas são todas detectáveis via `type=32`:**

| Momento | `type=32` com `class=` | Interpretação |
|---|---|---|
| Abriu comentários | `android.view.View` | folha por cima, não troca de activity |
| Abriu perfil do autor | `com.instagram.modal.ModalActivity` | activity modal |
| Voltou pro Reels | `com.instagram.mainactivity.InstagramMainActivity` | activity principal |
| Abriu compartilhar | `android.view.View` | folha por cima |

**Achado A12 — `dx` separa navegação lateral.**
Dentro do perfil apareceu `ViewPager dx=1075 dy=0` — swipe horizontal entre abas.
Sem o `dx=0` no filtro, isso viraria vídeo contado.

**Resposta à pergunta que o PRODUTO.md deixou aberta:** com comentários abertos o
app *sabe* que a pessoa saiu da superfície de vídeo (`type=32 android.view.View`) e
sabe quando voltou. Se isso deve pausar o contador é decisão de produto, não de
detecção — a informação existe.

---

## R9 — Notificação sobre tela cheia · CONCLUÍDO ✅

**Pergunta:** a notificação heads-up aparece por cima do Reels em tela cheia, ou o
aviso precisa virar cartão sobreposto (`SYSTEM_ALERT_WINDOW`)?

Era o único ⏳ do `PRODUTO.md` que bloqueava o desenho do onboarding.

#### Duas tentativas inválidas antes da boa

| Tentativa | Resultado | Por quê não valeu |
|---|---|---|
| `adb shell cmd notification post` ×6 | não apareceu | canal `shell_cmd` travado em `importance=3` (`mImportanceLockedDefaultApp=true`). DEFAULT nunca faz heads-up |
| E-mails do Gmail | não apareceu | canal "Mail" está em `imp=3`, com `userSet=true` — foi rebaixado pelo usuário. Nunca ia fazer heads-up |

Os dois eram falsos negativos por importância de canal, não por tela cheia.

#### Relatório — teste válido, lembrete do Calendário Samsung

```
19:01:44   ACTIVITY_RESUMED  com.instagram.android          ← Instagram em foreground
19:01:49   NOTIFICATION_INTERRUPTION  com.samsung.android.calendar
19:01:49   NOTIFICATION_SEEN          com.samsung.android.calendar
19:01:54   SecNotificationShadeWindowStateInteractor:
             headsUpNotificationShowing: true                ← HEADS-UP NA TELA
19:01:55   onHeadsUpPinnedModeChanged → false                ← dispensou em ~5 s

importance=4   channel=calendar_noti_ch_id_reminder   category=event
```

Confirmado pelo usuário e pelo log: **o cartão apareceu por cima do Reels em tela cheia.**

### Consequências

**D7 está de pé.** Notificação normal do sistema basta.

```
NÃO precisa de SYSTEM_ALERT_WINDOW
  → uma permissão a menos no onboarding
  → uma tela assustadora a menos
  → P1 intacto: desliza pra fora, some em ~5 s, não bloqueia
```

**A9b — bônus: o UsageStats registra as notificações do próprio app.**

```
type=NOTIFICATION_INTERRUPTION  package=<seu app>
type=NOTIFICATION_SEEN          package=<seu app>
```

Não precisa instrumentar nada para medir o H1. O ciclo completo sai da mesma API:

```
NOTIFICATION_INTERRUPTION   →  o aviso disparou, quando
NOTIFICATION_SEEN           →  chegou aos olhos
ACTIVITY_PAUSED em seguida  →  saiu, e quanto tempo depois
(nenhum PAUSED)             →  continuou
```

**Nota sobre o Reels:** o Instagram não usa modo imersivo pegajoso — é uma activity
normal com layout edge-to-edge. O TikTok também não. Nada nos dois apps bloqueia
heads-up. Confirmado indiretamente: o próprio TikTok declara 6 canais em
`importance=4` não travados (`Likes`, `Direct messages`, `Mentions`…).

---

## Achados consolidados

| # | Sinal | Confiável? | Sustenta / ameaça |
|---|---|---|---|
| A1 | `class=ViewPager` = Reels, `RecyclerView` = feed | ✅ sim | Sustenta D1 e a limitação #1 |
| A2 | Deslize = 3× `type=4096` em ~200 ms | ✅ sim | Sustenta a contagem de vídeos do F5 |
| A3 | `dy` negativo = hesitação | ✅ sim | Sustenta H7 |
| A4 | Assistir parado = silêncio (0–1,85 ev/s) | ✅ medido | **Ameaça D3 e o contador inteiro** |
| A5 | Sair do app = 0 eventos | ✅ medido | **Ameaça D3 e a limitação #4** |
| A6 | `type=2048` = 93% do volume, sem informação | ✅ medido | Otimização óbvia de bateria |
| A7 | Acelerômetro registrado mas **sem leitura logada** | ❌ inutilizável hoje | Exige mudar o logger antes de responder R3 |
| A8 | One UI congela o processo de fundo | ✅ medido | Risco novo, não estava no NEGOCIO.md |
| A9 | Batimento de loop: `cc=4 → cc=1… → cc=5` | ❌ **não confiável** | Sumiu por 158 s no R3-B mesmo com 3 loops de vídeo |
| A10 | Logger atual desiste da sessão aos 45 s de silêncio | ✅ medido | Mostra o custo de timeout curto |
| **A11** | **Assistir parado = 158,7 s sem nenhum evento** | ✅ **medido** | **Mata o contador dirigido por eventos. Ver R3-B.** |
| A12 | Comentários = `RecyclerView`; vídeo = `ViewPager`; `dx≠0` = nav lateral | ✅ medido | Sustenta a contagem do F5 sem inflar |
| A13 | Sub-telas emitem `type=32` com `class` distinta | ✅ medido | Responde o buraco do R5 no PRODUTO.md |

## Regra de contagem de vídeo (derivada, testada)

```
vídeo contado   ⟺   type=4096  E  class=androidx.viewpager.widget.ViewPager
                    E  dy≠0  E  dx=0
                    E  agrupado por gap > 0,5 s

hesitação       ⟺   mesmo filtro, com dy negativo dentro da rajada
```

Validada no R5: 9 deslizes reais → 9 contados, 0 falsos positivos entre 38 eventos
de RecyclerView.

**Validada contra gabarito humano no TikTok (T1):** 16 vídeos contados à mão → 16
gestos detectados, erro 0. Ver `TIKTOK.md`. No Instagram o gabarito nunca foi
coletado — o canal de instrução por áudio não funcionava.

## Perguntas que sobraram

1. **Como o app sabe que a pessoa está no app?** É a pergunta que ficou de pé.
   Sem ela não há F2. Opções em R2 e R3-B.
2. Tirar o filtro de package custa quanto em bateria?
3. O acelerômetro separa mão de mesa? → bloqueado por L1
4. Contagem de vídeos ainda sem gabarito humano independente.
5. R4 (recentes vs alternar rápido) — última pendência da rodada Instagram.

## Mudanças necessárias no logger

Descobertas pelos testes, não por leitura de código:

| # | Mudança | Por quê |
|---|---|---|
| L1 | Logar leitura do acelerômetro (magnitude/variância), não só acurácia | R3 é inrespondível sem isso |
| L2 | Remover ou ampliar o filtro de `packageNames` | Sem isso não há detecção de saída (A5) |
| L3 | Não desistir da sessão aos 45 s | Corta sessão de quem está assistindo (A10) |
| L4 | Considerar filtrar `type=2048 cc=1` na origem | 93% do volume, zero informação (A6) |
