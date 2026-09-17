# Escopo preliminar

## Identificação

- **Projeto:** Sistema de Presença e Livro Caixa Digital — ONG Mulheres Valentes
- **Disciplina:** IBM4024 — Projeto em Ciência de Dados II
- **Instituição:** IBMEC
- **Período:** 2026.2
- **Natureza:** projeto acadêmico independente

Este documento define uma proposta inicial para orientar prototipação e entrevista. Ele não substitui a validação com a ONG.

## Base confirmada

Até a entrevista, somente estes cinco fatos sobre a realidade da ONG são considerados confirmados:

1. há controle de presença;
2. há livro caixa;
3. o controle de presença é realizado manualmente;
4. o livro caixa é realizado manualmente;
5. existe interesse em digitalizar os controles e necessidade de uma experiência fácil, que oriente quem usa.

Nada além dessa lista deve ser apresentado como requisito organizacional confirmado.

## Objetivo proposto

Como **premissa de projeto**, criar uma aplicação web simples e independente que reúna:

- gestão de participantes, atividades, encontros e presença;
- registro e consulta de entradas e saídas;
- resumo de frequência e saldo;
- filtros e relatórios;
- ajuda durante o uso;
- recursos básicos de acessibilidade.

O objetivo é reduzir trabalho manual sem obrigar a pessoa usuária a conhecer termos técnicos. A adequação de cada item ao processo real ainda será validada.

## Escopo funcional preliminar

| Área | Proposta | Classificação organizacional |
|---|---|---|
| Participantes | cadastrar, pesquisar, visualizar, editar, ativar e desativar | Premissa |
| Atividades | cadastrar e organizar tipos de atividade | Premissa |
| Encontros | representar uma ocorrência datada de uma atividade | Premissa |
| Presença | registrar presente, ausente ou justificada em conjunto | Premissa |
| Frequência | calcular totais e percentual sem impor mínimo | Sugestão |
| Livro Caixa | registrar entradas e saídas com valor positivo | Premissa |
| Categorias | organizar lançamentos em categorias configuráveis | Sugestão |
| Extrato | pesquisar e filtrar movimentações | Sugestão |
| Saldo | somar entradas e subtrair saídas | Premissa |
| Relatórios | consultar e imprimir dados de presença e caixa | Sugestão |
| CSV | exportar movimentações quando necessário | Sugestão |
| Ajuda | explicar cada fluxo em linguagem simples | Premissa |
| Acessibilidade | fonte, contraste, tema escuro e redução de animações | Sugestão |
| API REST | permitir integração futura | Sugestão técnica |

## Fluxos propostos

### Presença

1. escolher uma atividade;
2. escolher ou cadastrar o encontro;
3. mostrar os participantes ativos;
4. marcar a situação de todos em uma única tela;
5. revisar totais;
6. salvar o conjunto;
7. informar claramente se funcionou.

Esse fluxo é uma premissa. Não se sabe ainda se a ONG organiza a presença por atividade, turma, evento, data ou outro conceito.

### Entrada

1. escolher **Registrar entrada**;
2. informar data, categoria, descrição e valor;
3. incluir observação, se necessário;
4. revisar;
5. salvar;
6. mostrar confirmação.

### Saída

1. escolher **Registrar saída**;
2. informar data, categoria, descrição e valor;
3. incluir observação, se necessário;
4. revisar;
5. salvar;
6. mostrar confirmação.

Os dois fluxos financeiros são premissas e não definem práticas contábeis da ONG.

## Relacionamentos propostos

- uma atividade pode possuir vários encontros;
- um encontro pertence a uma atividade;
- um participante pode possuir vários registros de presença;
- um encontro pode possuir vários registros de presença;
- cada combinação participante + encontro pode aparecer uma única vez;
- uma categoria financeira pode classificar vários lançamentos;
- um lançamento pertence a uma categoria e tem tipo entrada ou saída.

## Incluído nesta entrega acadêmica

- aplicação Java/Spring Boot executável de forma independente;
- interface web responsiva em português;
- persistência H2 local para desenvolvimento;
- Maven Wrapper;
- dados fictícios de demonstração;
- validações e mensagens compreensíveis;
- testes automatizados das regras centrais;
- documentação técnica e guia de uso;
- preparação arquitetural para mudanças após a entrevista.

## Fora do escopo atual

Os itens abaixo são **não confirmados** e não fazem parte da regra definitiva:

- autenticação e autorização em produção;
- perfis reais e suas permissões;
- integração bancária;
- emissão de nota, recibo fiscal ou documento contábil oficial;
- conciliação bancária;
- folha de pagamento;
- cobrança, PIX ou cartão;
- contabilidade avançada;
- frequência mínima obrigatória;
- envio de e-mail, SMS ou mensagens;
- armazenamento de documentos, telefone, endereço ou e-mail de participantes;
- integração com outros projetos;
- hospedagem e operação em produção;
- migração das planilhas ou livros atuais;
- política de backup, retenção e descarte;
- adequação jurídica e de privacidade concluída.

## Critérios preliminares de sucesso

Estes critérios são sugestões para validação com a ONG:

- uma pessoa que nunca usou o sistema encontra as ações principais na Home;
- o fluxo de presença não exige abrir um formulário por participante;
- entrada e saída têm títulos e mensagens diferentes;
- o saldo é calculado no servidor com BigDecimal;
- erros são explicados sem mostrar detalhes técnicos;
- nenhuma exclusão é acionada por requisição GET;
- telas funcionam com teclado e em tamanhos de celular, tablet e computador;
- relatórios deixam claro o período considerado;
- dados de demonstração são identificados como fictícios.

## Restrições técnicas da atividade acadêmica

As tecnologias Java 25, Maven, Spring Boot 4.1.x, Spring MVC, Spring Data JPA, Hibernate, H2, Thymeleaf, HTML e CSS são restrições do projeto acadêmico. Elas não são fatos levantados com a ONG.

## Estratégia para mudança

Para que respostas da entrevista possam alterar o produto com menor impacto:

- regras ficam em services;
- persistência fica em repositories;
- controllers não calculam saldo ou frequência;
- entidades não recebem campos pessoais não confirmados;
- categorias permanecem configuráveis;
- enums cobrem apenas estados preliminares solicitados;
- autenticação futura não é acoplada ao domínio atual;
- documentação registra a origem e o status de cada hipótese.

## Condição para fechar o escopo

O escopo somente deverá ser considerado validado depois de:

1. realizar a entrevista;
2. registrar respostas sem inferências;
3. atualizar premissas e dúvidas;
4. priorizar necessidades com representantes da ONG;
5. revisar protótipo e linguagem das telas;
6. definir privacidade, acesso, correção e backup;
7. obter concordância sobre o fluxo mínimo.
