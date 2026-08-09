# TikTok — Rodada de Testes de Detecção

> Objetivo: descobrir **que sinais o TikTok emite durante uso real** e se a regra de
> detecção validada no Reels transfere.
>
> Device: Samsung SM-A536E · Android 16 (SDK 36) · One UI 8.0
> Package alvo: `com.zhiliaoapp.musically` (confirmado)
> Logger: `com.resurface.app` / `ResurfaceAccessibilityService`, tag `Mindless`
> Status: 🔬 smoke test e ocioso concluídos · T1, T2, T5 pendentes
> Última atualização: 2026-08-08

> **Protocolo, formato de log e vocabulário:** ver `REELS.md`. São os mesmos.
> Resumo: não existe canal de instrução em tempo real; o roteiro vem no chat antes,
> a captura roda em segundo plano, as fronteiras são marcadas por **3 deslizes rápidos**.

---

## Conclusão parcial — a regra do Reels transfere

|  | Instagram Reels | TikTok For You |
|---|---|---|
| Classe do deslize | `androidx.viewpager.widget.ViewPager` | `androidx.viewpager.widget.ViewPager` |
| Assinatura | 3 eventos, `[médio, alto, baixo]` | 3–4 eventos, mesmo formato |
| `dy` típico | 320–1600 | 629–1358 |
| Silêncio assistindo parado | **158,7 s** | **153,8 s** |
| Saída do app | 0 eventos | 0 eventos |

```
vídeo contado  ⟺  type=4096 · class=ViewPager · dy≠0 · dx=0 · gap >0,5 s
```

**Serve nos dois apps sem alteração.** Isso barateia o **D2** (contador único
atravessando apps) — era um risco em aberto e virou custo baixo.

E o problema do contador de tempo **não é do Instagram, é da plataforma**: os dois
apps ficam mudos enquanto a pessoa assiste. Ver a conclusão de `REELS.md`.

---

## T-smoke — O logger enxerga o TikTok? · CONCLUÍDO

Executado pelo agente, sem intervenção: abrir por adb → 10 s ocioso → 4 swipes
injetados (`input swipe 540 1600 540 400 200`).

#### Relatório

**Sim.** 101 eventos, 100 com `app=com.zhiliaoapp.musically`.

```
por tipo:   2048 = 83    4096 = 14    32 = 3
classes:    ViewPager 40 · SeekBar 28 · TabHost 19 · LinearLayout 7
            com.ss.android.ugc.aweme.main.MainActivity 3 · FrameLayout 3
```

4 swipes injetados → 4 clusters de `type=4096`, todos `ViewPager`:

```
808  1282   37
629  1358  140
632   675  813    7
646   720  758    3
```

**Activity principal:** `com.ss.android.ugc.aweme.main.MainActivity`
**Splash na abertura:** `com.ss.android.ugc.aweme.splash.SplashActivity`

**Classes novas em relação ao Instagram:**
- `android.widget.TabHost` — a barra de abas de baixo. Candidato a discriminador
  For You vs Seguindo vs Perfil (a testar no T1).
- `android.widget.SeekBar` — a barra de progresso do vídeo, 28 eventos `type=2048 cc=64`
  a cada ~130 ms. Parecia um batimento de playback. **Não é** — ver T-idle.

---

## T-idle — O TikTok emite enquanto a pessoa assiste? · CONCLUÍDO

Executado pelo agente: abrir → 3 swipes injetados → **150 s sem tocar** → 1 swipe.

#### Relatório

**Não emite nada.**

```
swipes                ev=50   4096=10  2048=40   maxGap=  9,8 s
OCIOSO 150s           ev=0    4096=0   2048=0    maxGap=  0,0 s
(evento seguinte)                                 maxGap=153,8 s
```

Zero `SeekBar` durante o ocioso. A barra de progresso só emite **durante e logo após
a interação** — não é sinal de playback. A hipótese de batimento morreu.

`153,8 s` de silêncio absoluto, contra `158,7 s` no Reels (R3-B). Os dois apps se
comportam igual.

> Ressalva: os deslizes deste teste foram **injetados por adb**, não com o dedo.
> Isso não afeta a conclusão — a pergunta é sobre o que acontece *depois* de parar,
> e nada é injetado nesse período.

---

## T1 — Superfície e cadência humana · PARCIAL

**Pergunta:** `TabHost` separa For You de Seguindo/Perfil? E qual o volume de eventos
com dedo humano (o smoke test usou swipe sintético)?

#### Relatório

**A parte do For You funcionou.**

```
bloco de scroll   18:20:38 → 18:21:22   (44 s)
16 gestos · cadência 2,8 s/vídeo · 1 hesitação (6%)
```

**TT7 — O TikTok é bem mais econômico que o Instagram:**

| | eventos/s | eventos por gesto |
|---|---|---|
| Instagram Reels (R1 rápido) | 23,0 | ~41 |
| TikTok For You (T1) | 6,2 | ~17 |

Fator ~2,4× por gesto. O custo do serviço em bateria **não é o mesmo nos dois apps** —
o Instagram é o caro.

**TT8 — Hesitação existe no TikTok, mesma assinatura:**

```
320  -293  -27      ← dy negativo dentro da rajada, class=ViewPager
```

1 em 16 gestos (6%), contra 8% no Instagram. A regra do A3 transfere.

**TT9 — `TabHost cc=7` não é batimento ocioso.**
Emite a cada ~3 s, mas sempre colado a um deslize. Durante o T-idle: zero.

**A parte das superfícies falhou — conta não logada.**

```
18:21:59.9  type=32  X.0jBL
18:22:00.4  type=32  com.ss.android.ugc.aweme.account.login.auth.I18nSignUpActivity
18:22:03.0  type=32  com.ss.android.ugc.aweme.account.login.authorize.AuthorizeActivity
```

Tocar em **Seguindo** e em **Perfil** levou à tela de cadastro. As duas exigem conta.
`TabHost` como discriminador de superfície continua **não testado**.

> ⚠️ **Consequência para a rodada inteira:** o TikTok está sendo usado deslogado. O
> For You deslogado pode ter comportamento diferente do logado (sem histórico, mais
> conteúdo genérico, possivelmente vídeos mais curtos). E o T5 (comentários) pode
> bater na mesma parede.

**TT10 — GABARITO HUMANO: 16 vídeos contados, 16 gestos detectados.** ✅

```
regra aplicada:  type=4096 · class=ViewPager · dy≠0 · dx=0 · gap >0,5 s
detectado:       16 gestos
contado à mão:   16 vídeos
erro:            0
```

É a única validação independente da rodada inteira — no Instagram o gabarito nunca
foi coletado porque o canal de áudio não funcionava. A regra de contagem do **F5**
sai de "consistente com o padrão" para **medida contra verdade humana**.

---

## T2 — Loop de vídeo · PENDENTE

**Pergunta:** o TikTok repete o mesmo vídeo em loop. O reinício gera evento que possa
ser confundido com troca de vídeo e inflar a contagem?

O T-idle sugere que **não** (zero eventos em 150 s, com o vídeo certamente reiniciando
várias vezes). Este teste confirma com dedo humano e vídeos curtos.

**Roteiro**

```
1.  Abra o TikTok, confirme aba PARA VOCÊ
2.  ►► 3 DESLIZES RÁPIDOS              ← marcador
3.  Escolha um vídeo CURTO (menos de 15 s) e deixe dar loop
    ~40 s sem tocar. CONTE quantas vezes reiniciou
4.  ►► 3 DESLIZES RÁPIDOS              ← marcador
5.  HOME
```

#### Relatório
_(a preencher)_

---

## T5 — Comentários vs vídeo · PENDENTE

**Pergunta:** no Reels, comentários = `RecyclerView` e vídeo = `ViewPager`, separação
limpa. Vale no TikTok?

**Roteiro**

```
1.  Abra o TikTok, confirme aba PARA VOCÊ
2.  ►► 3 DESLIZES RÁPIDOS              ← marcador
3.  Abra os COMENTÁRIOS, ROLE por ~20 s, feche
4.  ►► 3 DESLIZES RÁPIDOS              ← marcador
5.  Abra o PERFIL DO AUTOR, ~15 s, volte
6.  ►► 3 DESLIZES RÁPIDOS              ← marcador
7.  HOME
```

#### Relatório
_(a preencher)_

---

## Testes que NÃO precisam ser feitos

Respondidos pela rodada do Instagram + T-idle. Rodar de novo é gastar tempo.

| Teste original | Por quê não |
|---|---|
| T3 (mão vs mesa) | Bloqueado por L1 — o logger não emite leitura do acelerômetro |
| T3-B (ocioso longo) | **Feito** como T-idle: 153,8 s de silêncio |
| T4 (sair e voltar) | Saída não gera evento em nenhum app; recentes e alternar rápido são idênticos (R4) |

---

## Achados consolidados

| # | Sinal | Confiável? | Sustenta / ameaça |
|---|---|---|---|
| TT1 | Logger enxerga `com.zhiliaoapp.musically` | ✅ medido | Rodada é possível |
| TT2 | Deslize = `ViewPager`, mesmo trio do Reels | ✅ medido | **Barateia o D2** |
| TT3 | Ocioso = 153,8 s sem evento | ✅ medido | Confirma A11 fora do Instagram |
| TT4 | `SeekBar` não é batimento de playback | ✅ medido | Mata a última esperança de heartbeat |
| TT5 | `TabHost` presente na barra de abas | 🟡 a validar | Pode separar For You de Seguindo (T1) |
| TT6 | Activity = `com.ss.android.ugc.aweme.main.MainActivity` | ✅ medido | Marcador de entrada, como no Reels |

## Perguntas que sobraram

1. `TabHost` separa For You de Seguindo? → T1
2. Cadência e volume de eventos com dedo humano (o medido foi sintético) → T1
3. Loop de vídeo curto infla contagem? → T2
4. Comentários usam `RecyclerView` como no Reels? → T5
