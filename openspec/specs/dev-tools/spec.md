# dev-tools Specification

## Purpose
TBD - created by archiving change test-alert-trigger. Update Purpose after archive.
## Requirements
### Requirement: Gatilho de aviso de teste

O app MUST oferecer, apenas em build de debug, uma ação que dispara um aviso na hora,
reusando o caminho real de composição e notificação, para verificar F2/F3/F7 sem esperar
o limite de tempo de vídeo curto ser atingido.

#### Scenario: Botão visível só em debug
- **WHEN** o usuário abre a tela de Ajustes num build de debug (`BuildConfig.DEBUG == true`)
- **THEN** um botão "Disparar aviso de teste" aparece numa seção de ferramentas de dev
- **AND** em build de release o botão não existe (o bloco fica atrás do gate `BuildConfig.DEBUG`)

#### Scenario: Dispara o aviso no tom atual pelo caminho real
- **WHEN** o usuário toca "Disparar aviso de teste"
- **THEN** o app compõe a mensagem no tom do perfil atual gerando pelo Gemini e validando
  com o `MessageGuard` (P2/P5)
- **AND** se a geração falhar, for insegura ou a chave estiver vazia, cai no template
  escrito à mão do tom atual
- **AND** posta a mesma notificação heads-up do aviso real via `Notifier`

#### Scenario: Não contamina o dado de pesquisa
- **WHEN** um aviso de teste é disparado
- **THEN** nenhuma linha é gravada em `alert_outcome` nem em episódios
- **AND** nenhuma migração de banco é necessária
- **AND** se o usuário tocar um botão de resposta (F7) da notificação de teste, a resposta
  é ignorada com segurança (id sentinela não casa com nenhuma linha)

### Requirement: Isolamento removível

O código das ferramentas de dev MUST ficar isolado, de modo que esconder ou remover o
recurso não exija tocar na lógica de produção além de um único ponto de contato.

#### Scenario: Ponto de contato único na produção
- **WHEN** um desenvolvedor quer esconder o recurso
- **THEN** basta remover a rota/linha de Debug do hub (gated por `BuildConfig.DEBUG`)
  ou apagar o pacote `dev/`
- **AND** nenhum código de produção (banco, `AlertEvaluator`, repositórios, fluxo de aviso)
  precisa mudar

### Requirement: Tela de Debug
As ferramentas de dev SHALL viver numa tela de Debug dedicada, alcançada por uma linha "Debug" no hub de ajustes, visível apenas em build de debug. O gatilho de aviso de teste (já existente) SHALL ficar nessa tela.

#### Scenario: Debug acessível só em debug
- **WHEN** o build é debug
- **THEN** a linha "Debug" aparece no hub e abre a tela de Debug com as ferramentas

#### Scenario: Ausente em release
- **WHEN** o build é release
- **THEN** não há linha nem tela de Debug

### Requirement: Alternar sempre mostrar onboarding (debug)
A tela de Debug SHALL oferecer um toggle "always show onboarding" e uma ação "reset onboarding now". O gate de launch SHALL honrar o flag persistido em vez de um reset hardcoded; em release o flag é desligado por padrão e o onboarding só reaparece se faltar consentimento/conclusão.

#### Scenario: Toggle liga o onboarding a cada launch
- **WHEN** o toggle "always show onboarding" está ligado (debug)
- **THEN** na próxima abertura o app reseta e mostra o onboarding

#### Scenario: Reset agora
- **WHEN** o usuário toca "reset onboarding now"
- **THEN** o consentimento/conclusão são zerados e o app volta pro onboarding

#### Scenario: Release não reseta
- **WHEN** o build é release
- **THEN** o flag é ignorado/desligado e o onboarding não reaparece após concluído

