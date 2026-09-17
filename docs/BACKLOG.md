# Backlog preliminar

## Aviso de validação

As histórias abaixo orientam a construção acadêmica, mas são baseadas em premissas. Os papéis “pessoa responsável”, “pessoa que registra” e “pessoa que consulta” não representam cargos confirmados na ONG. Prioridades e critérios deverão ser revisados na entrevista.

## Escala

- **Prioridade P0:** essencial para demonstrar o fluxo mínimo;
- **Prioridade P1:** importante para tornar o fluxo útil e seguro;
- **Prioridade P2:** melhoria que pode ser replanejada;
- **Story points:** escala 1, 2, 3, 5, 8 e 13, estimando esforço relativo, risco e incerteza.

## Visão geral

| ID | História | Prioridade | Pontos | Classificação |
|---|---|---:|---:|---|
| US01 | Manter participantes | P0 | 5 | Premissa |
| US02 | Organizar atividades e encontros | P0 | 5 | Premissa |
| US03 | Registrar uma lista de presença | P0 | 8 | Premissa |
| US04 | Consultar histórico e frequência | P1 | 5 | Sugestão |
| US05 | Registrar uma movimentação financeira | P0 | 8 | Premissa |
| US06 | Conferir extrato e saldo | P0 | 8 | Premissa |
| US07 | Gerar relatórios compreensíveis | P1 | 8 | Sugestão |
| US08 | Aprender e adaptar a interface durante o uso | P1 | 5 | Premissa |

## US01 — Manter participantes

**História:** Como pessoa que prepara o controle de presença, quero localizar e manter uma lista mínima de participantes, para não reescrever os mesmos nomes a cada encontro.

**Prioridade:** P0  
**Story points:** 5  
**Dependências:** nenhuma.

**Critérios de aceitação**

1. É possível cadastrar uma pessoa informando nome e, opcionalmente, observação.
2. Nome vazio é recusado com uma mensagem que explica como corrigir.
3. A listagem permite pesquisa pelo nome sem exigir correspondência de maiúsculas e minúsculas.
4. É possível visualizar e editar o cadastro.
5. Desativar não apaga o histórico de presença.
6. O sistema informa claramente quando cadastro ou alteração foi salvo.
7. Documento, telefone e e-mail não são exigidos sem confirmação.

**Questões para validação:** dados realmente necessários; nomes duplicados; política de inativação; volume.

## US02 — Organizar atividades e encontros

**História:** Como pessoa que organiza as listas, quero relacionar cada data a uma atividade, para entender a que ocasião cada registro de presença se refere.

**Prioridade:** P0  
**Story points:** 5  
**Dependências:** nenhuma.

**Critérios de aceitação**

1. É possível cadastrar uma atividade com nome e descrição opcional.
2. É possível criar um encontro escolhendo atividade, data e observação opcional.
3. Atividades inativas continuam visíveis no histórico, mas não aparecem como escolha normal para novos encontros.
4. A tela informa qual atividade e data estão sendo consultadas.
5. Erros de validação não exibem nomes de classe, SQL ou stack trace.

**Questões para validação:** vocabulário correto; possibilidade de grupos, turnos ou locais; duplicidade de encontros.

## US03 — Registrar uma lista de presença

**História:** Como pessoa encarregada do encontro, quero marcar a situação de várias pessoas em uma única tela, para concluir a lista com menos repetição e conferir o total antes de salvar.

**Prioridade:** P0  
**Story points:** 8  
**Dependências:** US01 e US02.

**Critérios de aceitação**

1. O fluxo permite escolher atividade e encontro antes de mostrar a lista.
2. Participantes ativos aparecem juntos e podem ser pesquisados.
3. Cada pessoa pode receber uma das situações preliminares: presente, ausente ou justificada.
4. Existe uma ação explícita para marcar todas como presentes.
5. A tela apresenta total, presentes, ausentes e justificadas.
6. Todos os registros são enviados em uma única confirmação.
7. A combinação participante + encontro não pode ser duplicada.
8. Em caso de duplicidade, a mensagem é “Já existe uma presença registrada para este participante nesse encontro.” ou equivalente igualmente claro.
9. Salvar e atualizar seguem POST-Redirect-GET para evitar repetição ao atualizar a página.

**Questões para validação:** estados reais; valor inicial de cada linha; atualização de uma lista já salva; tratamento de participante que chega depois.

## US04 — Consultar histórico e frequência

**História:** Como pessoa que acompanha as atividades, quero consultar o histórico e os totais de participação por período e atividade, para compreender os registros sem fazer contas manualmente.

**Prioridade:** P1  
**Story points:** 5  
**Dependências:** US03.

**Critérios de aceitação**

1. A consulta apresenta encontros, presenças, ausências, justificadas e percentual.
2. Cálculos são realizados em Java, não no navegador.
3. Divisão por zero é tratada sem mostrar NaN, Infinity ou erro técnico.
4. Filtros deixam claro o período e a atividade considerados.
5. Nenhum percentual mínimo é classificado como aprovado ou reprovado.
6. Um estado vazio explica como criar os primeiros registros.

**Questões para validação:** denominador do percentual; efeito da justificativa; necessidade real de frequência mínima; relatórios esperados.

## US05 — Registrar uma movimentação financeira

**História:** Como pessoa que atualiza o caixa, quero registrar separadamente o valor que entrou ou saiu, para manter a movimentação organizada e reduzir confusão entre recebimento e gasto.

**Prioridade:** P0  
**Story points:** 8  
**Dependências:** categorias financeiras fictícias ou cadastradas.

**Critérios de aceitação**

1. Entrada e saída possuem títulos, textos e botões diferentes.
2. O formulário solicita data, categoria, descrição e valor; observação é opcional.
3. A categoria deve estar ativa e ser compatível com o tipo.
4. O valor é armazenado positivo com BigDecimal e duas casas decimais.
5. Zero e valor negativo são recusados com a mensagem “Informe um valor maior que zero.” ou equivalente.
6. Uma entrada salva mostra “Entrada registrada com sucesso.”
7. Uma saída salva mostra “Saída registrada com sucesso.”
8. Atualizar a página após salvar não cria outro lançamento.

**Questões para validação:** campos obrigatórios; categorias oficiais; anexos; saldo inicial; correção, estorno e exclusão.

## US06 — Conferir extrato e saldo

**História:** Como pessoa que consulta o caixa, quero encontrar movimentações e ver entradas, saídas e saldo do período, para conferir os valores sem recalcular a lista.

**Prioridade:** P0  
**Story points:** 8  
**Dependências:** US05.

**Critérios de aceitação**

1. O saldo é calculado no service como soma de entradas menos soma de saídas.
2. Valores usam BigDecimal e são apresentados como moeda brasileira.
3. O extrato diferencia “↑ Entrada” e “↓ Saída” por texto e símbolo, não apenas por cor.
4. É possível filtrar por data inicial, data final, tipo, categoria e trecho da descrição.
5. Existe uma ação visível para limpar filtros.
6. O resumo informa total de entradas, total de saídas, saldo e número de movimentações do período selecionado.
7. Um período invertido é recusado com explicação simples.
8. Sem movimentos, a página explica o estado vazio e oferece caminhos para registrar entrada e saída.

**Questões para validação:** saldo geral versus saldo filtrado; saldo inicial; múltiplos caixas; paginação; ordenação.

## US07 — Gerar relatórios compreensíveis

**História:** Como pessoa que precisa compartilhar ou revisar informações, quero gerar relatórios de presença e caixa com período identificado, para consultar e imprimir dados de forma organizada.

**Prioridade:** P1  
**Story points:** 8  
**Dependências:** US04 e US06.

**Critérios de aceitação**

1. O relatório financeiro informa período, movimentações, total de entradas, total de saídas e saldo do período.
2. Relatórios de presença permitem visão por participante, período e atividade.
3. A versão impressa remove navegação desnecessária e mantém título, período e cabeçalhos.
4. Tabelas possuem cabeçalho acessível e não dependem apenas de cor.
5. Exportação CSV, quando oferecida, preserva valores e datas sem fórmulas.
6. O relatório se identifica como apoio de gestão e não como documento contábil oficial.

**Questões para validação:** destinatários; campos; assinatura; periodicidade; formato; exigências de prestação de contas.

## US08 — Aprender e adaptar a interface durante o uso

**História:** Como pessoa com pouca experiência digital, quero encontrar instruções claras e ajustar a leitura, para completar uma tarefa básica sem depender de treinamento longo.

**Prioridade:** P1  
**Story points:** 5  
**Dependências:** fluxos principais.

**Critérios de aceitação**

1. A Home mostra as ações centrais com título, explicação e botão grande.
2. Páginas importantes respondem onde a pessoa está, o que pode fazer e como voltar.
3. A Central de Ajuda explica presença, entrada, saída, saldo, pesquisa, filtros e correção em passos curtos.
4. Campos possuem label, indicação de obrigatoriedade e mensagem de erro associada.
5. É possível navegar com teclado e perceber o foco.
6. Existe link para pular ao conteúdo principal.
7. É possível escolher fonte normal, grande ou muito grande, alto contraste, modo escuro e redução de animações.
8. Preferências permanecem durante a sessão.

**Questões para validação:** necessidades prioritárias; palavras compreendidas; dispositivos; persistência preferida; teste com usuários.

## Regras comuns de aceite

Uma história só deve ser considerada concluída quando:

- compila com Java 25;
- testes relevantes passam;
- validações ocorrem também no servidor;
- não há operação destrutiva por GET;
- mensagens são humanas e não revelam detalhes técnicos;
- navegação por teclado e foco foram conferidos;
- telas foram verificadas em larguras de celular, tablet e computador;
- exemplos permanecem fictícios;
- documentação afetada foi atualizada.

## Próxima revisão

Depois da entrevista, a equipe deve:

1. remover ou reescrever histórias rejeitadas;
2. substituir papéis genéricos pelos termos usados pela ONG;
3. repriorizar junto às pessoas responsáveis;
4. dividir histórias que ultrapassem um fluxo compreensível;
5. adicionar critérios de privacidade, auditoria e operação;
6. registrar a origem de cada requisito confirmado.
