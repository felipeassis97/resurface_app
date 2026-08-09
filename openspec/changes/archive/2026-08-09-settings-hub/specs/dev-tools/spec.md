## ADDED Requirements

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

## MODIFIED Requirements

### Requirement: Isolamento removível

O código das ferramentas de dev MUST ficar isolado, de modo que esconder ou remover o
recurso não exija tocar na lógica de produção além de um único ponto de contato.

#### Scenario: Ponto de contato único na produção
- **WHEN** um desenvolvedor quer esconder o recurso
- **THEN** basta remover a rota/linha de Debug do hub (gated por `BuildConfig.DEBUG`)
  ou apagar o pacote `dev/`
- **AND** nenhum código de produção (banco, `AlertEvaluator`, repositórios, fluxo de aviso)
  precisa mudar
