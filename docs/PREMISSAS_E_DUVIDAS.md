# Premissas e dúvidas

## Finalidade

Este é o registro vivo das hipóteses do projeto, atualizado após a entrevista
registrada no documento de escopo (AC1). A coluna **Classificação** usa:

- **Confirmado:** informação explicitamente fornecida sobre a realidade da ONG;
- **Premissa:** hipótese adotada temporariamente para conseguir prototipar;
- **Sugestão:** alternativa de solução a apresentar, sem afirmar que é necessária;
- **Não confirmado:** questão em aberto ou informação ainda sem fonte.

## Fatos confirmados

| ID | Informação | Classificação |
|---|---|---|
| C01 | Existe controle de presença. | Confirmado |
| C02 | Existe livro caixa. | Confirmado |
| C03 | O controle de presença é realizado manualmente. | Confirmado |
| C04 | O livro caixa é realizado manualmente. | Confirmado |
| C05 | Há interesse em digitalizar os controles e necessidade de facilidade de uso e orientação. | Confirmado |
| C06 | A ONG atende regularmente 60 participantes; as atividades acontecem todos os dias. | Confirmado — entrevista |
| C07 | O controle de presença é único, não vinculado a uma atividade específica. | Confirmado — entrevista |
| C08 | Não existe livro caixa formal hoje; a ONG não lança valores. | Confirmado — entrevista |
| C09 | Há uma única entrada monetária fixa: aluguel/patrocínio de R$ 1.200 por mês. | Confirmado — entrevista |
| C10 | Doações de material não têm valor monetário; são controladas por comprovante. | Confirmado — entrevista |
| C11 | Já faltou dinheiro para pagar o aluguel. | Confirmado — entrevista |
| C12 | É comum uma participante faltar e depois precisar ser reincluída no controle. | Confirmado — entrevista |
| C13 | A prioridade da ONG é o livro caixa, não a presença. | Confirmado — entrevista |
| C14 | Karine e Isabelle precisarão de treinamento; nível médio de familiaridade digital. | Confirmado — entrevista |

## Premissas do escopo (AC1)

| ID | Premissa | Status | Reflexo no protótipo |
|---|---|---|---|
| PR01 | Presença e livro caixa são controles independentes. | A confirmar | Os dois módulos continuam separados; nada liga presença a lançamento. |
| PR02 | O caixa não precisa de conciliação bancária. | Confirmado — realidade ainda mais simples | Modelo enxuto: entradas, saídas e doações sem valor. |
| PR03 | Duas pessoas, com nível médio de familiaridade digital, operam o sistema. | Confirmado — ajustado de uma para duas | Atendido: login com contas individuais (RNF04); a administração cadastra as contas em `/usuarios`. |
| PR04 | O treinamento pode ser atendido com um manual de uso simples. | A confirmar | `docs/GUIA_USUARIO.md` e a Central de Ajuda dentro do sistema. |
| PR05 | Aluguel/patrocínio é o único valor fixo; doações de material não têm valor. | Confirmado — entrevista | `NaturezaLancamento`: valor nulo e comprovante obrigatório na doação de material. |

## Dúvidas ainda em aberto

- O patrocínio de R$ 1.200 é sempre fixo, ou varia de mês a mês?
- O comprovante da doação precisa ser texto ou também um anexo de arquivo?
- Que dispositivo a equipe usa no dia a dia: computador, celular, os dois?
- O que causou a falta de dinheiro para pagar o aluguel?

## Premissas adotadas no protótipo

As linhas marcadas com ✅ ou ⚠️ foram tocadas pela entrevista.

| ID | Premissa | Motivo temporário | Risco se estiver errada | Como adaptar |
|---|---|---|---|---|
| P01 ✅ | Pessoas poderão ser representadas por um cadastro mínimo com nome, contato, estado ativo e observação. O contato entrou pelo RF01. | Permitir demonstração sem coletar dados pessoais desnecessários. | O cadastro pode não refletir a unidade real acompanhada. | Alterar DTO, entidade e telas após validação. |
| P02 ⚠️ | ~~A presença será organizada por atividade e encontro datado.~~ A entrevista confirmou um controle **único por data**. | Separar o tipo de ação de cada ocorrência. | Já se mostrou parcialmente errada. | Feito: a presença passou a ser registrada por data e o encontro virou opcional. |
| P03 ⚠️ | Cada participante terá uma única situação por encontro **e uma única situação por data no controle avulso**. | Evitar dupla contagem. | Pode existir mais de um turno ou sessão no mesmo dia. | Acrescentar o discriminador confirmado. |
| P04 | Presente, ausente e justificada serão estados iniciais. | Viabilizar o protótipo solicitado. | Podem faltar estados ou “ausente” pode nem ser registrado. | Ajustar enum e migração de dados. |
| P05 | O registro será feito para várias pessoas na mesma tela. | Reduzir repetição e tempo. | O processo real pode ocorrer individualmente ou por lista externa. | Oferecer fluxos alternativos. |
| P06 ✅ | O caixa será modelado por entradas e saídas com valores positivos, **mais a natureza do lançamento**: dinheiro ou doação de material sem valor. | Tornar cálculo e compreensão previsíveis. | O controle atual pode incluir estornos, transferências ou múltiplos caixas. | Ampliar tipos somente após entrevista. |
| P07 | Uma categoria ativa e compatível com o tipo classificará cada lançamento. | Ajudar filtros e organização. | Categoria pode não ser obrigatória ou pode haver mais dimensões. | Tornar opcional ou criar dimensões confirmadas. |
| P08 | O saldo do protótipo será entradas menos saídas desde o início dos registros. | Permitir resumo sem regra contábil adicional. | Pode existir saldo inicial ou fechamento por caixa. | Criar configuração ou lançamento de abertura validado. |
| P09 | Datas serão informadas sem horário; o horário será usado apenas no registro técnico. | Simplificar formulários. | A ordem no mesmo dia pode ser relevante. | Acrescentar horário de negócio se necessário. |
| P10 | Desativar é mais seguro que apagar participantes, atividades e categorias. | Preservar histórico. | A ONG pode ter política diferente. | Configurar regras conforme retenção aprovada. |
| P11 | A aplicação será usada em português do Brasil e valores em reais. | Coerência com o contexto do projeto. | Pode haver outra moeda ou terminologia preferida. | Internacionalizar formatos e textos. |
| P12 | Preferências de acessibilidade poderão durar a sessão. | Evitar conta de usuário no protótipo. | Usuários podem precisar de preferência persistente. | Persistir por perfil após autenticação. |
| P13 | Alterações administrativas relevantes serão registradas em uma trilha somente leitura. | Permitir rastreabilidade sem autenticação completa. | A política real pode exigir outro conteúdo, retenção ou aprovação. | Ajustar eventos e retenção após validação. |
| P14 | Excluir um lançamento significa cancelá-lo logicamente e retirá-lo dos cálculos. | Evitar destruição silenciosa do histórico financeiro. | A ONG pode exigir estorno, motivo obrigatório ou dupla aprovação. | Migrar para o fluxo confirmado preservando a trilha existente. |

## Sugestões de solução

| ID | Sugestão | Benefício esperado | Validação necessária |
|---|---|---|---|
| S01 | Usar cards grandes na Home com ação e explicação. | Facilitar descoberta. | Teste de uso com pessoas da ONG. |
| S02 | Incluir “marcar todos como presentes”. | Agilizar encontros com alta presença. | Entender risco de marcação acidental. |
| S03 | Exibir totais antes de salvar presença. | Ajudar conferência. | Verificar quais totais fazem sentido. |
| S04 | Manter categorias financeiras configuráveis. | Acompanhar vocabulário da ONG. | Levantar categorias e permissões reais. |
| S05 | Oferecer filtros e limpeza em um clique. | Tornar consultas recuperáveis. | Identificar filtros mais usados. |
| S06 | Criar versões para impressão e CSV. | Apoiar prestação de informações. | Confirmar formato, destinatário e campos. |
| S07 | Mostrar ajuda contextual nas telas. | Ensinar durante a tarefa. | Revisar linguagem com pessoas usuárias. |
| S08 | Oferecer fonte ampliada, alto contraste, tema escuro e menos animação. | Atender diferentes necessidades visuais. | Validar prioridades e comportamento desejado. |
| S09 | Incluir autenticação futura com perfis ADMIN e OPERADOR. | Preparar controle de acesso. | Identificar usuários, ações e política de senha. |
| S10 | Manter Histórico do sistema somente leitura, com filtros e resumos mínimos. | Dar rastreabilidade a correções sem expor conteúdo desnecessário. | Definir retenção, conteúdo, acesso e identificação do responsável. |

## Informações não confirmadas e dúvidas prioritárias

| ID | Pergunta em aberto | Impacto | Prioridade para entrevista |
|---|---|---|---|
| D01 | Como o controle de presença acontece hoje, do início ao fim? | Define o fluxo central. | Muito alta |
| D02 | Quem registra e quem consulta presença? | Define linguagem, acesso e responsabilidades. | Muito alta |
| D03 | O que identifica uma atividade, encontro, turma ou evento no processo atual? | Pode alterar o modelo de dados. | Muito alta |
| D04 | Quais situações são registradas além do comparecimento? | Define estados e validações. | Alta |
| D05 | Quais dados de uma pessoa são indispensáveis? | Afeta privacidade e formulário. | Muito alta |
| D06 | Como o livro caixa é preenchido e conferido hoje? | Define o fluxo financeiro central. | Muito alta |
| D07 | Existe saldo inicial ou mais de um caixa/conta? | Altera o cálculo do saldo. | Muito alta |
| D08 | Quais informações compõem cada entrada e saída? | Altera entidade, formulário e relatório. | Muito alta |
| D09 | Como corrigir um lançamento financeiro errado? | Define exclusão, edição, estorno e auditoria. | Muito alta |
| D10 | Quais categorias são realmente utilizadas? | Define carga inicial e filtros. | Alta |
| D11 | Que relatórios e prestações de contas são necessários? | Define campos, períodos e exportações. | Muito alta |
| D12 | Quantas pessoas, encontros e lançamentos existem por período? | Afeta paginação e desempenho. | Média |
| D13 | Quais aparelhos, conexão e ambiente de uso estão disponíveis? | Afeta responsividade e implantação. | Alta |
| D14 | Quais dificuldades as pessoas encontram com ferramentas digitais? | Direciona UX e ajuda. | Muito alta |
| D15 | Quem poderá ver e alterar quais informações? | Define autenticação e segurança. | Muito alta |
| D16 | Como serão backup e recuperação? | Afeta continuidade e risco de perda. | Muito alta antes de produção |
| D17 | Quais cuidados de privacidade e consentimento se aplicam? | Afeta coleta, retenção e acesso. | Muito alta antes de dados reais |
| D18 | Onde e por quem o sistema será mantido? | Define tecnologia de produção e suporte. | Alta |

## Restrições do projeto acadêmico

Java 25, Maven, Spring Boot 4.1.x, MVC, JPA/Hibernate, H2, Thymeleaf, HTML e CSS são escolhas determinadas para esta entrega. Não representam preferência confirmada da ONG. A mesma distinção vale para API REST, Maven Wrapper e estrutura de pacotes.

## Dados fictícios

Os dados demonstrativos são deliberadamente fictícios. Categorias como “Doação”, “Contribuição”, “Evento”, “Materiais”, “Transporte”, “Alimentação”, “Manutenção” e “Outros” são apenas exemplos modificáveis. Não são categorias oficiais da ONG.

## Decisões que não devem ser tomadas sem resposta

- coletar documento, telefone, e-mail, endereço ou outros dados pessoais;
- fixar percentual mínimo de frequência;
- tratar categorias ilustrativas como oficiais;
- apagar lançamentos financeiros de forma definitiva;
- definir quem é administrador ou operador;
- publicar a aplicação na internet;
- importar dados manuais;
- considerar o relatório do protótipo um documento contábil oficial;
- armazenar anexos ou comprovantes;
- usar nomes ou valores reais em demonstrações.

## Processo de atualização

Depois da entrevista:

1. registrar data, participante e evidência de cada resposta;
2. mudar a classificação somente quando houver confirmação explícita;
3. anotar contradições em vez de escolher uma versão silenciosamente;
4. revisar modelo de dados, backlog e endpoints;
5. solicitar validação das regras de presença, saldo e correção;
6. manter histórico das decisões alteradas.
