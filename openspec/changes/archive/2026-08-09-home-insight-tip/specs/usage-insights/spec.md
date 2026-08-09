## REMOVED Requirements

### Requirement: Listar avisos com a resposta
**Reason**: O card de avisos saiu da home e não há outra tela que o exiba por ora. Os avisos continuam sendo gravados no `OutcomeRepository` (o dado não some).
**Migration**: Consultar via banco/relatório; uma tela de detalhes do H1 fica como trabalho futuro.

### Requirement: Resumo da razão de acerto (S2)
**Reason**: A proporção S2 deixou de ser exibida junto com o card de avisos removido. O dado bruto (respostas) segue gravado.
**Migration**: Recalcular a partir do `OutcomeRepository` numa futura tela de detalhes do H1.
