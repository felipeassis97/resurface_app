## 1. Gatilho isolado (pacote dev/)

- [x] 1.1 Criar pacote `com.resurface.resurface.dev/`.
- [x] 1.2 `TestAlertTrigger` (`@Singleton`): injeta `MessageGenerator`, `MessageGuard`,
      `TemplateComposer`, `ProfileRepository`, `Notifier`, `TimeProvider`, `@IoDispatcher`.
      `suspend fun fire()`: monta `Moment(limite, "Instagram", hora)`, gera → `guard.isSafe`
      → senão `templates.compose(...)`, e `notifier.postAlert(title, body, TEST_ALERT_ID)`.
- [x] 1.3 Definir `const val TEST_ALERT_ID = -1L` e comentar por quê (D-2, não grava outcome).
- [x] 1.4 Comentário de 1–2 linhas em cada método (G12).

## 2. ViewModel + UI (dev/)

- [x] 2.1 `DevToolsViewModel` (`@HiltViewModel`): injeta `TestAlertTrigger`; `onTestAlert()`
      chama `fire()` em `viewModelScope`.
- [x] 2.2 `DevToolsSection` (composable): título "Ferramentas de dev" + botão
      "Disparar aviso de teste" → `viewModel.onTestAlert()`. Usa tokens de tema (G10).

## 3. Ponto de contato único na produção

- [x] 3.1 Em `SettingsScreen`, após as seções existentes, adicionar **uma** chamada
      `if (BuildConfig.DEBUG) DevToolsSection()`. Importar `BuildConfig`.
- [x] 3.2 Confirmar que nenhum outro arquivo de produção referencia o pacote `dev/`.

## 4. Testes e verificação

- [x] 4.1 Teste unitário de `TestAlertTrigger.fire()` (G11): com gerador devolvendo mensagem
      segura → posta a gerada (`postAlert` chamado com o texto gerado); com gerador nulo →
      posta o template; nenhuma escrita em outcome/episódio.
- [x] 4.2 `./gradlew :app:testDebugUnitTest` verde.
- [x] 4.3 Instalar debug, abrir Ajustes, tocar o botão, confirmar notificação heads-up no
      tom atual no device.
