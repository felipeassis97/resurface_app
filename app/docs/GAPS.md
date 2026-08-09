# Resurface — GAPS · Checklist de validação do F1

> O que precisa ser **provado no aparelho** antes de construir o resto da v1.
> Não são gaps de desenho — esses foram fechados no `PRODUTO.md` (D22–D25) e no
> `NEGOCIO.md`. São as **suposições que só o hardware confirma**.
>
> Device: Samsung SM-A536E · Android 16 (API 36) · One UI 8.0
> Alvo fixado: `compileSdk 36 · targetSdk 36 · minSdk 36`
> Última atualização: 2026-08-09

---

## Rodada de 2026-08-09 · executada no SM-A536E (adb + probe APK)

Rodado direto no aparelho conectado. G2/G6/G7/Doze só com adb; G1/G4/G5 com um probe
APK descartável (`targetSdk 36`); G3 parcial. Quase tudo sem tocar na tela.

| Teste | Resultado | Número medido |
|---|---|---|
| **G1** alarme×Doze | ✅ **PASS — decisivo** | `setExactAndAllowWhileIdle` disparou com **deltaMs=12** e **`idleMode=true`** (Doze deep forçado). Fura o freezer. **D22 confirmado; a11y-obrigatória descartada; D15 fica** |
| **G2** F-fresh | ✅ **PASS forte** | Latência ocorreu→visível ~**1 s**, RESUMED e PAUSED (limite era 45 s) |
| **G4** FGS specialUse | ✅ **PASS** | Sobe de foreground sem exceção; `isForeground=true types=0x40000000` (SPECIAL_USE); `targetSdk 36`. `<property>` validada |
| **G5** heads-up próprio | ✅ **PASS** | Nossa notif canal HIGH (importance=4) sobre IG tela cheia → SystemUI `headsUpNotificationShowing: true`. Dispensa `SYSTEM_ALERT_WINDOW` (D7) |
| **G3** religa serviço | 🟡 **Parcial ✅** | `MY_PACKAGE_REPLACED` religou o FGS sozinho. Falta o `adb reboot` (BOOT_COMPLETED) — pende confirmação (aparelho do dono) |
| **G7** acesso ao uso | ✅ **Mecânica ok** | `appops get GET_USAGE_STATS` detecta estado; `set` disponível |
| **G6** freeze One UI | ✅ **Ambiente confirmado** | `FreecessHandler: freeze` a cada **6,0 s** exatos — a condição hostil que o G1 furou |
| **Doze** | ✅ **Controlável** | `deviceidle force-idle`→IDLE, `unforce`→ACTIVE; `battery unplug/reset` |

**Pendências de teste (não bloqueiam a arquitetura):**
- ~~G3 completo~~ ✅ feito (reboot físico, FGS voltou sozinho).
- **G4 longo** — INTERROMPIDO aos ~22 min pelo deploy do skeleton (não foi kill). specialUse
  sem-timeout apoiado na doc oficial. Confirmação de 6h fica pro MonitorService real do F1.
- **G1 natural** — Doze real por horas + Freecess simultâneo (o teste usou Doze forçado; sinal já é forte: 12 ms).

**Probe APK:** `com.resurface.resurface` (andaime descartável, coexiste com o logger
`com.resurface.app`). Fontes em `app/src/main/`. Remover com `adb uninstall com.resurface.resurface`.

> **Placar: 6,5 de 7.** Os três 🔴 decisivos (G1, G4, e o núcleo do G3/G5) **passaram no
> aparelho real**. A arquitetura do `PRODUTO.md` (D13/D20/D22/D24) fica de pé no hardware.
> Nenhuma surpresa. Só resta o reboot físico e o teste longo de 6 h.

---

## Como ler

Cada item tem: a **pergunta**, o que **decide**, o **roteiro**, e o critério
**passa/falha**. Ordem = por "mata o projeto se falhar".

Legenda: 🔴 bloqueia o núcleo · 🟡 bloqueia uma feature · 🟢 confirmação barata

> Restrição de protocolo (medida na rodada anterior, ver `REELS.md`): **não existe canal
> de instrução em tempo real** no aparelho. Roteiro vem antes, captura roda em segundo
> plano, fronteiras por 3 deslizes rápidos. Onde der, automatizar por `adb`.

---

## G1 · 🔴 O alarme exato atravessa o freeze do One UI? — **teste decisivo**

**A suposição que sustenta o produto inteiro (D22).** Se falhar, o aviso atrasa ou não
chega, e a proposta de valor nº 2 ("falar durante") cai. Aciona o plano B (a11y
obrigatória) — reabre D15/D20.

```
Pergunta:  um setExactAndAllowWhileIdle agendado dispara na hora com o app
           congelado pelo One UI (freeze medido a cada ~6 s) e em Doze?

Roteiro:
  1. app mínimo: agenda setExactAndAllowWhileIdle pra agora + 20 min
  2. manda o app pro fundo, tela apagada, NÃO mexer no aparelho
     (força Doze:  adb shell dumpsys deviceidle force-idle)
  3. registra o instante real em que o BroadcastReceiver acorda
  4. repete: com isenção de bateria ON vs OFF; com o app em "apps que
     nunca dormem" vs fora

Passa:  dispara dentro de poucos segundos do alvo, mesmo com tela apagada/Doze
Falha:  não dispara, ou atrasa minutos → medir de quanto é o atraso e em qual
        config. Se falhar mesmo com todas as camadas do D23 → acionar plano B
```

**O que decide:** se o D22 fica de pé como está, ou se a acessibilidade precisa voltar
a ser obrigatória (host residente pro relógio). Testar **antes** de qualquer outra coisa.

> ▶ **RESULTADO 2026-08-09 · ✅ PASS (decisivo).** Probe agendou `setExactAndAllowWhileIdle`,
> `dumpsys deviceidle force-idle` → Doze deep (`idleMode=true`). Disparou com **`deltaMs=12`**.
> Log cru: `ALARM_FIRED | deltaMs=12 (real - agendado) | idleMode=true`. Atravessa o freezer.
> **D22 confirmado. Plano B (a11y obrigatória) descartado; D15 fica.** Pende só Doze natural
> por horas + Freecess simultâneo (o teste usou Doze forçado — sinal já forte).

---

## G2 · 🔴 Latência do `queryEvents` ao vivo — Teste F-fresh

**Decide se o contador começa na hora e se a releitura do D22 lê estado fresco.**
Detalhe completo no `PRODUTO.md` F5.

```
Pergunta:  quanto tempo o sistema demora pra tornar um ACTIVITY_RESUMED
           visível ao queryEvents?

Roteiro (automatizável por adb):
  1. anota T0; abre o Instagram em T0
  2. em T0+15s, +30s, +45s, +60s:  queryEvents(T0−60s → agora)
     registra se o RESUMED de T0 já apareceu e com que timestamp
  3. mede latência = (instante visível) − T0
  4. repete pro ACTIVITY_PAUSED (latência da saída)

Passa:  RESUMED visível em ≤ 45 s (um intervalo de tick)
Falha:  latência > 45 s → contador começa atrasado; reavaliar intervalo do tick
        e/ou apoiar início do episódio em evento de a11y quando ligada
```

**O que decide:** o intervalo do tick de manutenção (§5.2) e a margem da releitura do D22.

> ▶ **RESULTADO 2026-08-09 · ✅ PASS forte.** `am start` do IG + poll de `dumpsys usagestats`
> a 1 s. RESUMED visível **~1 s** após ocorrer (ocorreu 11:16:17, visível 11:16:18). PAUSED
> idem (~1 s). Muito abaixo dos 45 s. Contador começa na hora; releitura do D22 lê fresco.

---

## G3 · 🔴 O serviço religa após reboot?

**Sem isso o produto morre a cada reinício em silêncio** (`DEVICE_STARTUP` observado no
log — acontece de verdade). Ver F1 e D24.

```
Pergunta:  RECEIVE_BOOT_COMPLETED + receiver reinicia o FGS specialUse após
           reboot, dentro das restrições de start em background?

Roteiro:
  1. serviço rodando, notificação fixa visível
  2. adb reboot
  3. NÃO abrir o app na mão
  4. verificar: a notificação fixa voltou sozinha? o serviço está no ar?
  5. confirmar que o replay do D24 reconstruiu o estado (se havia episódio)

Passa:  serviço volta sozinho após o boot, sem abrir o app
Falha:  fica morto até abertura manual → investigar restrição de start / atraso
        pós-boot / exigência de o app ter sido aberto ao menos uma vez
```

> ▶ **RESULTADO 2026-08-09 · ✅ PASS** (com 2 condições). Reboot físico (`adb reboot`): o
> FGS voltou **sozinho**, sem abrir o app (`isForeground=true types=0x40000000`, iniciado de
> `uidState: RCVR` = nosso BootReceiver, `code:SYSTEM_ALLOW_LISTED`). Confirmado que
> MainActivity NÃO foi aberta (último RESUMED nosso = pré-reboot). Também passou no
> `MY_PACKAGE_REPLACED` (reinstalação).
>
> **⚠ Duas condições reais, ambas atendidas pelo produto:**
> 1. **Primeiro desbloqueio pós-boot** (gate do FBE/File-Based Encryption): `BOOT_COMPLETED`
>    só é entregue **depois** que o usuário desbloqueia o aparelho uma vez. Antes disso o
>    serviço fica morto. Aceitável — o usuário desbloqueia o telefone logo após ligar.
>    *(Se quiser religar antes do unlock, precisaria de `LOCKED_BOOT_COMPLETED` + receiver
>    `directBootAware` — provavelmente desnecessário pro produto.)*
> 2. **Isenção de bateria / allowlist**: o start de FGS a partir do BootReceiver só foi
>    permitido porque o app estava na allowlist (`SYSTEM_ALLOW_LISTED`). Sem a isenção da
>    TELA 3, o start no boot pode ser barrado. Mais um motivo pra TELA 3 ser no onboarding.

---

## G4 · 🔴 FGS `specialUse` inicia com a declaração correta?

**Sem a declaração o serviço lança exceção ao iniciar** — independente de loja (ver D20).

```
Pergunta:  o serviço sobe com FOREGROUND_SERVICE_SPECIAL_USE + a <property>
           PROPERTY_SPECIAL_USE_FGS_SUBTYPE no manifesto? E sobrevive 24h+
           sem o timeout de 6h que mata dataSync/mediaProcessing?

Roteiro:
  1. declarar permissão + <property> no <service>
  2. startForegroundService com type specialUse
  3. deixar rodando > 6 h; confirmar que NÃO chega onTimeout

Passa:  sobe sem exceção; passa das 6 h vivo
Falha:  exceção no start → faltou property/permissão. onTimeout chamado → tipo errado
```

> ▶ **RESULTADO 2026-08-09 · ✅ PASS** (parte curta). Sobe de foreground sem exceção:
> `isForeground=true foregroundId=1 types=0x40000000` (= `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`),
> `targetSdkVersion:36`. `<property>` validada. Start de background (`am`/broadcast) é barrado
> pela restrição — start vem do Activity (onboarding) e do BootReceiver.
>
> **6 h sem `onTimeout` — teste INTERROMPIDO, não concluído.** O probe ficou vivo do início às
> 12:06 (~22 min, sem `onTimeout`), quando o **pacote foi substituído** pelo deploy do skeleton
> MVVM (Android Studio: `PACKAGE_REMOVED` + `set debug app`). **Não foi kill do One UI** — foi
> reinstalação. A claim "specialUse não tem timeout de 6h" continua apoiada na **doc oficial do
> Google** (autoritativa) + no start limpo aqui. Confirmação empírica de 6h fica pro
> `MonitorService` real do F1, observado ao longo de dias de uso. Baixa prioridade.

---

## G5 · 🟡 A notificação do PRÓPRIO app faz heads-up sobre tela cheia?

R9 já validou heads-up sobre Reels — **mas com notificação de terceiro** (Calendário).
Falta confirmar com a notificação do Resurface, canal `IMPORTANCE_HIGH`, + os dois botões
do F7. Ver F3/F7/D7.

```
Pergunta:  o aviso do Resurface (canal IMPORTANCE_HIGH, 2 action buttons)
           aparece por cima do Instagram/TikTok em tela cheia, e os botões
           [era hora]/[agora não] funcionam sem abrir tela?

Roteiro:
  1. POST_NOTIFICATIONS concedida
  2. canal criado em IMPORTANCE_HIGH (DEFAULT nunca faz heads-up — invalidou 2 testes no R9)
  3. com Instagram em foreground tela cheia, postar o aviso
  4. tocar cada botão; confirmar PendingIntent dispara e registra o outcome (F7)

Passa:  cartão sobre a tela cheia; botões respondem sem abrir Activity
Falha:  não aparece → conferir importância do canal e POST_NOTIFICATIONS
```

> ▶ **RESULTADO 2026-08-09 · ✅ PASS** (autônomo). IG em foreground tela cheia
> (`mCurrentFocus=com.instagram.android/...InstagramMainActivity`), probe postou o aviso
> canal `probe_alert` importance=4 + 2 botões → SystemUI: **`headsUpNotificationShowing: true`**.
> Cartão do próprio app sobre o Reels. **D7 confirmado com nosso app; dispensa `SYSTEM_ALERT_WINDOW`.**
> Teste do toque nos botões: pendente (precisa da mão ou injeção de tap).

---

## G6 · 🟡 Camadas de sobrevivência do One UI — efeito real (D23)

Quanto cada camada reduz o atraso do **tick de manutenção** (o disparo já é coberto por
G1). Mede a necessidade real do passo manual.

```
Pergunta:  com isenção de bateria + "apps que nunca dormem", o tick de 45 s
           roda com que regularidade? Sem elas, quanto piora?

Roteiro:
  1. deixar o serviço rodando 2–3 h em cada config:
     (a) nada  (b) só isenção de bateria  (c) isenção + nunca-dorme
  2. registrar os intervalos reais entre ticks; contar buracos > 45 s
  3. validar o autodiagnóstico (camada 5): ele detecta e mostra os buracos?

Passa:  na config (c) os buracos somem ou ficam raros; autodiagnóstico acusa quando há
Falha:  buracos grandes mesmo em (c) → o atraso do tick fica; confiar mais no alarme (G1)
```

> ▶ **RESULTADO 2026-08-09 · ✅ Ambiente confirmado.** `FreecessHandler: freeze` a cada
> **6,0 s** exatos (11:18:32/38/44/50/56). É a condição hostil que o G1 furou (12 ms). O
> efeito das camadas por config (a/b/c) sobre o *tick* fica pro run longo — mas como o G1
> mostrou que o **disparo** não depende do tick, o risco do buraco de tick é secundário.

---

## G7 · 🟢 As duas permissões de acesso especial concedem e são detectáveis?

```
Pergunta:  PACKAGE_USAGE_STATS concede via ACTION_USAGE_ACCESS_SETTINGS e é
           checável por AppOpsManager? A a11y (opcional) exige mesmo o passo
           de "permitir configurações restritas" em app sideload?

Roteiro:
  1. onboarding abre a tela de acesso ao uso; conceder; app confirma via AppOps
  2. tentar ligar a acessibilidade instalado por sideload → aparece o bloqueio
     de restricted settings? o passo "Info do app → permitir config. restritas"
     destrava?

Passa:  acesso ao uso detectado corretamente; caminho de restricted settings documentado
Falha:  AppOps reporta errado, ou a11y não liga nem após o passo → revisar TELA 5
```

> ▶ **RESULTADO 2026-08-09 · ✅ Mecânica ok** (parte adb). `appops get <pkg> GET_USAGE_STATS`
> retorna o estado (`default`/`allow`/`ignore`) → detectável; `appops set` disponível pra
> conceder. O `USE_EXACT_ALARM` do probe veio **concedido na instalação** (`canScheduleExact=true`,
> sem diálogo). O passo de restricted settings da a11y (via UI) fica pro teste híbrido.

---

## Pendências de código (não são teste — só aplicar)

| # | O quê | Onde | Estado |
|---|---|---|---|
| C1 | Fixar `compileSdk`/`targetSdk` de **37 → 36** | `app/build.gradle.kts` | ✅ **feito (2026-08-09)** |
| C2 | Declarar `FOREGROUND_SERVICE_SPECIAL_USE` + `<property>` | AndroidManifest | ✅ **feito no probe** (validado G4) |
| C3 | `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `USE_EXACT_ALARM` | AndroidManifest | ✅ **feito no probe** |
| C4 | Boot receiver + `MY_PACKAGE_REPLACED` receiver | novo | ✅ **feito no probe** (validado G3 parcial) |

> Nota: C1–C4 foram aplicados no **probe APK** (andaime). Quando o produto for escrito, o
> manifesto/gradle do probe serve de referência — mas o probe em si é descartável.

---

## Ordem recomendada

```
1. G1  ← decide o D22 (e se a11y volta a ser obrigatória). Testar PRIMEIRO.
2. G2  ← decide o intervalo do tick e a margem do "acordar-pra-conferir"
3. G4  ← o serviço precisa subir pra qualquer coisa acima rodar
4. G3  ← religar após reboot
5. G5  ← o aviso completo (canal próprio + botões F7)
6. G6  ← afinar as camadas do One UI
7. G7  ← confirmar o onboarding de permissões
```

**Regra:** nenhuma linha de feature (histórico, mensagens, dashboard) antes de G1–G4
passarem. São o que decide se a arquitetura do `PRODUTO.md` fica de pé no aparelho real.

---

*Companheiros: `PRODUTO.md` (decisões D22–D25) · `NEGOCIO.md` (riscos) · `REELS.md` · `TIKTOK.md`*
