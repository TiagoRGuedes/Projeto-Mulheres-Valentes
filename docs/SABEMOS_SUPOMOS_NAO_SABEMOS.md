# Sabemos, supomos e não sabemos

## Como ler

Este quadro impede que uma ideia de solução seja confundida com uma informação fornecida pela ONG. Deve ser atualizado depois de cada conversa, sempre registrando a origem da mudança.

**Atualizado após a entrevista** registrada no documento de escopo (AC1).

## Sabemos

Do tema recebido:

1. a ONG realiza controle de presença;
2. a ONG mantém um livro caixa;
3. o controle de presença é feito manualmente;
4. o livro caixa é feito manualmente;
5. há interesse em digitalizar esses controles e necessidade de facilidade de uso e orientação para pessoas com pouca familiaridade digital.

Da entrevista:

6. a ONG atende regularmente 60 participantes e as atividades acontecem todos os dias;
7. os dois registros são feitos em um caderno, sem sistema, planilha ou pessoa dedicada;
8. **o controle de presença é único, não vinculado a uma atividade específica**;
9. não existe hoje um livro caixa formal: a ONG não lança valores;
10. existe uma única entrada monetária fixa, um aluguel/patrocínio de R$ 1.200 por mês;
11. doações de material não são dinheiro: chegam fisicamente e são controladas por comprovante;
12. já faltou dinheiro para pagar o aluguel — o que justifica um controle financeiro mínimo;
13. é comum uma participante faltar e depois precisar ser reincluída no controle;
14. **se o sistema resolvesse só uma coisa, a prioridade é o livro caixa, não a presença**;
15. quem precisa de treinamento são Karine e Isabelle, com nível médio de familiaridade digital.

## Supomos

Os pontos abaixo continuam sendo hipóteses de trabalho, não confirmadas pela ONG:

- será útil manter um cadastro de participantes;
- **a presença poderá ser organizada por atividade e por encontro** — a entrevista
  contrariou parcialmente esta suposição: o controle é único. No protótipo, o
  vínculo com atividade virou opcional em vez de ser removido, porque já estava
  implementado e pode ajudar quem quiser separar a chamada;
- uma tela coletiva será mais adequada que editar uma pessoa de cada vez;
- os estados presente, ausente e justificada representam os casos necessários;
- participantes e atividades poderão ser desativados em vez de excluídos;
- a ONG desejará consultar frequência individual e por atividade;
- o livro caixa poderá ser representado por entradas e saídas;
- cada lançamento terá data, categoria, descrição, valor e observação opcional
  — ajustado pela entrevista: o valor só existe nos lançamentos monetários, e a
  doação de material é controlada por comprovante;
- categorias configuráveis facilitarão a organização;
- o saldo poderá ser calculado como entradas menos saídas;
- filtros por data, tipo, categoria e descrição serão úteis;
- relatórios impressos e arquivo CSV poderão apoiar consultas;
- a aplicação será usada em navegador por uma ou mais pessoas;
- guardar preferências de acessibilidade durante a sessão será suficiente no protótipo;
- dados fictícios facilitarão a apresentação acadêmica;
- uma interface com instruções curtas reduzirá a necessidade de treinamento.

## Não sabemos

### Presença

*Respondidas na entrevista:* como a ONG organiza a presença (controle único, sem
atividade) e o volume atendido (60 participantes, atividades diárias).

- Quem participa dos controles e como essas pessoas são chamadas pela ONG?
- Quem registra a presença?
- Em que momento e local o registro é feito?
- Quais situações de presença existem de fato? Presente, ausente e justificada
  são suficientes, ou a ONG só marca quem veio?
- Como são tratadas correções?
- Como novos participantes entram na lista?
- Quais dados pessoais são realmente necessários?
- O histórico de pessoas inativas deve permanecer visível?
- Existe algum cálculo de frequência, meta ou regra de elegibilidade?
- Qual é o volume médio e máximo de participantes e encontros?

### Livro caixa

*Respondidas na entrevista:* as categorias reais (aluguel/patrocínio e doação de
material), a inexistência de lançamentos formais hoje e a necessidade de corrigir
um lançamento feito por engano.

- O patrocínio de R$ 1.200 é sempre fixo, ou pode variar de mês a mês?
- O comprovante da doação de material precisa ser só uma descrição em texto, ou também um anexo?
- Quem registra entradas e quem registra saídas?
- Existe saldo inicial?
- Há mais de um caixa, conta, projeto, centro de custo ou fonte de recurso?
- Como um lançamento incorreto é corrigido?
- É permitido apagar ou deve haver estorno?
- Quais comprovantes precisam ser relacionados?
- Há numeração, responsável, beneficiário, forma de pagamento ou documento obrigatório?
- Qual período é usado para fechamento?
- Quais regras de prestação de contas precisam ser atendidas?
- O saldo do sistema deve ser comparado com dinheiro físico ou conta bancária?
- Existem lançamentos futuros, recorrentes, parcelados ou transferências internas?

### Uso e operação

*Respondida na entrevista:* quem vai precisar de treinamento (Karine e Isabelle,
nível médio de familiaridade digital).

- Que dispositivo a equipe usa no dia a dia: computador, celular, os dois?
- Quantas pessoas usarão o sistema e ao mesmo tempo?
- Quais aparelhos e navegadores estão disponíveis?
- A internet é estável no local de uso?
- O sistema precisa funcionar fora da sede?
- Quais dificuldades digitais aparecem com mais frequência?
- Que palavras e exemplos são mais familiares para as pessoas usuárias?
- Quais recursos de acessibilidade são prioritários?
- Quem poderá consultar, alterar, exportar ou imprimir informações?
- Como deverá funcionar backup, recuperação e retenção?
- Onde o sistema poderá ser hospedado?
- Como será feito o suporte depois da entrega acadêmica?
- Quais dados são sensíveis e quais cuidados de privacidade são exigidos?

## Sugestões ainda não validadas

- testar o protótipo com tarefas reais e observar onde surgem dúvidas;
- usar desativação para cadastros históricos;
- separar claramente entrada e saída;
- exibir texto junto de todo ícone;
- oferecer um resumo antes de imprimir;
- adotar trilha de auditoria se mais de uma pessoa puder alterar finanças;
- definir uma rotina de backup antes do uso real;
- revisar a linguagem com representantes da ONG;
- impedir o uso de dados reais durante demonstrações acadêmicas.

## Regra de atualização

Um item só deve migrar para “Sabemos” quando houver uma resposta explícita da ONG, registrada com data e fonte. Inferências da equipe permanecem em “Supomos”, mesmo quando parecem prováveis.
