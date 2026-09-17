# Decisões técnicas

## Contexto

Este documento registra escolhas da entrega acadêmica, seus motivos, consequências e pontos de revisão. Restrições técnicas do projeto não devem ser apresentadas como preferências confirmadas da ONG.

## Escala de status

- **Definida para a entrega:** faz parte da implementação acadêmica;
- **Provisória:** adotada para prototipar e deverá ser validada;
- **Futura:** preparada ou recomendada, mas não implementada como regra definitiva.

## DT01 — Projeto independente

**Status:** definida para a entrega.

O sistema possui Maven, banco, pacotes, configuração, entidades, repositório, código e documentação próprios. Não depende do sistema da ONG Remo Meu Rumo.

**Motivo:** isolamento evita acoplamento, vazamento de dados e confusão entre dois contextos.

**Consequência:** qualquer componente comum deverá ser avaliado e implementado separadamente; não haverá compartilhamento implícito.

## DT02 — Java 25, Spring Boot 4.1.1 e Maven

**Status:** definida para a entrega.

O projeto usa Java 25, Spring Boot 4.1.1 e Maven Wrapper.

**Motivo:** atender às restrições acadêmicas e tornar a execução reproduzível no Windows.

**Consequência:** o ambiente precisa de JDK 25. O Wrapper baixa uma versão compatível do Maven na primeira execução.

## DT03 — Aplicação web MVC no servidor

**Status:** definida para a entrega.

As páginas usam Spring MVC, Thymeleaf, HTML semântico e CSS próprio. JavaScript é evitado.

**Motivo:** manter o projeto essencialmente Java, reduzir dependências e entregar fluxos previsíveis.

**Consequência:** interações enviam requisições ao servidor; melhorias dinâmicas só serão adicionadas se forem realmente necessárias e acessíveis.

## DT04 — Arquitetura em camadas

**Status:** definida para a entrega.

~~~text
Controller → Service → Repository → Banco
       ↓         ↓
      DTO      Model
~~~

- controllers tratam HTTP, binding, validação de entrada e navegação;
- services aplicam regras e delimitam transações;
- repositories tratam consultas e persistência;
- models representam estado do domínio;
- DTOs evitam expor entidades diretamente em formulários e contratos externos;
- tratamento de erro é centralizado.

**Motivo:** facilitar teste, leitura e mudança depois da entrevista.

**Consequência:** saldo, frequência e validações entre entidades não devem ficar em controller ou template.

## DT05 — Pacote e estrutura

**Status:** definida para a entrega.

Pacote-base:

~~~text
com.ibmec.mulheresvalentes
~~~

Estrutura:

~~~text
src/main/java/com/ibmec/mulheresvalentes/
├── config/
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
└── service/
~~~

Templates são agrupados por área e compartilham fragments de head, cabeçalho, navegação, mensagens, acessibilidade e rodapé.

## DT06 — JPA/Hibernate e H2 persistente

**Status:** definida para desenvolvimento; provisória para qualquer outro ambiente.

O H2 usa arquivo em ./dados/mulheresvalentes e console em /h2-console.

**Motivo:** instalação simples, dados mantidos entre reinícios e boa experiência em laboratório.

**Consequência:** dados/ é ignorada pelo Git. H2 Console e ddl-auto=update não são apropriados para produção.

**Revisão necessária:** banco de produção, migrações versionadas, backup, restauração, concorrência e operação.

## DT07 — Tipos do domínio

**Status:** definida para a entrega.

- Long para IDs;
- LocalDate para data de negócio;
- LocalDateTime para registro técnico;
- BigDecimal para dinheiro;
- EnumType.STRING para enums;
- DECIMAL(10,2) para valor.

**Motivo:** evitar perda de precisão e tornar os dados legíveis no banco.

**Consequência:** valores são positivos; TipoLancamento decide soma ou subtração. Alterar o nome de um enum exige cuidado com dados existentes.

## DT08 — Saldo no service

**Status:** provisória quanto ao processo; definida quanto à implementação.

O service calcula:

~~~text
saldo = entradas - saídas
~~~

Filtros de período usam limites inclusivos. BigDecimal é usado do início ao fim.

**Motivo:** centralizar regra e impedir divergência entre página, API e relatório.

**Questão:** saldo inicial, múltiplos caixas, transferências e fechamento não foram confirmados.

## DT09 — Presença única por participante e encontro

**Status:** provisória.

Uma restrição única protege participante + encontro, acompanhada de validação no service.

**Motivo:** impedir contagem duplicada inclusive em requisições concorrentes.

**Consequência:** se houver turno, sessão ou outra dimensão, o modelo precisará ser revisto.

## DT10 — Categorias configuráveis

**Status:** provisória.

CategoriaFinanceira possui nome, tipo e estado ativo. Exemplos do DataLoader são fictícios.

**Motivo:** evitar codificar categorias que ainda não foram confirmadas.

**Consequência:** lançamento valida compatibilidade entre categoria e tipo. Categorias usadas no histórico são desativadas em vez de apagadas.

## DT11 — POST-Redirect-GET

**Status:** definida para a entrega.

Depois de um formulário POST bem-sucedido, o controller redireciona para uma página GET e usa mensagem temporária.

**Motivo:** evitar registro duplicado ao atualizar a página e apresentar confirmação clara.

**Consequência:** erros de validação retornam ao formulário preservando os valores; sucesso redireciona.

## DT12 — Operações destrutivas protegidas

**Status:** definida para a entrega; política financeira final provisória.

- nenhuma exclusão ocorre por GET;
- participantes, atividades e categorias preferem ativação/desativação;
- ações irreversíveis exigem confirmação;
- integridade referencial é respeitada.

**Motivo:** reduzir acidentes para pessoas com pouca experiência digital e preservar histórico.

**Questão:** edição, exclusão, estorno e auditoria de lançamentos ainda precisam ser definidos pela ONG.

## DT13 — Validação em duas camadas

**Status:** definida para a entrega.

Bean Validation cobre formato e obrigatoriedade. Services cobrem regras que dependem do domínio, como valor positivo, compatibilidade de categoria, período válido e duplicidade.

**Motivo:** HTML pode ajudar, mas não protege sozinho requisições diretas à API.

**Consequência:** mensagens devem explicar como corrigir sem revelar detalhes técnicos.

## DT14 — Tratamento centralizado de erros

**Status:** definida para a entrega.

Exceções de negócio e recursos não encontrados são convertidos em páginas ou respostas JSON compreensíveis. Erros inesperados são registrados tecnicamente e apresentados de forma genérica.

**Motivo:** impedir exposição de SQL, stack trace, classe Java ou configuração.

**Consequência:** handlers diferenciam resposta web e REST e não engolem informações necessárias ao diagnóstico interno.

## DT15 — Interface que ensina

**Status:** definida como direção de UX; conteúdo provisório.

Cada tela importante deve incluir:

- título orientado à tarefa;
- frase curta explicando quando usar;
- label visível;
- obrigatoriedade clara;
- botão com verbo e objeto;
- confirmação;
- estado vazio com próximo passo;
- ajuda contextual;
- caminho de volta.

**Motivo:** facilidade de uso e orientação são parte da necessidade confirmada.

**Consequência:** ícone nunca substitui texto; jargão técnico não aparece para a pessoa usuária; linguagem precisa ser testada com a ONG.

## DT16 — Acessibilidade

**Status:** definida para a entrega.

São usados HTML semântico, link para pular conteúdo, foco visível, labels, mensagens associadas, teclado, contraste adequado, alvos grandes e tabelas com cabeçalho. Preferências de fonte, contraste, tema e animação são mantidas na sessão Java.

**Motivo:** garantir uso mais amplo e permitir personalização sem conta de usuário.

**Consequência:** sessão não mantém preferência em outro navegador. Persistência por usuário depende de autenticação futura.

## DT17 — Responsividade sem framework de CSS

**Status:** definida para a entrega.

CSS customizado usa layout fluido, grid, quebra de conteúdo, áreas roláveis para tabelas e media queries.

**Motivo:** identidade própria, menor dependência e compatibilidade com desktop, notebook, tablet e celular.

**Consequência:** telas precisam de verificação manual em diferentes larguras; tabelas não podem forçar a página inteira a rolar lateralmente.

## DT18 — API REST paralela às páginas

**Status:** definida para a entrega.

APIs cobrem participantes, presenças, lançamentos e resumo financeiro. DTOs separam contrato externo das entidades.

**Motivo:** cumprir o escopo técnico e preparar integrações futuras.

**Consequência:** sem autenticação, a API é somente local e demonstrativa. Versionamento e compatibilidade devem ser definidos antes de integração real.

## DT19 — Relatórios no servidor

**Status:** provisória quanto ao conteúdo.

Consultas, totais e preparação de CSV são feitos em Java. CSS de impressão adapta a página.

**Motivo:** manter a regra consistente e evitar lógica financeira importante no template.

**Consequência:** conteúdo e formato ainda precisam ser comparados com a prestação de contas real.

## DT20 — Dados demonstrativos idempotentes e fictícios

**Status:** definida para a entrega.

O DataLoader só cria exemplos quando a carga está habilitada e o banco não contém os registros esperados.

**Motivo:** permitir apresentação imediata sem duplicar dados a cada inicialização.

**Consequência:** todo conteúdo deve ser claramente fictício. O carregador deve ser desabilitado no ambiente real.

## DT21 — Estratégia de testes

**Status:** definida para a entrega.

Testes cobrem:

- entrada, saída, saldo e período;
- valor inválido e precisão BigDecimal;
- registro e duplicidade de presença;
- frequência e período vazio;
- repositories com H2 de teste;
- controllers e erros principais quando o risco justificar.

**Motivo:** proteger as regras centrais durante mudanças.

**Consequência:** testes não usam o arquivo H2 de desenvolvimento e devem ser executados com Maven Wrapper antes da entrega.

## DT22 — Preparação, não implementação, de autenticação

**Status:** futura.

A arquitetura evita depender de um usuário global e mantém services independentes de tela. Spring Security poderá ser adicionado depois.

ADMIN e OPERADOR são apenas sugestões de nomes.

**Motivo:** perfis, pessoas usuárias e permissões não foram confirmados.

**Consequência:** o protótipo não é seguro para exposição pública ou dados reais.

## DT23 — Formatação brasileira na borda

**Status:** definida para a entrega.

Datas e moeda são apresentadas em pt-BR; o domínio mantém LocalDate e BigDecimal. A API usa formatos estáveis, sem símbolo de moeda.

**Motivo:** separar armazenamento/cálculo da forma de leitura.

**Consequência:** conversores devem aceitar entrada compreensível e produzir erro claro quando o formato for inválido.

## DT24 — Identidade oficial adaptada ao sistema administrativo

**Status:** definida para a entrega visual.

O site oficial `mulheresvalentes.org.br` é a fonte principal de identidade. A logo e a fonte Manrope foram obtidas de arquivos publicados no próprio domínio da ONG e armazenadas localmente. A paleta administrativa usa verde-petróleo como base de confiança e organização, com magenta e rosa oficial em seleção, orientação e detalhes de marca.

**Motivo:** fazer o produto parecer parte da mesma instituição sem copiar a experiência do site público nem recorrer a uma estética genérica ou estereotipada.

**Consequências:**

- a logo mantém proporção, cores e arquivo original;
- o sistema funciona sem depender de assets remotos;
- ações financeiras continuam usando texto, seta e sinal, sem depender apenas de cor;
- estados de erro e sucesso preservam cores semânticas distintas dos destaques institucionais;
- modo escuro, alto contraste, responsividade e impressão possuem tratamento próprio;
- não foi criado favicon por não existir asset oficial identificado como apropriado para esse formato.

Detalhes e origem dos arquivos estão em `docs/IDENTIDADE_VISUAL.md`.

## DT25 — Histórico administrativo explícito e transacional

**Status:** definida para o protótipo; política organizacional pendente.

Os services de domínio chamam `HistoricoOperacaoService` explicitamente. O registro exige uma transação já aberta (`Propagation.MANDATORY`), portanto a mudança e sua trilha são confirmadas ou desfeitas juntas. A interface de Histórico é somente leitura e não oferece edição nem exclusão.

**Motivo:** tornar o comportamento fácil de explicar, testar e manter, sem AOP complexo. A referência é textual e não possui chave estrangeira para continuar legível mesmo após uma exclusão segura.

**Consequência:** `usuarioId` e `usuarioNome` permanecem vazios até existir autenticação. Retenção, perfis autorizados e conteúdo definitivo ainda precisam de validação.

## DT26 — Cancelamento lógico de lançamentos financeiros

**Status:** definida para o protótipo; regra da ONG pendente.

O comando existente de exclusão passou a marcar o lançamento como cancelado. Antes da marcação, o Histórico guarda tipo, data, categoria, descrição e valor. Consultas financeiras consideram apenas registros não cancelados.

**Motivo:** preservar rastreabilidade sem introduzir uma arquitetura contábil de estornos antes da entrevista.

**Consequência:** saldo, filtros, relatório, CSV, API e dashboard permanecem coerentes; a ONG ainda deve confirmar se deseja cancelamento, estorno, motivo obrigatório ou aprovação.

## Alternativas não adotadas

| Alternativa | Motivo de não adoção nesta entrega |
|---|---|
| React, Vue, Angular ou Next.js | fora das restrições e adicionariam outra camada de complexidade |
| Node.js, Python, PHP ou .NET no backend | o backend obrigatório é Java/Spring |
| Firebase como banco principal | não atende à arquitetura solicitada |
| Tailwind ou template visual pronto | a identidade deve ser própria e o CSS controlado |
| dinheiro em double ou float | risco de imprecisão |
| cálculo de saldo no template | duplicaria regra e dificultaria teste |
| exclusão por link GET | insegura e sujeita a acionamento acidental |
| campos pessoais preventivos | coleta sem necessidade confirmada |
| frequência mínima fixa | regra organizacional não confirmada |
| categorias codificadas no enum | dificultaria adaptação à realidade da ONG |

## Riscos técnicos e mitigação

| Risco | Mitigação no protótipo | Trabalho antes de produção |
|---|---|---|
| perda do banco local | persistência em arquivo e orientação de cópia | backup automatizado e teste de restauração |
| acesso sem controle | uso somente local e sem dados reais | autenticação, autorização e sessões seguras |
| alteração financeira sem trilha | confirmação, histórico transacional e cancelamento lógico | autenticação, retenção e política de estorno |
| regra errada por falta de entrevista | camadas e documentação de premissas | validação e migrações |
| concorrência no H2 | restrições de banco | banco e estratégia de produção |
| dados de demonstração confundidos com reais | rótulos e conteúdo fictício | desabilitar DataLoader |
| relatório inadequado | versão preliminar | validar prestação de contas |
| problemas em telas pequenas | CSS responsivo | testes com aparelhos reais |
| barreira de acessibilidade | semântica, teclado e preferências | teste com pessoas e auditoria especializada |

## Ambientes

### Desenvolvimento

- H2 em arquivo;
- console H2 habilitado;
- Thymeleaf sem cache;
- dados fictícios habilitados.

### Teste

- configuração separada;
- base isolada e descartável;
- nenhum acesso a ./dados/;
- carga controlada.

### Produção

Ainda não definida. Antes de criar esse ambiente, decidir:

- hospedagem e responsável técnico;
- banco e migrações;
- HTTPS;
- autenticação e permissões;
- segredo e configuração externa;
- logs sem dados sensíveis;
- backup e restauração;
- retenção e privacidade;
- monitoramento e suporte;
- desativação do H2 Console e dos dados fictícios.

## Critério de revisão

Uma decisão provisória deve ser revisada quando:

- a entrevista trouxer informação incompatível;
- uma pessoa usuária não conseguir completar o fluxo;
- surgir requisito de produção ou integração;
- o volume real superar a premissa;
- houver obrigação de privacidade, segurança ou prestação de contas;
- uma alteração de tecnologia afetar Java 25 ou Spring Boot 4.1.x.

## DT27 — Review Layer com token de uso único

**Status:** implementada para todas as mutações da interface web.

Cadastros, edições, alterações de situação, correções, exclusões seguras e
cancelamentos passam por uma tela intermediária comum. A preparação guarda o
DTO somente no servidor e não chama o service de domínio. A confirmação final
consome um token aleatório de 256 bits, vinculado à sessão e à operação, com
validade de dez minutos.

**Motivo:** permitir que uma pessoa leiga entenda o que acontecerá e evitar que
duplo clique, atualização da página ou repetição do POST dupliquem registros.

**Consequência:** voltar da revisão não modifica banco nem histórico. Somente um
POST final consegue executar a operação. APIs REST mantêm seu contrato próprio;
clientes de API continuam responsáveis por idempotência e autenticação quando
esse uso for definido.

## DT28 — Busca global server-side

**Status:** implementada.

A rota `/busca` consulta participantes, atividades, encontros, lançamentos
ativos e categorias em grupos limitados. As consultas usam repositories e
paginação curta, sem carregar tabelas completas no navegador.

**Motivo:** reduzir a necessidade de saber em qual módulo uma informação está e
tornar explícita a conexão entre as áreas do sistema.

## DT29 — Massa demonstrativa ampla sem apagar banco existente

**Status:** implementada para banco vazio.

Com `app.dados-demonstracao=true`, um banco vazio recebe 36 participantes, 6
atividades, 24 encontros, 864 presenças, 11 categorias, 50 lançamentos e uma
timeline representativa. A carga grava diretamente pelos repositories para não
produzir centenas de eventos artificiais de inicialização.

**Motivo:** permitir demonstrar dashboards, busca, filtros e diferentes faixas
de frequência com dados inteiramente fictícios.

**Consequência:** um banco já preenchido não é completado, apagado ou duplicado.
Essa proteção é intencional; recriar uma base de demonstração exige uma decisão
manual e uma cópia prévia de qualquer arquivo que precise ser preservado.

## DT30 — Consulta somente leitura de lançamento cancelado

**Status:** implementada.

O cancelamento continua excluindo o lançamento de saldos, movimentações ativas,
busca e relatórios. A rota de detalhes, porém, pode reabrir o registro preservado
a partir do Histórico do sistema e o identifica como **Cancelado**. Nesse estado,
a interface não oferece edição nem novo cancelamento.

**Motivo:** cumprir a rastreabilidade do Livro Caixa sem recolocar o lançamento
nos fluxos operacionais nem criar uma arquitetura contábil mais complexa.
