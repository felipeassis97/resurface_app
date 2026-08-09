## Context

Última camada do F1: a UI. Tudo abaixo já existe e roda no aparelho. Segue o `ENGENHARIA.md`
§6 (ViewModel → UiState `StateFlow`, tela stateless com `collectAsStateWithLifecycle`), §7
(tokens do tema), §8 (as 3 telas já registradas no NavHost). Consome `EpisodeStateHolder`
(Q-A, opção 1), `EpisodeRepository`, `OutcomeRepository`, `ConfigRepository`.

## Goals / Non-Goals

**Goals:**
- Home com contador vivo (atualiza ~1 s), estado de repouso, indicador de pausa.
- Insights com episódios recentes + avisos/respostas + a razão S2.
- Settings com limite (10–60) e pausar por hoje.
- ViewModels testados (mapeamento puro + Turbine); telas não testadas (UI visual, G11).

**Non-Goals:**
- Onboarding real / gate de permissões (adiado; permissões via adb no F1).
- Mensagem no tom / perfil (F2). Gráficos ricos, heatmap, comparações (F5 completo). Editar hobbies.
- Animações elaboradas (só o essencial calmo; motion fino fica pra depois).

## Decisions

### D-1: Contador vivo = holder + ticker + relógio, com mapeamento puro
`HomeViewModel` faz `combine(holder.state, ticker1s, config.pausedToday)` e mapeia via uma função
**pura** `toHomeUiState(state, nowMillis, paused)`. O ticker (1 s) só força a re-emissão; o número
vem de `state.accumulatedMsAt(now)`. Assim o contador anda de segundo a segundo mesmo entre os
ticks de 45 s do serviço.
- **Testável:** a função de mapeamento é testada com `now` fixo; o ticker não entra no teste.
- **Alternativa recusada:** mostrar só o `bankedMs` do holder (pularia de 45 em 45 s, feio).

### D-2: ViewModels no padrão do ENGENHARIA §6
`StateFlow<UiState>` imutável via `combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), Inicial)`.
Eventos como funções (`onSetLimit`, `onPauseToday`) em `viewModelScope`. Repositório é a fonte
da verdade; a UI não flippa otimista.

### D-3: Insights deriva S2 puramente
`InsightsViewModel` combina `episodes.history` + `outcomes.outcomes` e deriva a razão de "era hora"
entre os respondidos numa função pura (testada). "Sem resposta" (ignorado) não entra no
denominador da S2, mas aparece na lista (P3).

### D-4: Telas Screen(VM) / Content(stateless)
Cada tela: `XScreen(vm = hiltViewModel())` coleta com `collectAsStateWithLifecycle` e passa pro
`XContent(state, onEvento)` stateless e `@Preview`-ável (§6.2). Número grande usa
`ResurfaceTextStyles.statDisplay` (tabular, não treme). Cores/spacing por token (G10).

### D-5: Rótulo do app compartilhado
O mapa pacote→rótulo (Instagram/TikTok/vídeo curto, D25) já existe no `AlertEvaluator`; extrair
pra um util compartilhado `AppLabels` pra a Home reusar sem duplicar.

## Risks / Trade-offs

- **[Ticker + teste]** um `Flow` de 1 s é chato de testar por tempo. Mitigação: testar só a função
  de mapeamento pura; o ticker é trivial e coberto por olho no aparelho.
- **[Holder frio no cold-start]** logo após abrir, o holder pode estar em INITIAL até o 1º tick do
  serviço (≤45 s). Mitigação: aceitável no F1 (mostra repouso brevemente); o serviço já roda.
- **[S2 sem respostas]** com 0 avisos respondidos a razão é indefinida. Mitigação: a função devolve
  null / "—" nesse caso; a UI mostra "sem dados ainda".

## Migration Plan

Não aplicável — só UI nova, sem dado nem schema.

## Open Questions

- Motion do contador (respiração/maré) fica pro polimento visual depois; o F1 usa incremento simples.
- Mostrar status de permissão nos ajustes? Adiado pro onboarding (F2); no F1 as permissões são adb.
