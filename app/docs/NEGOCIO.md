# Resurface — Documento de Negócio

> Escrito em 2026-08-08. Sem detalhe técnico.
> Companheiro do `PRODUTO.md`, que trata do "o quê". Este trata do "por quê" e "pra quem".
>
> **Revisão de 2026-08-08 (noite):** a rodada de testes de detecção foi executada.
> Ela confirmou a proposta de valor nº 1 com dado real, mudou o quadro de riscos e
> destravou o mecanismo de rotulagem que faltava. Evidência em `REELS.md` e `TIKTOK.md`.

---

## 0. Que projeto é este? — ✅ FECHADO em 2026-08-08

Era a pergunta mais importante do documento, e estava aberta. **Agora não está.**

| Caminho | O que significa | Sucesso é | |
|---|---|---|---|
| **A. Ferramenta pessoal** | Você constrói pra você. Talvez uns amigos. | Você continuar usando depois de 2 meses. | ✅ **ESCOLHIDO** |
| B. Pesquisa acadêmica | Estudo, participantes, publicação. | Um resultado publicável. | descartado |
| C. Produto de mercado | Loja, usuários desconhecidos, talvez receita. | Retenção e crescimento. | descartado |

### 🔒 Decisão: caminho A — ferramenta pessoal

O app é construído para o próprio autor usar. Não vai para a Play Store, não tem
entregável acadêmico, não tem prazo externo, e ninguém além do autor lê os dados.

**As quatro perguntas que discriminam, respondidas:**

```
quem vai usar?              só você
qual o entregável?          o app funcionando
existe prazo externo?       não
alguém mais lê os dados?    não
```

**O que isso trava:**

| | Consequência |
|---|---|
| **H1 continua fatal** | Se saber o tempo não muda nada, o app é um cronômetro bonito. É o risco que pode matar o projeto — ver §5 e §7 |
| **F7 é essencial** | Sem os botões, o H1 fica sem resposta. Não é feature opcional |
| **S1 é a métrica de topo** | "Continuar instalado e ativo na semana 8". Sobrevivência antes de eficácia |
| **N=1 basta** | Você é o universo do estudo. Nenhuma afirmação precisa generalizar |
| **Sem ética / consentimento** | Não há terceiros envolvidos |
| **Escopo = o mínimo que testa o H1** | Nada entra por ser interessante de construir |

**Por que não B:** exigiria consentimento formal, participantes recrutados, comitê de
ética e desenho de grupo de controle. Além disso, o caminho B **foi tentado antes e
travou** — por falta de um mecanismo de rotulagem que hoje existe no desenho (F7),
mas que resolve só a metade técnica do problema.

**Por que não C:** o app não vai para a loja. Distribuição é instalação direta, num
aparelho só. Ver a seção 0 do `PRODUTO.md`.

> ✅ **Mudou: o mecanismo de rotulagem agora existe no desenho.**
> O **F7** do `PRODUTO.md` põe dois botões no próprio aviso (`[era hora]` / `[agora não]`).
> É rotulagem no momento, com um toque, sem formulário. E o lado objetivo vem de graça:
> o sistema registra `NOTIFICATION_SEEN` e o que a pessoa fez depois.
>
> Isso **não** transforma o projeto em pesquisa — falta comitê de ética, participantes
> e grupo de controle desenhado. Mas remove o obstáculo técnico que travou o B.
> Se o objetivo virar B mais à frente, o caminho fica mais curto do que era.

> ⚠️ **Se o objetivo real for B, isto muda agora, não depois.** Dados coletados sem
> rótulo não viram dados de pesquisa retroativamente — mas com o F7 desde a v1, eles
> nascem rotulados.

---

## 1. O problema

Vídeo curto — Reels, TikTok — é desenhado pra apagar a noção de tempo. Não é um
defeito do usuário; é o produto funcionando como projetado. Rolagem infinita,
autoplay, sem fim natural, sem marco de tempo em lugar nenhum.

O resultado é uma experiência específica e muito comum:

> "Eu ia dar uma olhada rápida. Já se passaram 40 minutos e eu não percebi."

O incômodo não é o tempo em si. É a **surpresa** — a distância entre o que a pessoa
achava que estava fazendo e o que de fato aconteceu. Ninguém escolheu 40 minutos.
Eles simplesmente ocorreram.

**A hipótese central:** o problema não é falta de força de vontade. É **falta de
informação no momento certo**.

> ✅ **"O produto funcionando como projetado" deixou de ser premissa.**
> O sistema entrega ao Resurface as notificações de terceiros, incluindo as do próprio
> Instagram (`ig_heads_up_reminders_with_vibrations`) e do TikTok
> (`recommend_video_push_associated_4`) — ambas observadas no aparelho de referência.
>
> Correlacionando com o início dos episódios, o app pode dizer quantos deles começaram
> logo depois de um toque no ombro do próprio app. Isso vira o **D17** do `PRODUTO.md`,
> custa zero e transforma esta afirmação de retórica em número.

---

## 2. Quem

**Usuário primário (v1): você.**

Alguém que:
- usa Reels ou TikTok todo dia
- se incomoda com a perda de noção de tempo, não com o uso em si
- já tentou os limites nativos do celular e os ignorou ou desligou
- rejeita apps que bloqueiam, culpam ou infantilizam
- se importa com privacidade a ponto de recusar um app que mande dados pra um servidor

**Não é pra:** quem quer parar de usar redes sociais (isso é bloqueio, outro
produto), quem não se incomoda (não há problema a resolver), quem quer relatórios
corporativos de produtividade.

> ⚠️ **Tensão descoberta na rodada de testes: o usuário primário não usa TikTok.**
> Não há conta no aparelho — tocar em "Seguindo" ou no perfil leva à tela de cadastro.
>
> No caminho A, o usuário **é** o usuário. Isso significa que o D2 (contador único
> atravessando apps), que este documento defende como diferencial nº 1, é justamente
> o comportamento que o uso real do dono do projeto **não exercita**.
>
> Manter o TikTok custa quase nada em código — é um nome numa lista de dois, e a
> regra de detecção é idêntica nos dois apps (medido). Custa não ter evidência própria.
> É uma aposta barata sobre o caminho C, mas é uma aposta. Ver Q7 do `PRODUTO.md`.

---

## 3. Por que o que existe não resolve

| Solução | O que faz | Por que não resolve |
|---|---|---|
| **Bem-Estar Digital / Tempo de Uso** | Limite diário por app | **Conta por app.** 25 min de TikTok + 25 de Instagram = dois "quase limites", nenhum aviso. E o aviso vem no fim do dia, longe do momento. |
| **Bloqueadores** (Opal, Freedom) | Impedem abrir | Tira a decisão da pessoa. Gera driblagem e desinstalação. Viola autonomia. |
| **Apps de fricção** (one sec) | Atrasam a abertura | Agem na *entrada*. O problema acontece 20 minutos *depois* de entrar. |
| **Apps de foco** (Forest) | Gamificam concentração | Exigem que a pessoa decida antes. O uso não intencional, por definição, não é decidido antes. |
| **Não fazer nada** | — | Funciona pra muita gente. É o concorrente real. |

**O buraco em comum:** todos agem **antes** (bloquear, atrasar) ou **depois**
(relatório do dia). Nenhum age **durante** — no minuto 22, enquanto a pessoa ainda
está lá e ainda pode decidir.

> ✅ **O buraco do Bem-Estar Digital foi medido, não suposto.**
> Em 2026-08-08, aplicando a regra do Resurface sobre o uso real do aparelho:
>
> ```
> episódio 17:45:58 → 18:22:16    18 min 43 s
>          Instagram até 18:00 · TikTok até 18:22 · fundidos num episódio só
> ```
>
> O Bem-Estar Digital teria mostrado *"Instagram 39 min · TikTok 8 min"* e nenhum
> alerta. Faltou **1 min 17 s** para o aviso de 20 minutos do Resurface disparar.

---

## 4. A proposta de valor

> **Um relógio para uma experiência que foi projetada para não ter nenhum.**

Três coisas que, juntas, ninguém mais faz:

**1. Conta a atividade, não o aplicativo.** ✅ *validado — ver §3*
Pular do TikTok pro Instagram não zera nada. É meia hora de vídeo curto, e o app diz
isso. As ferramentas nativas são cegas justamente pra esse comportamento.

**2. Fala no momento em que ainda dá pra decidir.** ✅ *validado — ver §7, risco de overlay*
Não no fim do dia. No minuto 22, com a pessoa ainda na tela. E a notificação **aparece
por cima do vídeo em tela cheia** — medido, não suposto.

**3. Recua quando é ignorado.**
Cada aviso ignorado dobra o silêncio seguinte. Um app que insiste é desinstalado
na terceira semana. Um app que recua sobrevive.

E dois sustentadores:

**4. Fala do seu jeito.** O aviso é escrito no seu tom, com referência ao que você gosta.

**5. Nada sai do aparelho.** Sem conta, sem servidor, sem nuvem.

---

## 5. Hipóteses e como validar

| # | Hipótese | Teste | Estado |
|---|---|---|---|
| H1 | Saber a duração no momento muda a decisão | Registrar, por 4 semanas, o que aconteceu depois de cada aviso | ❓ não testada — **mas o instrumento agora existe (F7)** |
| H2 | Contar entre apps pega o que o nativo não pega | Contar quantos episódios cruzam TikTok↔Instagram | 🟢 **1 episódio já observado** (18:43, §3). Falta volume e uso natural |
| H3 | Recuar dobrando evita o desgaste | Ainda usar o app na semana 8 | ❓ não testada |
| H4 | Mensagem personalizada é notada mais que texto fixo | Alternar geradas e fixas, registrar reação | ❓ não testada |
| H5 | 20 min é um bom padrão | Olhar a distribuição real dos episódios | ❓ não testada |
| H6 | A medição é confiável nos dois apps | Rodada de testes | ✅ **respondida — ver abaixo** |
| H7 | Hesitação é um sinal interessante | Ver se aparece e se varia com o contexto | ✅ **mensurável.** 8% dos deslizes no Instagram, 6% no TikTok, com assinatura própria |
| **H8** | **Episódios puxados por notificação do próprio app duram mais, ou reagem pior ao aviso, que os iniciados espontaneamente** | Comparar duração e resposta ao aviso entre os dois grupos. Os dados vêm de graça (D17) | 🆕 não testada — **instrumento já existe** |

### H6 — respondida, com um resultado que mudou o produto

**A parte boa:** contagem de vídeos validada contra gabarito humano (16 = 16, erro zero),
mesma regra nos dois apps, superfícies distinguíveis, deslize revertido detectável.

**A parte que derrubou o desenho original:** eventos de acessibilidade **não medem
permanência**.

```
deslizando          →  23 ev/s      visível
ASSISTINDO PARADO   →   0 ev/s      INVISÍVEL por 158,7 s (medido)
saiu do app         →   0 ev/s      INVISÍVEL
```

Assistir e ter saído produzem o mesmo sinal. O contador de tempo — o coração do
produto — passou a se basear no `UsageStatsManager` (D13 do `PRODUTO.md`), que acertou
6 de 6 transições em teste controlado.

**Consequência de negócio, não só técnica:** o produto ficou **mais barato e mais
robusto**. Ver §7.

### H1 é a hipótese que sustenta o produto inteiro

Se saber o tempo não muda nada, o app é um cronômetro bonito. Ela só se responde com
uso real e registro honesto do que aconteceu depois de cada aviso — por isso o **F7**
entra na v1, não depois.

---

## 6. O que conta como sucesso

Para o caminho A (ferramenta pessoal), em ordem de importância:

| # | Métrica | Meta | Como sabe |
|---|---|---|---|
| S1 | **Continuar instalado e ativo na semana 8** | sim | Trivial. É o teste de sobrevivência |
| S2 | **Os avisos chegam em momentos que fazem sentido** | ≥70% "sim, era hora" | ✅ **agora tem instrumento:** os botões do F7 |
| S3 | **Nunca sentir vontade de desligar por irritação** | 0 vezes | Você sabe |
| S4 | **Redução do tempo total em vídeo curto** | qualquer queda | Comparar mês 1 com mês 3 |
| S5 | **Surpresas ("já passou tudo isso?") diminuem** | subjetivo | O incômodo original era esse |

S1 e S3 valem mais que S4. Um app que reduz o tempo mas irrita é desinstalado, e aí
a redução vira zero. **A sobrevivência do app é pré-requisito de qualquer efeito.**

> A S2 era a métrica sem instrumento — a mesma lacuna que travou o caminho B.
> Com o F7, ela passa a ser medível com um toque.

---

## 7. Riscos

| Risco | Chance | Impacto | O que fazer |
|---|---|---|---|
| **A pessoa se acostuma e ignora sempre** | Alta | Alto | Recuo dobrado; variar a mensagem; teto diário |
| **A permissão assusta** | Média→**Baixa** | Alto | ✅ **melhorou.** A acessibilidade virou **opcional** (D15). O obrigatório é só "acesso ao uso", que é a tela mais branda. O app funciona inteiro sem a assustadora |
| **Instagram/TikTok mudam e a detecção de tempo quebra** | ~~Alta~~ → **Muito baixa** | Médio | ✅ **quase eliminado.** O contador depende só do nome do package. `ACTIVITY_RESUMED` não muda quando o Instagram redesenha o Reels |
| **Instagram/TikTok mudam e a detecção de comportamento quebra** | Alta (em meses) | **Baixo** | Só afeta o F5 (contagem de vídeos, deslize revertido). O produto continua funcionando, com estatísticas mais pobres |
| **A pessoa usa outra superfície pra escapar** | ~~Média~~ → **Baixa** | Baixo | ✅ feed e stories agora contam (D14). Sobrou o YouTube Shorts |
| **H1 é falsa — saber não muda nada** | Média | **Fatal** | Descobrir cedo, em 4 semanas de uso próprio. O F7 é o que permite descobrir |
| **A mensagem gerada soa estranha ou julgadora** | Média | Médio | Limites rígidos no que ela pode dizer; sempre existe alternativa escrita à mão |
| **O aparelho não suporta geração no dispositivo** | Média | Baixa | As mensagens escritas à mão são a fundação, não o plano B |
| **Consumo de bateria perceptível** | ~~Baixa~~ → **Muito baixa** | Alto | ✅ o `UsageStats` já é gravado pelo sistema. Sem a acessibilidade, o app quase não gasta. Com ela, medimos 6–23 eventos/s, e 93% do volume é ruído descartável |
| **O One UI congela o app em segundo plano** | Alta | ~~Médio~~ → **Baixo** | 🆕 medido: `freeze` a cada 6,0 s exatos. O contador sobrevive (registro do sistema). ✅ **o aviso TAMBÉM — provado no aparelho (2026-08-09):** o alarme exato disparou **12 ms de atraso em Doze deep** (`GAPS.md` G1). O `setExactAndAllowWhileIdle` atravessa o freeze/Doze (D22). Mais 4 camadas no D23. **Risco praticamente eliminado no núcleo** |
| **Permissão faltando mata o produto em silêncio** | 🆕 — | **Alto** | 🆕 descoberto na revisão: faltavam `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` e o passo de *restricted settings*. Sem a 1ª o aviso não aparece; sem a 2ª o serviço morre a cada reboot. Agora no onboarding e no manifesto — ver F1 do `PRODUTO.md` |
| **O usuário primário não usa TikTok** | — | Médio | 🆕 o D2 fica sem evidência própria. Ver §2 e Q7 |

**Leitura geral: o quadro de riscos melhorou de novo na revisão de permissões.** O
congelamento do One UI caiu de Médio pra Baixo (o alarme exato atravessa o freeze), e o
gap de permissões faltantes — que matava o produto em silêncio — foi fechado no desenho.
O único fatal (H1) continua sendo o que decide o projeto, e tem instrumento de medida.

---

## 8. Entrega por fases — valor, não tarefa

| Fase | O que a pessoa ganha | Pergunta que responde | Estado |
|---|---|---|---|
| **F0** · dias | *(nada visível)* | A medição funciona nos dois apps? O aviso aparece por cima do app? | ✅ **CONCLUÍDA** |
| **F1** · ~1 semana | Um aviso quando passa do limite + os dois botões | H1 procede? Vale continuar? | próxima |
| **F2** · +1 semana | Perfil + mensagem no seu tom | Personalizar faz diferença? | |
| **F3** · +1 semana | Histórico e observações do padrão | Ver o histórico muda algo? | |
| **F4** · v2 | Aviso na pulseira, sem tela | Um toque no pulso funciona melhor que a tela? | |

**F0 respondeu as duas perguntas:**
- *A medição funciona nos dois apps?* Sim — mas não do jeito planejado. O tempo vem do
  sistema, não dos eventos. A contagem de vídeos funciona e foi validada contra gabarito humano.
- *O aviso aparece por cima do app?* **Sim.** Heads-up sobre Reels em tela cheia, confirmado
  em log. Dispensa permissão de sobreposição.

> ✅ **F0 ampliada (2026-08-09) — a arquitetura de sobrevivência foi provada no aparelho.**
> Um probe APK descartável (`targetSdk 36`) fechou os testes decisivos do `GAPS.md` no
> SM-A536E: o alarme exato dispara o aviso através do Doze deep (**12 ms** de atraso), o
> FGS `specialUse` sobe limpo, e o heads-up do próprio app aparece sobre o Reels. Ou seja:
> o app **sobrevive ao One UI** — e "a sobrevivência do app é pré-requisito de qualquer
> efeito" (§6). O risco técnico saiu da frente. **O que resta pra F1 é comportamental
> (H1), não de plataforma.**

**F1 continua sendo o marco que decide o projeto.** É a menor coisa que testa H1 — e
agora inclui os botões, sem os quais o teste não tem leitura. **O caminho de plataforma
até ele está aberto.**

---

## 9. Privacidade como posicionamento

Um app que observa o que você usa carrega um ônus de confiança maior que o normal.
A resposta aqui não é uma política — é uma restrição de arquitetura.

> ⚠️ **Esta seção mudou. O app passou a poder ver mais do que via.**
> O `UsageStatsManager` dá acesso ao histórico de **todos** os apps do aparelho, não
> só dos dois alvos. É mais amplo do que a acessibilidade filtrada por package.
> Isso precisa ser dito com todas as letras — omitir seria o oposto do posicionamento.

```
o que o app PODE ler  →  quais apps foram abertos, quando e por quanto tempo
                         (todos eles — é assim que a API funciona)

o que o app GUARDA    →  só Instagram e TikTok. Nada mais é gravado.

o que o app NÃO lê    →  o conteúdo dos vídeos
                         quem você segue
                         o que você escreve
                         qualquer imagem ou texto da tela

pra onde vai          →  lugar nenhum. Fica no aparelho.
com quem falamos      →  ninguém. Sem conta, sem servidor.
apagar tudo           →  um botão, imediato, sem pergunta
```

**A diferença entre "pode ler" e "guarda" é a promessa inteira.** Ela é verificável de
duas formas: o app é local (P4) e o código é seu. Num produto de mercado (caminho C),
isso exigiria auditoria ou código aberto para ser crível.

**Em compensação, a permissão mais invasiva virou opcional.** A acessibilidade — que
tecnicamente permite ler a tela — deixou de ser necessária para o produto funcionar.
Quem não a conceder tem o contador e os avisos completos; perde só a contagem de vídeos.

---

## 10. As perguntas que faltam responder

**1. ~~É ferramenta pessoal, pesquisa ou produto?~~** ✅ **FECHADA em 2026-08-08.**
Caminho **A — ferramenta pessoal**. Ver §0. Com isso, tudo abaixo passa a ser estável.

**2. H1 é verdadeira?**
Saber que passaram 22 minutos muda o que a pessoa faz? Quatro semanas de uso e o
registro do F7 respondem. É a única hipótese que pode matar o projeto.

**3. Quanto tempo você aguenta este app?**
Não é sobre eficácia — é sobre convivência. Ferramenta de bem-estar que não sobrevive
à convivência não tem eficácia nenhuma, por melhor que meça.

**4. 🆕 TikTok continua no escopo?**
O usuário da v1 não tem conta. Manter custa quase nada; o que se perde é evidência
própria do diferencial nº 1. Decisão do dono, não dos dados.

---

*Última atualização: 2026-08-09 (caminho A fechado; revisão de riscos de permissão e congelamento)*
*Companheiro: `PRODUTO.md` (escopo e decisões)*
*Evidência: `REELS.md` · `TIKTOK.md` · logs crus em `logs/`*
