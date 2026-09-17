# Modelo de dados preliminar

## Status

Este modelo foi criado para viabilizar o protótipo acadêmico. As entidades e regras são **premissas de solução**, não uma descrição confirmada do processo da ONG. Depois da entrevista, nomes, campos, cardinalidades e regras deverão ser revistos.

O projeto usa tipos Java no domínio e H2 persistente em desenvolvimento:

- identificadores: Long;
- datas de negócio: LocalDate;
- datas técnicas de registro: LocalDateTime;
- valores: BigDecimal;
- estados: enum;
- valor monetário no banco: DECIMAL(10,2).

## Visão de relacionamentos

~~~mermaid
erDiagram
    PARTICIPANTE ||--o{ PRESENCA : possui
    ATIVIDADE ||--o{ ENCONTRO : possui
    ENCONTRO ||--o{ PRESENCA : recebe
    CATEGORIA_FINANCEIRA ||--o{ LANCAMENTO_CAIXA : classifica

    PARTICIPANTE {
        Long id PK
        String nome
        Boolean ativo
        String observacoes
        LocalDateTime dataCadastro
        LocalDateTime dataAtualizacao
    }

    ATIVIDADE {
        Long id PK
        String nome
        String descricao
        Boolean ativo
        LocalDateTime dataCadastro
    }

    ENCONTRO {
        Long id PK
        Long atividade_id FK
        LocalDate data
        String observacoes
        LocalDateTime dataCadastro
    }

    PRESENCA {
        Long id PK
        Long participante_id FK
        Long encontro_id FK
        StatusPresenca status
        String observacoes
        LocalDateTime registradoEm
    }

    CATEGORIA_FINANCEIRA {
        Long id PK
        String nome
        TipoLancamento tipo
        Boolean ativo
    }

    LANCAMENTO_CAIXA {
        Long id PK
        LocalDate data
        TipoLancamento tipo
        Long categoria_id FK
        String descricao
        BigDecimal valor
        String observacoes
        LocalDateTime registradoEm
        LocalDateTime atualizadoEm
        Boolean cancelado
        LocalDateTime canceladoEm
    }

    HISTORICO_OPERACAO {
        Long id PK
        ModuloHistorico modulo
        String tipoEntidade
        Long entidadeId
        String nomeEntidade
        AcaoHistorico acao
        String descricao
        String valorAnteriorResumo
        String valorNovoResumo
        LocalDateTime dataHora
        Long usuarioId
        String usuarioNome
    }
~~~

## Participante

Representa, no protótipo, uma pessoa disponível para o controle de presença.

| Campo | Tipo Java | Obrigatório | Regra preliminar |
|---|---|---:|---|
| id | Long | sim | chave primária gerada |
| nome | String | sim | não vazio; tamanho limitado na aplicação |
| ativo | Boolean | sim | inicia como true |
| observacoes | String | não | texto livre com tamanho limitado |
| dataCadastro | LocalDateTime | sim | definida na criação |
| dataAtualizacao | LocalDateTime | sim | atualizada a cada alteração |

**Decisões de privacidade**

- documento, telefone, e-mail, endereço e data de nascimento não fazem parte do modelo;
- não existe requisito confirmado para coletar esses dados;
- nomes demonstrativos devem ser fictícios;
- desativação preserva o histórico.

**Dúvidas:** como diferenciar homônimos; quais dados são indispensáveis; por quanto tempo guardar; quem pode consultar.

## Atividade

Representa um tipo de ação que pode ocorrer em uma ou mais datas. A própria existência desse conceito no processo real ainda deve ser validada.

| Campo | Tipo Java | Obrigatório | Regra preliminar |
|---|---|---:|---|
| id | Long | sim | chave primária gerada |
| nome | String | sim | não vazio |
| descricao | String | não | explicação opcional |
| ativo | Boolean | sim | inicia como true |
| dataCadastro | LocalDateTime | sim | definida na criação |

Atividade inativa não deve ser apagada se já houver encontros associados.

## Encontro

Representa uma ocorrência datada de uma atividade.

| Campo | Tipo Java | Obrigatório | Regra preliminar |
|---|---|---:|---|
| id | Long | sim | chave primária gerada |
| atividade | Atividade | sim | associação ManyToOne |
| data | LocalDate | sim | data da ocorrência |
| observacoes | String | não | detalhe opcional |
| dataCadastro | LocalDateTime | sim | definida na criação |

Não foi confirmada uma regra de unicidade para atividade + data. Podem existir dois encontros da mesma atividade no mesmo dia. Se o processo real usar turno, local ou grupo, o modelo deve ser ampliado antes de criar uma restrição.

## Presenca

Liga participante e encontro e registra uma situação.

| Campo | Tipo Java | Obrigatório | Regra preliminar |
|---|---|---:|---|
| id | Long | sim | chave primária gerada |
| participante | Participante | sim | associação ManyToOne |
| encontro | Encontro | sim | associação ManyToOne |
| status | StatusPresenca | sim | armazenado como texto |
| observacoes | String | não | justificativa ou detalhe opcional |
| registradoEm | LocalDateTime | sim | definida no registro |

### StatusPresenca

- PRESENTE;
- AUSENTE;
- JUSTIFICADA.

Esses três estados foram definidos para o protótipo, mas ainda não foram confirmados pela ONG.

### Integridade

Existe unicidade para participante_id + encontro_id. A validação deve ocorrer no service e também no banco, protegendo contra requisições concorrentes.

Mensagem esperada:

> Já existe uma presença registrada para este participante nesse encontro.

## CategoriaFinanceira

Representa um agrupamento configurável para lançamentos.

| Campo | Tipo Java | Obrigatório | Regra preliminar |
|---|---|---:|---|
| id | Long | sim | chave primária gerada |
| nome | String | sim | não vazio |
| tipo | TipoLancamento | sim | ENTRADA ou SAIDA |
| ativo | Boolean | sim | inicia como true |

É recomendada unicidade de nome + tipo, comparada de maneira consistente. Uma categoria de entrada não pode ser usada em uma saída e vice-versa.

Categorias de demonstração são fictícias:

- entrada: Doação, Contribuição, Evento e Outros;
- saída: Materiais, Transporte, Alimentação, Manutenção e Outros.

Esses nomes não são categorias oficiais da ONG.

## LancamentoCaixa

Representa uma movimentação individual do livro caixa.

| Campo | Tipo Java | Coluna sugerida | Obrigatório | Regra preliminar |
|---|---|---|---:|---|
| id | Long | BIGINT | sim | chave primária gerada |
| data | LocalDate | DATE | sim | data de negócio |
| tipo | TipoLancamento | VARCHAR | sim | armazenado como texto |
| categoria | CategoriaFinanceira | BIGINT/FK | sim | tipo compatível e categoria ativa no cadastro |
| descricao | String | VARCHAR | sim | não vazia |
| valor | BigDecimal | DECIMAL(10,2) | sim | estritamente maior que zero |
| observacoes | String | VARCHAR | não | texto opcional |
| registradoEm | LocalDateTime | TIMESTAMP | sim | definida na criação |
| atualizadoEm | LocalDateTime | TIMESTAMP | sim | atualizada na edição |
| cancelado | Boolean | BOOLEAN | sim | inicia false; quando true deixa de compor consultas e cálculos |
| canceladoEm | LocalDateTime | TIMESTAMP | não | momento do cancelamento lógico |

### TipoLancamento

- ENTRADA;
- SAIDA.

O valor sempre é armazenado positivo. O tipo determina seu efeito no saldo. Não são usados double ou float.

### Cancelamento lógico

Um lançamento não é removido fisicamente pelo fluxo comum. Ao confirmar o cancelamento, o service:

1. registra no Histórico do sistema um resumo com tipo, data, categoria, descrição e valor;
2. marca `cancelado = true` e informa `canceladoEm`;
3. exclui o item do saldo, extrato, relatórios, CSV, API e dashboards usuais.

Assim, a operação deixa de afetar o caixa, mas sua existência continua rastreável. A política definitiva de estorno/cancelamento ainda deve ser validada com a ONG.

## HistoricoOperacao

Registra alterações administrativas em linguagem resumida e sem guardar dados sensíveis desnecessários.

| Campo | Tipo Java | Obrigatório | Regra preliminar |
|---|---|---:|---|
| id | Long | sim | chave primária gerada |
| modulo | ModuloHistorico | sim | participantes, atividades, encontros, presenças, livro caixa ou categorias |
| tipoEntidade | String | sim | nome técnico estável do tipo afetado |
| entidadeId | Long | não | identificador que existia no momento da ação |
| nomeEntidade | String | sim | identificação humana curta |
| acao | AcaoHistorico | sim | criação, edição, ativação, desativação, exclusão, registro ou correção |
| descricao | String | sim | resumo compreensível da ação |
| valorAnteriorResumo | String | não | estado anterior estritamente necessário |
| valorNovoResumo | String | não | estado posterior estritamente necessário |
| dataHora | LocalDateTime | sim | horário técnico do registro |
| usuarioId / usuarioNome | Long / String | não | reservados para autenticação futura; permanecem vazios atualmente |

O histórico é somente leitura na interface. Não existe relacionamento JPA com a entidade afetada, pois a referência deve sobreviver à exclusão segura de um cadastro sem uso.

### Precisão e arredondamento

- escala de armazenamento: duas casas decimais;
- validação: valor maior que zero;
- arredondamento, quando necessário: HALF_UP;
- cálculos: BigDecimal.ZERO como ponto inicial;
- comparação: compareTo, e não equals, quando a escala não deve influenciar.

## Regras de cálculo

### Saldo geral

~~~text
saldo = soma(valores de ENTRADA) - soma(valores de SAIDA)
~~~

### Resumo do período

Para data inicial e final inclusivas:

~~~text
entradasDoPeriodo = soma de ENTRADA no intervalo
saidasDoPeriodo   = soma de SAIDA no intervalo
saldoDoPeriodo    = entradasDoPeriodo - saidasDoPeriodo
~~~

Saldo geral e saldo do período devem receber rótulos diferentes na interface.

**Premissa crítica:** o protótipo calcula desde os lançamentos existentes e não possui regra confirmada de saldo inicial, fechamento, mais de um caixa, conta bancária ou transferência interna.

### Frequência

Uma proposta inicial é:

~~~text
percentual = quantidade de PRESENTE / quantidade de encontros considerados × 100
~~~

Ausências justificadas aparecem separadamente e, nessa proposta, continuam no total de encontros. Essa fórmula não é requisito confirmado. A entrevista deve definir denominador, efeito de justificativas, vínculo com atividades e eventual regra de frequência mínima. Até lá, o sistema não deve classificar participantes como aprovados ou reprovados.

## Regras de ciclo de vida

- participantes, atividades e categorias preferem desativação;
- participante só pode ser excluído definitivamente quando não possui presenças;
- atividade só pode ser excluída quando não possui encontros;
- encontro só pode ser excluído quando não possui presenças;
- registros históricos mantêm referência a itens inativos;
- nenhuma remoção ocorre por GET;
- lançamento financeiro usa cancelamento lógico nesta versão e preserva resumo no Histórico do sistema;
- operações financeiras irreversíveis exigem confirmação visível no fluxo web;
- mensagens de erro não revelam SQL, stack trace ou classe Java.

## Índices recomendados

Índices são sugestões técnicas e devem acompanhar medições:

- participante por nome normalizado;
- encontro por data e atividade;
- presença por encontro;
- presença por participante;
- lançamento por data;
- lançamento por tipo e data;
- lançamento por categoria e data;
- lançamento por estado de cancelamento e data;
- histórico por data/hora, módulo, ação e entidade;
- restrição única de presença por participante e encontro.

## Banco de desenvolvimento

- URL: jdbc:h2:file:./dados/mulheresvalentes;
- arquivos persistidos em dados/;
- criação/evolução automática no protótipo com Hibernate;
- console H2 habilitado apenas no ambiente de desenvolvimento;
- dados de teste usam configuração separada.

H2 e ddl-auto=update facilitam desenvolvimento, mas não definem a solução de produção. Antes de uso real, decidir banco, migrações versionadas, backup, restauração, criptografia, acesso e retenção.

## Autenticação futura

ADMIN e OPERADOR são apenas nomes sugeridos para perfis futuros. Não há entidade de usuário nem permissões definitivas no domínio atual. A arquitetura deve permitir Spring Security sem acoplar previamente regras não confirmadas.

## Pontos obrigatórios de revisão após a entrevista

1. conceito real equivalente a participante, atividade e encontro;
2. campos pessoais necessários e base de tratamento;
3. estados e correções de presença;
4. cálculo de frequência;
5. categorias e dimensões financeiras;
6. saldo inicial e múltiplos caixas;
7. confirmar a política de edição, cancelamento/estorno, retenção e auditoria;
8. relatórios e prestação de contas;
9. usuários, permissões e privacidade;
10. volume, backup, hospedagem e migração.
