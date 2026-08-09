# behavior-capture Specification

## Purpose
TBD - created by archiving change accessibility-capture. Update Purpose after archive.
## Requirements
### Requirement: Contar vídeos pela assinatura validada
O sistema SHALL contar um vídeo assistido para cada grupo de eventos `TYPE_VIEW_SCROLLED`
(`class` = `androidx.viewpager.widget.ViewPager`, `dy≠0`, `dx=0`) separado do grupo seguinte por
um intervalo maior que 0,5 s. É a regra validada contra gabarito humano (16 = 16, erro zero).

#### Scenario: Três eventos ViewPager = um vídeo
- **WHEN** chegam três `TYPE_VIEW_SCROLLED` de ViewPager com `dy≠0`, `dx=0`, dentro de ~200 ms
- **THEN** o detector conta exatamente um vídeo

#### Scenario: Dois grupos separados = dois vídeos
- **WHEN** dois grupos de eventos ViewPager são separados por mais de 0,5 s
- **THEN** o detector conta dois vídeos

### Requirement: Detectar hesitação
O sistema SHALL marcar como hesitação um grupo de deslize (ViewPager) que contenha `dy` negativo
no meio da rajada — o deslize começou e voltou (H7). É afirmação factual sobre o gesto, não sobre
estado mental (P2).

#### Scenario: dy negativo na rajada é hesitação
- **WHEN** um grupo ViewPager tem `dy` positivo seguido de `dy` negativo (ex.: 393, −323, −66)
- **THEN** o detector marca esse deslize como hesitação

### Requirement: Distinguir superfície e ignorar não-vídeo
O sistema SHALL classificar a superfície pela classe: `ViewPager` = Reels; `RecyclerView` =
feed/comentários. Rolar comentários (RecyclerView) NÃO SHALL contar como vídeo, e navegação lateral
(`dx≠0`, swipe entre abas) NÃO SHALL contar como vídeo.

#### Scenario: Comentários não inflam a contagem
- **WHEN** chegam eventos `TYPE_VIEW_SCROLLED` de `RecyclerView`
- **THEN** o detector não conta vídeos (nem hesitação) por eles

#### Scenario: Swipe lateral não conta
- **WHEN** chega um evento ViewPager com `dx≠0` (navegação entre abas)
- **THEN** o detector não conta um vídeo

### Requirement: Registrar comportamento sem afetar o tempo
O sistema SHALL persistir os eventos de comportamento (vídeo/hesitação, superfície, timestamp,
package) numa fonte separada. Isto NÃO SHALL alterar a contagem de tempo, os episódios, o aviso
nem o outcome (D13/F5) — é aditivo.

#### Scenario: Comportamento é gravado à parte
- **WHEN** um vídeo é detectado durante o uso
- **THEN** um evento de comportamento é persistido, e o contador de tempo/episódio permanece inalterado

### Requirement: Opcional e desligável
A captura de comportamento SHALL funcionar só quando a permissão de acessibilidade está concedida.
Sem ela, o app continua inteiro (contador, aviso, outcome) — só sem o dado de comportamento (D15).

#### Scenario: Sem permissão, sem captura, app inteiro
- **WHEN** a permissão de acessibilidade não está concedida
- **THEN** nenhum evento de comportamento é gravado, e o resto do app funciona normalmente

